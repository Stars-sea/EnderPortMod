package com.github.stars_sea.enderport.item;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class EnderPortItems {
    public static final EnderScroll ENDER_SCROLL = new EnderScroll();
    public static final EnderPearlFragment ENDER_PEARL_FRAGMENT = new EnderPearlFragment();

    public static void register(String path, Item item) {
        Registry.register(Registry.ITEM, new Identifier("enderport", path), item);
    }

    public static void registerAll() {
        register("ender_scroll", ENDER_SCROLL);
        register("ender_pearl_fragment", ENDER_PEARL_FRAGMENT);
    }
}
