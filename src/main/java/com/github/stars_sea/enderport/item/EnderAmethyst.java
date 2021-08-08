package com.github.stars_sea.enderport.item;

import com.github.stars_sea.enderport.util.EffectHelper;
import com.github.stars_sea.enderport.world.Location;
import com.github.stars_sea.enderport.world.poi.LandmarkPOIType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
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
        super(new Settings().maxCount(1).group(ItemGroup.TRANSPORTATION));
        this.searchDistance = searchDistance;
    }

    public EnderAmethyst() {
        this(2048);
    }

    @Override
    public TypedActionResult<ItemStack> use(@NotNull World world, @NotNull PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (world instanceof ServerWorld serverWorld) {
            user.getItemCooldownManager().set(this, 200);

            // TODO: Need to fix.
            // We don't use Future because we can load poi in this case.
//            CompletableFuture.supplyAsync(
//                    () -> LandmarkPOIType.getNearestLandmark(serverWorld, user.getBlockPos(), searchDistance)
//            ).thenAccept(pos -> pos.ifPresentOrElse(
//                    landmark -> {
//                        prepareToTeleport(new Location(world, landmark).add(0, 1, 0), user);
//                        user.setGlowing(true);
//                        stack.decrement(1);
//                        EffectHelper.playBrokenSound(world, user.getBlockPos());
//                    },
//                    () -> {
//                        MutableText text = new TranslatableText("tip.enderport.not_fount_landmark", searchDistance);
//                        user.sendSystemMessage(text.formatted(Formatting.RED), Util.NIL_UUID);
//                    }
//            ));
            LandmarkPOIType.getNearestLandmark(serverWorld, user.getBlockPos(), searchDistance).ifPresentOrElse(
                    landmark -> {
                        prepareToTeleport(new Location(world, landmark).add(0, 1, 0), user);
                        user.setGlowing(true);
                        stack.decrement(1);
                        EffectHelper.playBrokenSound(world, user.getBlockPos());
                    },
                    () -> {
                        MutableText text = new TranslatableText("tip.enderport.not_fount_landmark", searchDistance);
                        user.sendSystemMessage(text.formatted(Formatting.RED), Util.NIL_UUID);
                    }
            );
        }
        return TypedActionResult.consume(stack);
    }

    public static void prepareToTeleport(@NotNull Location location, @NotNull PlayerEntity user) {
        double distance  = Vec3d.of(user.getBlockPos()).distanceTo(location.pos());
        MutableText text = new TranslatableText("tip.enderport.found_landmark", Math.round(distance));
        user.sendMessage(text.formatted(Formatting.GREEN), true);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                user.setGlowing(false);
                location.teleport(user);
            }
        }, 3000);
    }
}
