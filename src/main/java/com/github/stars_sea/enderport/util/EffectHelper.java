package com.github.stars_sea.enderport.util;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public final class EffectHelper {
    public static void addParticles(@NotNull World world, Vec3d pos, ParticleEffect effect) {
        Random random = world.random;
        for(int f = 0; f < 32; ++f)
            world.addParticle(effect, pos.x, pos.y, pos.z, random.nextGaussian(), random.nextGaussian(), random.nextGaussian());
    }

    public static void addTpParticles(World world, Vec3d pos) {
        addParticles(world, pos, ParticleTypes.PORTAL);
    }
}
