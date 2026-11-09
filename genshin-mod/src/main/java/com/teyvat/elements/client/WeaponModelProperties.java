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
                return data != null && data.ascension() > 0 ? 1.0f : 0.0f;
            };
            ItemProperties.register(ModItems.HOMA_STAFF.get(),
                    ResourceLocation.fromNamespaceAndPath(TeyvatElements.MOD_ID, "ascended"),
                    ascended);
        });
    }

    private WeaponModelProperties() {}
}
