package de.siphalor.pushtocraft.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class PushToCraftMixinConfig implements IMixinConfigPlugin {
	private static final String DataPackContents$remapped$slashy;
	private static final String DataPackContents$getContents$remapped;
	private static final String DataPackContents$recipeManager$remapped;
	private static final String RecipeManager$remapped$slashy;

	static {
		MappingResolver mappingResolver = FabricLoader.getInstance().getMappingResolver();
		DataPackContents$remapped$slashy = mappingResolver.mapClassName("intermediary", "net.minecraft.class_5350").replace('.', '/');
		DataPackContents$getContents$remapped = mappingResolver.mapMethodName("intermediary", "net.minecraft.class_5350", "method_40427", "()Ljava/util/List;");
		String RecipeManager$remapped = mappingResolver.mapClassName("intermediary", "net.minecraft.class_1863");
		RecipeManager$remapped$slashy = RecipeManager$remapped.replace('.', '/');
		DataPackContents$recipeManager$remapped = mappingResolver.mapFieldName("intermediary", "net.minecraft.class_5350", "field_25337", "Lnet/minecraft/class_1863;");
	}

	@Override
	public void onLoad(String mixinPackage) {

	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		if ("de.siphalor.pushtocraft.mixin.MixinDataPackContents".equals(mixinClassName)) {
			for (MethodNode method : targetClass.methods) {
				if (DataPackContents$getContents$remapped.equals(method.name)) {
					targetClass.methods.remove(method);
					method.accept(new GetContentsTransformer(
							targetClass.visitMethod(method.access, method.name, method.desc, method.signature, method.exceptions.toArray(new String[0])),
							method.access,
							method.name,
							method.desc
					));
					break;
				}
			}
		}
	}

	private static class GetContentsTransformer extends GeneratorAdapter {
		public GetContentsTransformer(MethodVisitor methodVisitor, int access, String name, String descriptor) {
			super(Opcodes.ASM9, methodVisitor, access, name, descriptor);
		}

		@Override
		public void visitInsn(int opcode) {
			if (opcode == Opcodes.ARETURN) {
				loadThis();
				visitFieldInsn(Opcodes.GETFIELD, DataPackContents$remapped$slashy, "pushToCraftManager", "Lde/siphalor/pushtocraft/PushToCraftManager;");
				loadThis();
				visitFieldInsn(Opcodes.GETFIELD, DataPackContents$remapped$slashy, DataPackContents$recipeManager$remapped, "L" + RecipeManager$remapped$slashy + ";");
				visitMethodInsn(Opcodes.INVOKESTATIC, "de/siphalor/pushtocraft/Util", "insertBeforeImmutable", "(Ljava/util/List;Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/List;", false);
			}
			super.visitInsn(opcode);
		}
	}
}
