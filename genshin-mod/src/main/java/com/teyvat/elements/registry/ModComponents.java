package com.teyvat.elements.registry;

import java.util.function.Supplier;

import com.teyvat.elements.TeyvatElements;
import com.teyvat.elements.weapon.WeaponData;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 自定义物品组件注册。
 */
public final class ModComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, TeyvatElements.MOD_ID);

    /** 武器养成数据（persistent = 跟随物品存档） */
    public static final Supplier<DataComponentType<WeaponData>> WEAPON_DATA =
            COMPONENTS.register("weapon_data", () -> DataComponentType.<WeaponData>builder()
                    .persistent(WeaponData.CODEC)
                    .build());

    private ModComponents() {}
}
