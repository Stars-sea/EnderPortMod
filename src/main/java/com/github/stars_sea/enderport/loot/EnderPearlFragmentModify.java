package com.github.stars_sea.enderport.loot;

import com.github.stars_sea.enderport.item.EnderPortItems;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.loot.v2.LootTableSource;
import net.minecraft.entity.EntityType;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class EnderPearlFragmentModify implements LootTableEvents.Modify {
    private static final Identifier ENDERMAN_LOOTTABLE_ID = EntityType.ENDERMAN.getLootTableId();

    @Override
    public void modifyLootTable(ResourceManager resourceManager, LootManager lootManager, Identifier identifier, LootTable.Builder builder, LootTableSource lootTableSource) {
        if (lootTableSource.isBuiltin() && ENDERMAN_LOOTTABLE_ID.equals(identifier)) {
            LootPool.Builder poolBuilder = LootPool.builder()
                    .rolls(UniformLootNumberProvider.create(0, 2))
                    .with(ItemEntry.builder(EnderPortItems.ENDER_PEARL_FRAGMENT));
            builder.pool(poolBuilder);
        }
    }
}
