package com.github.stars_sea.enderport.loot;

import com.github.stars_sea.enderport.item.EnderPortItems;
import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.FabricLootSupplierBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.entity.EntityType;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class EndermanLootTable implements LootTableLoadingCallback {
    private static final Identifier EndermanLootTableID = EntityType.ENDERMAN.getLootTableId();

    @Override
    public void onLootTableLoading(ResourceManager resourceManager, LootManager manager, Identifier id, FabricLootSupplierBuilder supplier, LootTableSetter setter) {
        if (EndermanLootTableID.equals(id)) {
            FabricLootPoolBuilder builder = FabricLootPoolBuilder.builder()
                    .rolls(UniformLootNumberProvider.create(0, 2))
                    .with(ItemEntry.builder(EnderPortItems.ENDER_PEARL_FRAGMENT));
            supplier.pool(builder);
        }
    }
}
