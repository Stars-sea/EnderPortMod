package com.github.stars_sea.enderport.sound;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.function.Supplier;

public record SoundShortcut(SoundEvent sound, SoundCategory category, Supplier<Float> volume, Supplier<Float> pitch) {
    private static final Random rand = new Random();

    public static final SoundShortcut TELEPORT  = new SoundShortcut(SoundEvents.ENTITY_ENDERMAN_TELEPORT);
    public static final SoundShortcut BURNING   = new SoundShortcut(SoundEvents.ENTITY_GENERIC_BURN, 0.4f, () -> 2f + rand.nextFloat() * 0.4f);
    public static final SoundShortcut SPLASH    = new SoundShortcut(SoundEvents.ENTITY_PLAYER_SPLASH, 1f, () -> 1f + (rand.nextFloat() - rand.nextFloat()) * 0.4f);
    public static final SoundShortcut PAGE_TURN = new SoundShortcut(SoundEvents.ITEM_BOOK_PAGE_TURN, 0.3f, 1f);
    public static final SoundShortcut AMETHYST_BREAK = new SoundShortcut(SoundEvents.BLOCK_LARGE_AMETHYST_BUD_BREAK);

    public SoundShortcut(SoundEvent sound, Supplier<Float> volume, Supplier<Float> pitch) {
        this(sound, SoundCategory.PLAYERS, volume, pitch);
    }

    public SoundShortcut(SoundEvent sound, float volume, Supplier<Float> pitch) {
        this(sound, () -> volume, pitch);
    }

    public SoundShortcut(SoundEvent sound, float volume, float pitch) {
        this(sound, () -> volume, () -> pitch);
    }

    public SoundShortcut(SoundEvent sound) {
        this(sound, 1, 1);
    }

    public void play(@NotNull PlayerEntity player) {
        player.world.playSound(null, player.getBlockPos(), sound, category, volume.get(), pitch.get());
    }
}
