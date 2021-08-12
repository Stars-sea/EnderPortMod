package com.github.stars_sea.enderport.recipe;

import com.github.stars_sea.enderport.item.EnderPortItems;
import com.github.stars_sea.enderport.item.LocationRecordable;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.MapCloningRecipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Copy {@link EnderPortItems#ENDER_SCROLL}
 * Reference: {@link MapCloningRecipe}
 */
public class LocationCloningRecipe extends SpecialCraftingRecipe {
    public LocationCloningRecipe(Identifier id) {
        super(id);
    }

    @Override
    public boolean matches(@NotNull CraftingInventory inventory, World world) {
        int empties = 0;
        ItemStack recorded = ItemStack.EMPTY;

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack curr = inventory.getStack(i);
            if (curr.isEmpty()) continue;

            if (!(curr.getItem() instanceof LocationRecordable))
                return false;

            if (LocationRecordable.hasRecorded(curr)) {
                if (!recorded.isEmpty()) return false;
                recorded = curr;
            }
            else empties++;
        }
        return !recorded.isEmpty() && (empties > 0);
    }

    @Override
    public ItemStack craft(@NotNull CraftingInventory inventory) {
        int empties = 0;
        ItemStack recorded = ItemStack.EMPTY;

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack curr = inventory.getStack(i);
            if (curr.isEmpty()) continue;

            if (!(curr.getItem() instanceof LocationRecordable))
                return ItemStack.EMPTY;

            if (LocationRecordable.hasRecorded(curr)) {
                if (!recorded.isEmpty()) return ItemStack.EMPTY;
                recorded = curr;
            }
            else empties++;
        }
        if (recorded.isEmpty() || empties < 1)
            return ItemStack.EMPTY;

        ItemStack newStack = recorded.copy();
        newStack.setCount(empties + 1);
        return newStack;
    }

    @Override
    public boolean fits(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return EnderPortRecipeSerializers.LOCATION_CLONING;
    }
}
