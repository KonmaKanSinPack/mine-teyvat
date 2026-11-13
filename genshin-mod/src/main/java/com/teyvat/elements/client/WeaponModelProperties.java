package com.teyvat.elements.client;

import com.teyvat.elements.TeyvatElements;
import com.teyvat.elements.registry.ModComponents;
import com.teyvat.elements.registry.ModItems;
import com.teyvat.elements.weapon.WeaponData;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * 武器模型谓词注册：读物品组件里的突破阶数，驱动 overrides 换升阶模型。
 */
public final class WeaponModelProperties {

    public static void register(FMLClientSetupEvent event) {
        // 注册表事件时序原因，物品谓词必须 enqueueWork
        event.enqueueWork(() -> {
            ItemPropertyFunction ascended = (stack, level, entity, seed) -> {
                WeaponData data = stack.get(ModComponents.WEAPON_DATA);
                // 3 阶（60 级突破后）换升阶模型
                return data != null && data.ascension() >= 3 ? 1.0f : 0.0f;
            };
            // 覆盖全部原神武器（护摩是 GenshinWeaponItem 子类，一并纳入）
            net.minecraft.core.registries.BuiltInRegistries.ITEM.forEach(item -> {
                if (item instanceof com.teyvat.elements.item.GenshinWeaponItem) {
                    ItemProperties.register(item,
                            ResourceLocation.fromNamespaceAndPath(TeyvatElements.MOD_ID, "ascended"),
                            ascended);
                }
            });
        });
    }

    private WeaponModelProperties() {}
}
