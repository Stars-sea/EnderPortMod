package com.github.stars_sea.enderport.mixin.accessor;

import net.minecraft.block.BlockState;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Set;

@Mixin(PointOfInterestTypes.class)
public interface PointOfInterestTypesAccessor {
    @Invoker("register")
    static PointOfInterestType register(Registry<PointOfInterestType> registry, RegistryKey<PointOfInterestType> key, Set<BlockState> states, int ticketCount, int searchDistance) {
        throw new AssertionError();
    }
}
