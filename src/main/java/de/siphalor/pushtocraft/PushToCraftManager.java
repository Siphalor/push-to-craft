package de.siphalor.pushtocraft;

import com.google.common.collect.Streams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Pair;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class PushToCraftManager extends JsonDataLoader {
	private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

	private final Map<String, Collection<Entry>> exactPushes = new HashMap<>();
	private final Map<String, Collection<Pair<Pattern, Entry>>> namespacePushes = new HashMap<>();
	private final Collection<Pair<Pattern, Entry>> flexiblePushes = new LinkedList<>();
	private final Collection<Entry> wildcardPushes = new LinkedList<>();

	private static PushToCraftManager instance;

	public static PushToCraftManager getInstance() {
		return instance;
	}

	public PushToCraftManager() {
		super(GSON, "push_to_craft");
		instance = this;
	}

	public String getName() {
		return PushToCraft.MOD_ID;
	}

	@Override
	protected void apply(Map<Identifier, JsonElement> resources, ResourceManager manager, Profiler profiler) {
		for (Map.Entry<Identifier, JsonElement> e : resources.entrySet()) {
			Identifier identifier = e.getKey();
			if (!e.getValue().isJsonObject()) {
				logError(identifier, "must be a JSON Object");
				continue;
			}
			JsonObject jsonObject = e.getValue().getAsJsonObject();
			if (jsonObject.has("additions")) {
				JsonElement element = jsonObject.get("additions");
				if (element.isJsonArray()) {
					LinkedList<String> additions = new LinkedList<>();
					for (JsonElement additionElement : element.getAsJsonArray()) {
						if (JsonHelper.isString(additionElement)) {
							additions.add(additionElement.getAsString());
						} else {
							logWarn(identifier, "contains an illegal additions entry of type " + additionElement.getClass().getSimpleName());
						}
					}

					Collection<String> targets = null;
					if (jsonObject.has("targets")) {
						element = jsonObject.get("targets");
						if (element.isJsonArray()) {
							targets = new LinkedList<>();
							for (JsonElement targetElement : element.getAsJsonArray()) {
								if (JsonHelper.isString(targetElement)) {
									targets.add(targetElement.getAsString());
								} else {
									logWarn(identifier, "has item/tag target in array of invalid type " + targetElement.getClass().getSimpleName() + " - should be string");
								}
							}
						} else if (JsonHelper.isString(element)) {
							targets = Collections.singleton(element.getAsString());
						} else {
							logError(identifier, "has item/tag target of invalid type " + element.getClass().getSimpleName() + " - should be string or array of strings");
						}
					}
					if (targets == null || targets.isEmpty()) {
						logError(identifier, "targets no items or tags");
						continue;
					}

					if (jsonObject.has("recipes")) {
						element = jsonObject.get("recipes");
						if (element.isJsonObject()) {
							jsonObject = element.getAsJsonObject();
							Collection<RecipeSerializer<?>> recipeSerializers = Collections.emptyList();

							if (jsonObject.has("types")) {
								element = jsonObject.get("types");
								if (element.isJsonArray()) {
									recipeSerializers = new LinkedList<>();
									for (JsonElement recipeSerializerElement : element.getAsJsonArray()) {
										if (JsonHelper.isString(recipeSerializerElement)) {
											Identifier recipeSerializerId = new Identifier(recipeSerializerElement.getAsString());
											if (Registry.RECIPE_SERIALIZER.containsId(recipeSerializerId)) {
												recipeSerializers.add(Registry.RECIPE_SERIALIZER.get(recipeSerializerId));
											} else {
												logWarn(identifier, "has recipe type specifier \"" + recipeSerializerId + "\" that could not be resolved to a recipe type");
											}
										} else {
											logWarn(identifier, "has recipe type specifier in array of invalid type " + recipeSerializerElement.getClass().getSimpleName() + " - should be a string");
										}
									}
									if (recipeSerializers.isEmpty()) {
										logError(identifier, "has empty or completely invalid recipe type specifier array");
										continue;
									}
								} else {
									logError(identifier, "has recipe type specifier of invalid type " + element.getClass().getSimpleName() + " - should be an array");
									continue;
								}
							}

							Entry entry = new Entry(targets, additions, recipeSerializers);

							if (jsonObject.has("ids")) {
								element = jsonObject.get("ids");
								if (element.isJsonArray()) {
									boolean valid = false;
									for (JsonElement idElement : element.getAsJsonArray()) {
										if (JsonHelper.isString(idElement)) {
											String id = idElement.getAsString();
											if (id.charAt(0) == '/' && id.endsWith("/")) {
												flexiblePushes.add(new Pair<>(
														Pattern.compile(id.substring(0, id.length() - 1)),
														entry
												));
											} else {
												int index = id.indexOf(':');
												String namespace, path;
												if (index == -1) {
													namespace = "minecraft";
													path = id;
												} else {
													namespace = id.substring(0, index);
													path = id.substring(index + 1);
												}
												if (path.charAt(0) == '/' && path.endsWith("/")) {
													namespacePushes.computeIfAbsent(namespace, s -> new LinkedList<>())
															.add(new Pair<>(Pattern.compile(path.substring(1, path.length() - 1)), entry));
												} else {
													exactPushes.computeIfAbsent(path, s -> new LinkedList<>())
															.add(entry);
												}
											}
											valid = true;
										} else {
											logWarn(identifier, "has recipe id in array of invalid type " + idElement.getClass().getSimpleName() + " - should be a string");
										}
									}
									if (!valid) {
										logError(identifier, "has no valid recipes ids in array");
									}
								} else {
									logError(identifier, "has recipe ids tag of invalid type " + element.getClass().getSimpleName() + " - should be an array");
								}
							} else {
								wildcardPushes.add(entry);
							}
						} else {
							logError(identifier, "has recipes specifier of invalid type " + element.getClass().getSimpleName() + " - should be an object");
						}
					} else {
						logError(identifier, "targets no recipes");
					}
				} else {
					logError(identifier, "has an ill-formed additions tag of type " + element.getClass().getSimpleName());
				}
			} else {
				logError(identifier, "is missing additions list. Sorry but pushing nothing makes no sense O_O");
			}
		}
	}

	private void logError(Identifier identifier, String errorText) {
		PushToCraft.LOGGER.error("PushToCraft entry " + identifier + " " + errorText);
	}

	private void logWarn(Identifier identifier, String warnText) {
		PushToCraft.LOGGER.warn("PushToCraft entry " + identifier + " " + warnText);
	}

	public Stream<Entry> getMatches(Identifier recipeId, RecipeSerializer<?> recipeSerializer) {
		String recipeIdString = recipeId.toString();
		String path = recipeId.getPath();
		//noinspection UnstableApiUsage
		return Streams.concat(
				exactPushes.getOrDefault(recipeIdString, Collections.emptyList()).parallelStream(),
				namespacePushes.getOrDefault(recipeId.getNamespace(), Collections.emptyList()).parallelStream().filter(pair -> pair.getLeft().matcher(path).matches()).map(Pair::getRight),
				flexiblePushes.parallelStream().filter(pair -> pair.getLeft().matcher(recipeIdString).matches()).map(Pair::getRight),
				wildcardPushes.parallelStream()
		).filter(entry -> entry.recipeSerializerMatches(recipeSerializer));
	}

	public static class Entry {
		private final Collection<String> targets;
		private final Collection<String> additions;
		private final Collection<RecipeSerializer<?>> recipeSerializers;

		public Entry(Collection<String> targets, Collection<String> additions, Collection<RecipeSerializer<?>> recipeSerializers) {
			this.targets = targets;
			this.additions = additions;
			this.recipeSerializers = recipeSerializers;
		}

		public boolean recipeSerializerMatches(RecipeSerializer<?> recipeSerializer) {
			if (recipeSerializers.isEmpty())
				return true;
			return recipeSerializers.contains(recipeSerializer);
		}

		public Collection<String> getAdditions() {
			return additions;
		}

		public Collection<String> getTargets() {
			return targets;
		}
	}
}
