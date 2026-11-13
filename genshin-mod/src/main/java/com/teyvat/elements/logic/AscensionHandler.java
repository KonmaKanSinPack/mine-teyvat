package com.teyvat.elements.logic;

import com.teyvat.elements.item.GenshinWeaponItem;
import com.teyvat.elements.item.HomaStaffItem;
import com.teyvat.elements.registry.ModComponents;
import com.teyvat.elements.registry.ModItems;
import com.teyvat.elements.weapon.WeaponData;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.AnvilUpdateEvent;

/**
 * 武器养成（铁砧一站式）：武器（左）+ 材料（右）。
 *
 * <ul>
 *   <li>武器 + 突破石：到等级上限时突破升阶（5 级玩家经验）</li>
 *   <li>武器 + 精锻用杂矿/良矿/魔矿：升 1/2/5 级（到顶截断，1 级经验手续费）</li>
 *   <li>满级满阶：无配方</li>
 * </ul>
 */
public final class AscensionHandler {

    /** 注册在 NeoForge.EVENT_BUS */
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        if (!(left.getItem() instanceof GenshinWeaponItem)) return;

        WeaponData data = left.getOrDefault(ModComponents.WEAPON_DATA, WeaponData.DEFAULT);

        // 突破石：到顶突破升阶
        if (event.getRight().is(ModItems.ASCENSION_STONE.get())) {
            if (!data.canAscend()) return;
            ItemStack output = left.copy();
            output.set(ModComponents.WEAPON_DATA, data.ascend());
            event.setOutput(output);
            event.setCost(5);
            event.setMaterialCost(1);
            return;
        }

        // 精锻矿三档：杂矿 +1 / 良矿 +2 / 魔矿 +5
        int gain = oreTier(event.getRight());
        if (gain > 0) {
            if (!data.canLevelUp()) return;
            ItemStack output = left.copy();
            output.set(ModComponents.WEAPON_DATA, data.addLevels(gain));
            event.setOutput(output);
            event.setCost(1);
            event.setMaterialCost(1);
        }
    }

    /** 精锻矿档位 → 升级数；非精锻矿返回 0 */
    private static int oreTier(ItemStack stack) {
        if (stack.is(ModItems.ENHANCEMENT_ORE.get())) return 1;
        if (stack.is(ModItems.ENHANCEMENT_ORE_FINE.get())) return 2;
        if (stack.is(ModItems.ENHANCEMENT_ORE_MYSTIC.get())) return 10;
        return 0;
    }

    private AscensionHandler() {}
}
