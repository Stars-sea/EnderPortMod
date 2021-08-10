package com.github.stars_sea.enderport.item;

import com.github.stars_sea.enderport.util.EffectHelper;
import com.github.stars_sea.enderport.util.ItemHelper;
import com.github.stars_sea.enderport.world.Location;
import com.github.stars_sea.enderport.world.poi.LandmarkPOIType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;

public class EnderAmethyst extends Item {
    public final int searchDistance;

    public EnderAmethyst(int searchDistance) {
        super(new Settings().maxCount(16).group(ItemGroup.TRANSPORTATION));
        this.searchDistance = searchDistance;
    }

    public EnderAmethyst() {
        this(1024);
    }

    @Override
    public TypedActionResult<ItemStack> use(@NotNull World world, @NotNull PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        stack.decrement(1);
        EffectHelper.playBrokenSound(world, user.getBlockPos());
        EffectHelper.addTpParticles(world, user.getPos());

        if (world instanceof ServerWorld serverWorld) {
            user.getItemCooldownManager().set(this, 200);
            user.incrementStat(Stats.USED.getOrCreateStat(this));

            // TODO: Need to fix.
            // We don't use Future because we can load poi in this case.
            LandmarkPOIType.getNearestLandmark(serverWorld, user.getBlockPos(), searchDistance).ifPresentOrElse(
                    landmark -> {
                        prepareToTeleport(new Location(world.getRegistryKey(), landmark), user);
                        user.setGlowing(true);
                    },
                    () -> {
                        MutableText text = new TranslatableText("tip.enderport.not_fount_landmark", searchDistance);
                        user.sendSystemMessage(text.formatted(Formatting.RED), Util.NIL_UUID);

                        // If not found landmark, give a new amethyst back to the player.
                        ItemHelper.tryGiveToPlayer(user, new ItemStack(this));
                    }
            );
        }
        return TypedActionResult.consume(stack);
    }

    public static void prepareToTeleport(@NotNull Location location, @NotNull PlayerEntity user) {
        double      distance  = Vec3d.of(user.getBlockPos()).distanceTo(location.pos());
        MutableText text      = new TranslatableText("tip.enderport.found_landmark", Math.round(distance));
        user.sendMessage(text.formatted(Formatting.GREEN), true);

        new Timer().schedule(new TimerTask() {
            @Override public void run() {
                user.setGlowing(false);
                location.teleportToNearbySafely(4, user);
            }
        }, (long) (Math.ceil(distance / 100) * 1000)); // Add 1 sec for every 100 blocks.
    }
}
