package com.github.stars_sea.enderport.item;

import com.github.stars_sea.enderport.sound.SoundShortcut;
import com.github.stars_sea.enderport.util.EffectHelper;
import com.github.stars_sea.enderport.world.Location;
import com.github.stars_sea.enderport.world.poi.LandmarkPOIType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class EnderAmethyst extends Item {
    public final int searchDistance;
    public final int maxTpCount;

    public EnderAmethyst(int searchDistance, int maxTpCount) {
        super(new Settings().maxCount(16).group(ItemGroup.TRANSPORTATION));
        this.searchDistance = searchDistance;
        this.maxTpCount     = maxTpCount;
    }

    public EnderAmethyst(int maxTpCount) {
        this(1024, maxTpCount);
    }

    @Override
    public TypedActionResult<ItemStack> use(@NotNull World world, @NotNull PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        stack.decrement(1);
        SoundShortcut.AMETHYST_BREAK.play(user);
        EffectHelper.addTpParticles(world, user.getPos());

        if (world instanceof ServerWorld serverWorld) {
            user.getItemCooldownManager().set(this, 200);
            user.incrementStat(Stats.USED.getOrCreateStat(this));

            // TODO: Need to fix.
            // We don't use Future because we can load poi in this case.
            LandmarkPOIType.getNearestLandmark(serverWorld, user.getBlockPos(), searchDistance).ifPresentOrElse(
                    landmark -> prepareToTeleportAround(new Location(world, landmark), user, maxTpCount),
                    () -> {
                        MutableText text = new TranslatableText("tip.enderport.not_fount_landmark", searchDistance);
                        user.sendSystemMessage(text.formatted(Formatting.RED), Util.NIL_UUID);

                        // If not found landmark, give a new amethyst back to the player.
                        user.getInventory().offerOrDrop(new ItemStack(this));
                    }
            );
        }
        return TypedActionResult.consume(stack);
    }

    protected static void prepareToTeleportAround(@NotNull Location location, @NotNull PlayerEntity player, int max) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        Location pos = new Location(player.world, player.getPos());

        var sources = (max == 1) ? List.of(player) : pos.getLivingEntitiesAround(server, 5);
        List<? extends LivingEntity> entities = sources.subList(0, Math.min(sources.size(), max));

        double distance = pos.pos().distanceTo(location.pos());
        double seconds  = Math.ceil(distance / 100);

        for (LivingEntity entity : entities) {
            if (entity instanceof PlayerEntity playerEntity) {
                MutableText text = new TranslatableText("tip.enderport.found_landmark", Math.round(distance));
                playerEntity.sendMessage(text.formatted(Formatting.GREEN), true);
            }

            entity.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.GLOWING, (int) (seconds * 20), 0, false, false
            ));
        }

        new Timer().schedule(new TimerTask() {
            @Override public void run() {
                for (LivingEntity entity : entities) {
                    Location nearby = location.teleportToNearbySafely(3, entity);
                    if (nearby != null) entity.refreshPositionAfterTeleport(nearby.pos());
                }
            }
        }, (long) (seconds * 1000)); // Add 1 sec for every 100 blocks.
    }
}
