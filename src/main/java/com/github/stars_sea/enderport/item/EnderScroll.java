package com.github.stars_sea.enderport.item;

import com.github.stars_sea.enderport.util.EffectHelper;
import com.github.stars_sea.enderport.world.Location;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EnderScroll extends Item {
    public EnderScroll() {
        super(new Settings().maxCount(1).group(ItemGroup.TRANSPORTATION));
    }

    // Override Methods
    @Override
    public boolean hasGlint(ItemStack stack) {
        return hasRecorded(stack);
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return hasRecorded(stack) ? 20 : 40;
    }

    @Override
    public boolean isUsedOnRelease(@NotNull ItemStack stack) {
        return stack.isOf(this) ;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, @NotNull PlayerEntity user, Hand hand) {
        ItemStack stack    = user.getStackInHand(hand);
        Location  location = getData(stack);
        if (location != null) {
            Vec3d    effectPos = location.pos().add(0, 1, 0);
            BlockPos blockPos  = new BlockPos(effectPos);
            if (!world.isAir(blockPos))
                effectPos = location.pos();
            if (world.isAir(blockPos)) {
                EffectHelper.addEnderPearl(world, user, effectPos);
                EffectHelper.addTpParticles(world, effectPos);
            }
        }

        user.setCurrentHand(hand);
        return TypedActionResult.consume(stack);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, @NotNull World world, @NotNull LivingEntity user, int remainingUseTicks) {
        int i = getMaxUseTime(stack) - remainingUseTicks;
        float progress = i / (float) getMaxUseTime(stack);

        EffectHelper.killEffectEnderPearl(world, user.getBlockPos());
        if (progress >= 1 && user instanceof PlayerEntity player) {
            Location location = getData(stack);

            if (location == null)
                recordPos(stack, world, player);
            else {
                if (world.isClient || location.teleport(player))
                    teleportSucceed(player, world, location);
                else teleportFail(player, location);

                EffectHelper.killEffectEnderPearl(world, new BlockPos(location.pos()));
            }
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        Location location = getData(stack);
        if (location != null) {
            LiteralText worldText = new LiteralText(location.getDimensionName());
            if (world == null || !world.getRegistryKey().getValue().equals(location.dimension().getValue()))
                worldText.formatted(Formatting.YELLOW);
            else worldText.formatted(Formatting.RED).formatted(Formatting.ITALIC);

            MutableText posText = new LiteralText(location.toString(false)).formatted(Formatting.RED);

            tooltip.add(worldText);
            tooltip.add(posText);
        } else {
            tooltip.add(new TranslatableText("tooltip.enderport.ender_scroll.blank"));
        }

        super.appendTooltip(stack, world, tooltip, context);
    }

    // Tool Methods
    public static boolean hasRecorded(@NotNull ItemStack stack) {
        return Location.valid(stack.getOrCreateSubNbt("location"));
    }

    private void record(@NotNull ItemStack stack, @NotNull RegistryKey<World> world, Vec3d pos) {
        Location location = new Location(world, pos);
        stack.getOrCreateNbt().put("location", location.getNbt());
    }

    private void record(@NotNull ItemStack stack, @NotNull PlayerEntity player) {
        record(stack, player.world.getRegistryKey(), player.getPos());
    }

    @Nullable
    private Location getData(@NotNull ItemStack stack) {
        return Location.deserialize(stack.getOrCreateSubNbt("location"));
    }

    // Wrapper Methods
    private void recordPos(ItemStack stack, @NotNull World world, PlayerEntity player) {
        record(stack, player);

        player.incrementStat(Stats.USED.getOrCreateStat(this));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100));
        player.getHungerManager().add(-2, 0.2f);
        player.getItemCooldownManager().set(this, 60);

        world.playSound(null, player.getBlockPos(), SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.PLAYERS,
                1F, 1F / (world.getRandom().nextFloat() * 0.5F + 1.0F) + 0.2F);
    }

    private void teleportSucceed(@NotNull PlayerEntity player, @NotNull World world, @NotNull Location location) {
        Text text = new TranslatableText("tip.enderport.tp_succeed", location).formatted(Formatting.GREEN);
        player.sendMessage(text, true);
        player.getItemCooldownManager().set(this, 30);
        player.incrementStat(Stats.USED.getOrCreateStat(this));

        // Effect
        EffectHelper.addTpParticles(world, player.getPos());
        EffectHelper.playTpSound(world, player.getBlockPos());
    }

    private void teleportFail(@NotNull PlayerEntity player, Location location) {
        Text text = new TranslatableText("tip.enderport.tp_fail", location).formatted(Formatting.RED);
        player.sendSystemMessage(text, Util.NIL_UUID);
    }
}
