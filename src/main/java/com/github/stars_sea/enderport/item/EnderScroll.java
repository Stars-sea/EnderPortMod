package com.github.stars_sea.enderport.item;

import com.github.stars_sea.enderport.util.EffectHelper;
import com.github.stars_sea.enderport.util.ItemHelper;
import com.github.stars_sea.enderport.world.Location;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EnderScroll extends LocationRecordable {
    public EnderScroll() {
        super(new Settings().maxCount(16).group(ItemGroup.TRANSPORTATION));
    }

    // Override Methods
    @Override
    public boolean hasGlint(ItemStack stack) {
        return hasRecorded(stack);
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        Location location = getLocation(stack);
        Entity   entity   = stack.getHolder();
        if (location != null && entity != null) {
            double distance = location.pos().distanceTo(entity.getPos());
            return (int) (Math.ceil(distance / 100)) * 30; // Add 1.5 sec for every 100 blocks.
        }
        return 40;
    }

    @Override
    public boolean isUsedOnRelease(@NotNull ItemStack stack) {
        return stack.isOf(this) ;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, @NotNull PlayerEntity user, Hand hand) {
        ItemStack stack    = user.getStackInHand(hand);
        Location  location = getLocation(stack);
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
            Location location = getLocation(stack);

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
    public ActionResult useOnBlock(@NotNull ItemUsageContext context) {
        World      world = context.getWorld();
        BlockPos   pos   = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        ItemStack  stack = context.getStack();
        // 如果被右键方块是含水炼药锅 且 卷轴有记录, 则消除卷轴记录, 去掉一格水
        if (hasRecorded(stack) && state.isOf(Blocks.WATER_CAULDRON)) {
            clearLocation(stack);
            LeveledCauldronBlock.decrementFluidLevel(state, world, pos);
            return ActionResult.SUCCESS;
        }
        // 如果被右键方块是含岩浆炼药锅, 则消毁卷轴
        if (state.isOf(Blocks.LAVA_CAULDRON)) {
            stack.decrement(1);
            return ActionResult.CONSUME;
        }
        return ActionResult.PASS;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        Location location = getLocation(stack);
        if (location != null) {
            LiteralText worldText = new LiteralText(location.getDimensionName());
            if (world == null || !world.getRegistryKey().getValue().equals(location.dimension().getValue()))
                worldText.formatted(Formatting.YELLOW);
            else worldText.formatted(Formatting.GRAY).formatted(Formatting.ITALIC);

            MutableText posText = new LiteralText(location.toString(false)).formatted(Formatting.GREEN);

            tooltip.add(worldText);
            tooltip.add(posText);
        } else {
            tooltip.add(new TranslatableText("tooltip.enderport.ender_scroll.blank").formatted(Formatting.GRAY));
        }

        super.appendTooltip(stack, world, tooltip, context);
    }

    // Wrapper Methods
    private void recordPos(@NotNull ItemStack stack, @NotNull World world, @NotNull PlayerEntity player) {
        ItemStack newStack = new ItemStack(this);
        recordLocation(newStack, player.world.getRegistryKey(), player.getPos());

        stack.decrement(1);
        ItemHelper.tryGiveToPlayer(player, newStack);

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
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 30));

        // Effect
        EffectHelper.addTpParticles(world, player.getPos());
        EffectHelper.playTpSound(world, player.getBlockPos());
    }

    private void teleportFail(@NotNull PlayerEntity player, Location location) {
        Text text = new TranslatableText("tip.enderport.tp_fail", location).formatted(Formatting.RED);
        player.sendSystemMessage(text, Util.NIL_UUID);
    }
}
