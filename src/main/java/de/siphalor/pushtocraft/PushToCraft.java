package de.siphalor.pushtocraft;

import net.fabricmc.api.ModInitializer;

import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PushToCraft implements ModInitializer {

    public static final Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "pushtocraft";
    public static final String MOD_NAME = "Push To Craft";

    public static Identifier currentRecipeId;
    public static RecipeSerializer<?> currentRecipeSerializer;

    @Override
    public void onInitialize() {
        log(Level.INFO, "Initializing");
        //TODO: Initializer
    }

    public static void log(Level level, String message){
        LOGGER.log(level, "["+MOD_NAME+"] " + message);
    }

}
