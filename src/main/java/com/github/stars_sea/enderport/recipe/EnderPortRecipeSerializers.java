package com.github.stars_sea.enderport.recipe;

import com.github.stars_sea.enderport.EnderPortMod;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class EnderPortRecipeSerializers {
    public static SpecialRecipeSerializer<LocationCloningRecipe> LOCATION_CLONING =
            new SpecialRecipeSerializer<>(LocationCloningRecipe::new);

    private static void register(String path, RecipeSerializer<?> serializer) {
        Registry.register(Registries.RECIPE_SERIALIZER, EnderPortMod.genId(path), serializer);
    }

    public static void registerAll() {
        register("special_crafting_location_cloning", LOCATION_CLONING);
    }
}
