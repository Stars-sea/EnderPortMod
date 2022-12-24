package com.github.stars_sea.enderport.loot;

import net.fabricmc.fabric.api.loot.v2.LootTableEvents;

public class EnderPortLootTableManager {
    public static void register() {
        LootTableEvents.MODIFY.register(new EnderPearlFragmentModify());
    }
}
