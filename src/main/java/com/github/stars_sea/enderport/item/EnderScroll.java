package com.github.stars_sea.enderport.item;

import com.github.stars_sea.enderport.network.server.SendToClient;
import com.github.stars_sea.enderport.sound.SoundShortcut;
import com.github.stars_sea.enderport.util.EffectHelper;
import com.github.stars_sea.enderport.util.ItemHelper;
import com.github.stars_sea.enderport.world.Location;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

public class EnderScroll extends LocationRecordable {
    public EnderScroll() {
        super(new Settings().maxCount(16));
        ItemHelper.addToGroup(this, ItemGroups.TOOLS, ItemGroups.INGREDIENTS);
    }

    // Override Methods
    @Override
    public boolean hasGlint(ItemStack stack) {
        return hasRecorded(stack);
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return hasRecorded(stack) ? 30 : 60;
    }

    @Override
    public boolean isUsedOnRelease(@NotNull ItemStack stack) {
        return stack.isOf(this) ;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, @NotNull PlayerEntity user, Hand hand) {
        ItemStack stack    = user.getStackInHand(hand);
        Location  location = getLocation(stack);
        if (location != null)
            EffectHelper.addTpParticles(world, location.pos().add(0, 1, 0));

        user.setCurrentHand(hand);
        return TypedActionResult.success(stack);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, @NotNull World world, @NotNull LivingEntity user, int remainingUseTicks) {
        float progress = ItemHelper.getUseProgress(getMaxUseTime(stack) - remainingUseTicks, stack);

        if (progress >= 1 && user instanceof ServerPlayerEntity player) {
            Location location = getLocation(stack);

            if (location == null) {
                if (!world.isClient) SendToClient.sendOpenRenameScreen(player);
            } else {
                if (player.isSneaking())
                    clearPos(player, stack);
                else if (world.isClient || location.teleport(player, true))
                    teleportSucceed(player, world, location);
                else teleportFail(player, location);
            }
        }
    }

    @Override
    public ActionResult useOnBlock(@NotNull ItemUsageContext context) {
        World        world  = context.getWorld();
        BlockState   state  = world.getBlockState(context.getBlockPos());
        ItemStack    total  = context.getStack();
        PlayerEntity player = context.getPlayer();
        if (player == null) return ActionResult.FAIL;

        // 如果被右键方块是含水炼药锅 且 卷轴有记录, 则消除一个卷轴的记录, 去掉一格水
        if (hasRecorded(total) && state.isOf(Blocks.WATER_CAULDRON)) {
            total.decrement(1);
            player.getInventory().offerOrDrop(new ItemStack(this));
            SoundShortcut.SPLASH.play(player);
            return ActionResult.SUCCESS;
        }
        // 如果被右键方块是含岩浆炼药锅, 则消毁卷轴
        if (state.isOf(Blocks.LAVA_CAULDRON)) {
            total.decrement(1);
            SoundShortcut.BURNING.play(player);
            return ActionResult.CONSUME;
        }
        return ActionResult.PASS;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);

        Location location = getLocation(stack);
        if (location != null) {
            MutableText worldText = Text.literal(location.getDimensionName());
            Text        posText   = Text.literal(location.toString(false)).formatted(Formatting.GREEN);
            MutableText distance  = null;
            if (location.isSameWorld(world)) {
                worldText.formatted(Formatting.GRAY, Formatting.ITALIC);
                if (world.isClient && tryGetClientPlayer() instanceof PlayerEntity player) {
                    double d = player.getPos().distanceTo(location.pos());
                    distance = Text.translatable("tooltip.enderport.ender_scroll.distance", Math.round(d));
                    if (d > 20) distance.formatted(Formatting.GREEN);
                    else distance.formatted(Formatting.GRAY);
                }
            } else worldText.formatted(Formatting.YELLOW);

            tooltip.add(worldText);
            tooltip.add(posText);
            if (distance != null) tooltip.add(distance);
        } else {
            tooltip.add(Text.translatable("tooltip.enderport.ender_scroll.blank").formatted(Formatting.GRAY));
        }
    }

    // Wrapper Methods
    public void recordPos(@NotNull PlayerEntity player, Location location, @Nullable String name) {
        ItemStack       newStack  = genStackWithLocation(1, location);
        PlayerInventory inventory = player.getInventory();
        if (name != null) newStack.setCustomName(Text.literal(name));

        if (player.isCreative())
            inventory.offerOrDrop(newStack);
        else findBlankEnderScroll(inventory).ifPresent(blank -> {
            blank.decrement(1);
            inventory.offerOrDrop(newStack);

            player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 2));
            player.getHungerManager().add(-2, 0.2f);
        });
        player.incrementStat(Stats.USED.getOrCreateStat(this));
        player.getItemCooldownManager().set(this, 60);

        SoundShortcut.PAGE_TURN.play(player);
    }

    private void clearPos(@NotNull PlayerEntity player, @NotNull ItemStack stack) {
        clearLocation(stack);
        stack.removeCustomName();

        player.sendMessage(Text.translatable("tip.enderport.clear_location").formatted(Formatting.GREEN), true);
        player.getItemCooldownManager().set(this, 30);
        player.incrementStat(Stats.USED.getOrCreateStat(this));
        if (!player.isCreative()) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 2));
            player.getHungerManager().add(-2, 0.2f);
        }

        SoundShortcut.PAGE_TURN.play(player);
    }

    private void teleportSucceed(@NotNull PlayerEntity player, @NotNull World world, @NotNull Location location) {
        player.sendMessage(Text.translatable("tip.enderport.tp_succeed", location).formatted(Formatting.GREEN), true);
        player.getItemCooldownManager().set(this, 30);
        player.incrementStat(Stats.USED.getOrCreateStat(this));
        if (!player.isCreative()) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 30, 0, false, false));
        }

        // Effect
        EffectHelper.addTpParticles(world, player.getPos());
        SoundShortcut.TELEPORT.play(player);
    }

    private void teleportFail(@NotNull PlayerEntity player, Location location) {
        player.sendMessage(
                Text.translatable("tip.enderport.tp_fail", location).formatted(Formatting.RED),
                false
        );
    }

    private Optional<ItemStack> findBlankEnderScroll(@NotNull Inventory inventory) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isOf(this) && !hasRecorded(stack))
                return Optional.of(stack);
        }
        return Optional.empty();
    }

    /**
     * Get a {@link net.minecraft.client.network.ClientPlayerEntity} instance in current minecraft client.
     * The reason why use reflection is to be compatible with the server.
     * @return An instance of {@code ClientPlayerEntity} in client side. {@code null} in server side.
     */
    @Nullable
    private static LivingEntity tryGetClientPlayer() {
        try {
            Class<?> minecraftClient   = Class.forName("net.minecraft.client.MinecraftClient");
            Field    playerField       = minecraftClient.getField("player");
            Method   getInstanceMethod = minecraftClient.getMethod("getInstance");
            return (PlayerEntity) playerField.get(getInstanceMethod.invoke(null));
        }
        catch (ClassNotFoundException | NoSuchFieldException | NoSuchMethodException |
               IllegalAccessException | InvocationTargetException ignored) { }
        return null;
    }
}
