package com.github.stars_sea.enderport.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class EffectHelper {
    public static void addParticles(@NotNull World world, Vec3d pos, ParticleEffect effect) {
        Random random = world.random;
        for(int f = 0; f < 32; ++f)
            world.addParticle(effect, pos.x, pos.y, pos.z, random.nextGaussian(), random.nextGaussian(), random.nextGaussian());
    }

    public static void addTpParticles(World world, Vec3d pos) {
        addParticles(world, pos, ParticleTypes.PORTAL);
    }

    @NotNull
    public static EnderPearlEntity addEnderPearl(World world, LivingEntity user, @NotNull Vec3d pos) {
        EnderPearlEntity enderPearl = new EnderPearlEntity(world, user);
        enderPearl.setNoGravity(true);
        enderPearl.addScoreboardTag("enderport.effect");
        enderPearl.setPos(pos.x, pos.y, pos.z);

        world.spawnEntity(enderPearl);
        return enderPearl;
    }

    public static void killEffectEnderPearl(@NotNull World world, @NotNull BlockPos pos) {
        BlockPos pos1 = pos.add(-50, -50, -50);
        BlockPos pos2 = pos.add(50,  50,  50);

        Box searchBox = new Box(pos1, pos2);
        List<EnderPearlEntity> enderPearls = world.getEntitiesByClass(
                EnderPearlEntity.class, searchBox,
                enderPearl -> enderPearl.removeScoreboardTag("enderport.effect")
        );
        for (EnderPearlEntity enderPearl : enderPearls) {
            enderPearl.kill();
        }
    }
}
