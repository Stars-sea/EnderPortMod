package com.github.stars_sea.enderport.network;

import com.github.stars_sea.enderport.EnderPortMod;
import net.minecraft.util.Identifier;

public final class EnderPortChannels {
    public static final Identifier C2S_RENAME_ENDER_SCROLL = EnderPortMod.genId("network.c2s.rename_ender_scroll");
    public static final Identifier S2C_OPEN_RENAME_SCROLL  = EnderPortMod.genId("network.s2c.open_rename_screen");
}
