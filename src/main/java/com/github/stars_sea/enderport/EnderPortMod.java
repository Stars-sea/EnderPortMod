package com.github.stars_sea.enderport;

import com.github.stars_sea.enderport.item.EnderPortItems;
import com.github.stars_sea.enderport.loot.EndermanLootTable;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;

public class EnderPortMod implements ModInitializer {
	@Override
	public void onInitialize() {
		LootTableLoadingCallback.EVENT.register(new EndermanLootTable());
		EnderPortItems.registerAll();
	}
}
