package com.teyvat.elements.registry;

import java.util.function.Supplier;

import com.teyvat.elements.TeyvatElements;
import com.teyvat.elements.menu.WeaponImprintMenu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 容器菜单注册。
 */
public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, TeyvatElements.MOD_ID);

    public static final Supplier<MenuType<WeaponImprintMenu>> WEAPON_IMPRINT =
            MENUS.register("weapon_imprint",
                    () -> new MenuType<>(WeaponImprintMenu::new, FeatureFlags.VANILLA_SET));

    private ModMenus() {}
}
