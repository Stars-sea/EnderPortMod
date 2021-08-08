package com.github.stars_sea.enderport.event.listener;

import com.github.stars_sea.enderport.world.poi.LandmarkPOIType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ConduitBlockEntity;
import net.minecraft.server.world.ServerWorld;

public class BlockEntityEventsHandler
        implements ServerBlockEntityEvents.Load, ServerBlockEntityEvents.Unload {

    @Override
    public void onLoad(BlockEntity blockEntity, ServerWorld world) {
        if (blockEntity instanceof BeaconBlockEntity || blockEntity instanceof ConduitBlockEntity) {
            world.getPointOfInterestStorage().add(blockEntity.getPos(), LandmarkPOIType.Landmark);
        }
    }

    @Override
    public void onUnload(BlockEntity blockEntity, ServerWorld world) {
        if (blockEntity instanceof BeaconBlockEntity || blockEntity instanceof ConduitBlockEntity) {
            world.getPointOfInterestStorage().remove(blockEntity.getPos());
        }
    }

    public static void register() {
        BlockEntityEventsHandler handler = new BlockEntityEventsHandler();
        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register(handler);
        ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register(handler);
    }
}
