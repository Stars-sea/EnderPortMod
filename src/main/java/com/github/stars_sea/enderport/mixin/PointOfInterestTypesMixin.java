package com.github.stars_sea.enderport.mixin;

import com.github.stars_sea.enderport.mixin.accessor.PointOfInterestTypesAccessor;
import com.github.stars_sea.enderport.world.poi.LandmarkPOIType;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registry;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;
import java.util.stream.Collectors;

@Mixin(PointOfInterestTypes.class)
public class PointOfInterestTypesMixin {
    private static final Set<BlockState> LANDMARK_BLOCKS =
            ImmutableSet.of(Blocks.BEACON, Blocks.CONDUIT, Blocks.RESPAWN_ANCHOR)
                    .stream().flatMap(block -> block.getStateManager().getStates().stream())
                    .collect(Collectors.toSet());

    @Inject(method = "registerAndGetDefault(Lnet/minecraft/registry/Registry;)Lnet/minecraft/world/poi/PointOfInterestType;",
            at = @At("RETURN"))
    private static void registerAndGetDefault(Registry<PointOfInterestType> registry, CallbackInfoReturnable<PointOfInterestType> cir) {
        PointOfInterestTypesAccessor.register(registry, LandmarkPOIType.LANDMARK, LANDMARK_BLOCKS, 1, 1);
    }
}
