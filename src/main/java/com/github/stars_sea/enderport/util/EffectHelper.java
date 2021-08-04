package com.github.stars_sea.enderport.util;

import com.github.stars_sea.enderport.Location;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class EffectHelper {
    public static void addTpParticles(World world, double x, double y, double z) {
        for(int f = 0; f < 32; ++f) {
            world.addParticle(ParticleTypes.PORTAL, x, y, z,
                    world.random.nextGaussian(), world.random.nextGaussian(), world.random.nextGaussian());
        }
    }

    public static void addTpParticles(World world, @NotNull Vec3d pos) {
        addTpParticles(world, pos.getX(), pos.getY(), pos.getZ());
    }

    @NotNull
    public static EnderPearlEntity addEnderPearl(World world, LivingEntity user, double x, double y, double z) {
        EnderPearlEntity enderPearl = new EnderPearlEntity(world, user);
        enderPearl.setNoGravity(true);
        enderPearl.addScoreboardTag("enderport.effect");
        enderPearl.setPos(x, y, z);

        world.spawnEntity(enderPearl);
        return enderPearl;
    }

    @NotNull
    public static EnderPearlEntity addEnderPearl(World world, LivingEntity user, @NotNull Vec3d pos) {
        return addEnderPearl(world, user, pos.getX(), pos.getY(), pos.getZ());
    }

    public static void playTpSound(@NotNull World world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.NEUTRAL,
                0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
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
