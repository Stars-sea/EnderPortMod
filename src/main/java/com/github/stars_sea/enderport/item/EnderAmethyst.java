package com.github.stars_sea.enderport.item;

import com.github.stars_sea.enderport.sound.SoundShortcut;
import com.github.stars_sea.enderport.util.EffectHelper;
import com.github.stars_sea.enderport.util.ItemHelper;
import com.github.stars_sea.enderport.world.Location;
import com.github.stars_sea.enderport.world.poi.LandmarkPOIType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

public class EnderAmethyst extends Item {
    public final int searchDistance;
    public final int maxTpCount;

    public EnderAmethyst(int searchDistance, int maxTpCount) {
        super(new Settings().maxCount(16));
        ItemHelper.addToGroup(this, ItemGroups.TOOLS, ItemGroups.INGREDIENTS);

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
            user.getItemCooldownManager().set(this, 30);
            user.incrementStat(Stats.USED.getOrCreateStat(this));

            if (user.isSneaking()) {
                GlobalPos lastDeathPos = user.getLastDeathPos().orElse(null);
                if (lastDeathPos != null) {
                    prepareToTeleportAround(
                            new Location(lastDeathPos), user,
                            d -> Text.translatable("tip.enderport.tp_last_death_pos").formatted(Formatting.GREEN)
                    );
                    return TypedActionResult.consume(stack);
                }
            }

            LandmarkPOIType.getNearestLandmark(serverWorld, user.getBlockPos(), searchDistance).ifPresentOrElse(
                    landmark -> prepareToTeleportAround(
                            new Location(world, landmark), user,
                            d -> Text.translatable("tip.enderport.found_landmark", d).formatted(Formatting.GREEN)
                    ),
                    () -> {
                        MutableText text = Text.translatable("tip.enderport.not_fount_landmark", searchDistance);
                        user.sendMessage(text.formatted(Formatting.RED), true);

                        // If not found landmark, give a new amethyst back to the player.
                        user.getInventory().offerOrDrop(new ItemStack(this));
                    }
            );
        }
        return TypedActionResult.consume(stack);
    }

    protected void prepareToTeleportAround(@NotNull Location location, @NotNull PlayerEntity player, Function<Long, Text> tip) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        Location pos = new Location(player);

        var sources  = maxTpCount == 1 ? List.of(player) : pos.getLivingEntitiesAround(server, 10);
        var entities = sources.subList(0, Math.min(sources.size(), maxTpCount));

        double distance = pos.pos().distanceTo(location.pos());
        for (LivingEntity entity : entities) {
            if (entity instanceof PlayerEntity playerEntity)
                playerEntity.sendMessage(tip.apply(Math.round(distance)), true);

            entity.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.GLOWING, 40, 0, false, false
            ));
        }

        for (LivingEntity entity : entities) {
            Location nearby = location.teleportToNearbySafely(3, entity, true, 10);
            if (nearby != null) entity.refreshPositionAfterTeleport(nearby.pos());
        }
    }
}
