package com.teyvat.elements.logic;

import com.teyvat.elements.item.HomaStaffItem;
import com.teyvat.elements.registry.ModComponents;
import com.teyvat.elements.registry.ModItems;
import com.teyvat.elements.weapon.WeaponData;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.AnvilUpdateEvent;

/**
 * 武器突破：铁砧 = 武器（左）+ 突破石（右）→ 升一阶。
 *
 * <p>条件：到当前阶数等级上限且未满阶。消耗 1 个突破石 + 5 级玩家经验。</p>
 */
public final class AscensionHandler {

    /** 注册在 NeoForge.EVENT_BUS */
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        if (!(left.getItem() instanceof HomaStaffItem)) return;
        if (!event.getRight().is(ModItems.ASCENSION_STONE.get())) return;

        WeaponData data = left.getOrDefault(ModComponents.WEAPON_DATA, WeaponData.DEFAULT);
        if (!data.canAscend()) return;

        ItemStack output = left.copy();
        output.set(ModComponents.WEAPON_DATA, data.ascend());

        event.setOutput(output);
        event.setCost(5);          // 玩家经验等级消耗
        event.setMaterialCost(1);  // 突破石消耗 1 个
    }

    private AscensionHandler() {}
}
