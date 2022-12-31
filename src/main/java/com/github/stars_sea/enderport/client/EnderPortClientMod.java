package com.github.stars_sea.enderport.client;

import com.github.stars_sea.enderport.item.EnderPortItems;
import com.github.stars_sea.enderport.item.EnderScroll;
import com.github.stars_sea.enderport.network.client.listener.EnderPortClientListeners;
import com.github.stars_sea.enderport.util.ItemHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class EnderPortClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Predicate Provider Register
        ModelPredicateProviderRegistry.register(
                EnderPortItems.ENDER_SCROLL,
                new Identifier("progress"),
                (stack, world, entity, seed) -> {
                    if (entity != null && entity.getActiveItem() == stack)
                        return ItemHelper.getUseProgress(entity.getItemUseTime(), stack);
                    return 0F;
                }
        );
        ModelPredicateProviderRegistry.register(
                EnderPortItems.ENDER_SCROLL,
                new Identifier("recorded"),
                (stack, world, entity, seed) -> EnderScroll.hasRecorded(stack) ? 1F : 0F
        );

        EnderPortClientListeners.register();
    }
}
