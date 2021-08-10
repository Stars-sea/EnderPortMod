package com.github.stars_sea.enderport.item;

import com.github.stars_sea.enderport.EnderPortMod;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

public final class EnderPortItems {
    public static final EnderScroll ENDER_SCROLL = new EnderScroll();
    public static final EnderAmethyst ENDER_AMETHYST = new EnderAmethyst();
    public static final EnderPearlFragment ENDER_PEARL_FRAGMENT = new EnderPearlFragment();

    private static void register(String path, Item item) {
        Registry.register(Registry.ITEM, EnderPortMod.genId(path), item);
    }

    public static void registerAll() {
        register("ender_scroll", ENDER_SCROLL);
        register("ender_amethyst", ENDER_AMETHYST);
        register("ender_pearl_fragment", ENDER_PEARL_FRAGMENT);
    }
}
