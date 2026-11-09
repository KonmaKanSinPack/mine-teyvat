package com.teyvat.elements.logic;

import com.teyvat.elements.item.HomaStaffItem;
import com.teyvat.elements.registry.ModComponents;
import com.teyvat.elements.registry.ModItems;
import com.teyvat.elements.weapon.WeaponData;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.AnvilUpdateEvent;

/**
 * 武器养成（铁砧一站式）：武器（左）+ 突破石（右）。
 *
 * <ul>
 *   <li>未到等级上限：升 1 级，消耗玩家经验 {@link WeaponData#levelUpCost()} 级</li>
 *   <li>到上限且未满阶：突破升一阶，消耗 5 级经验</li>
 *   <li>满级满阶：无配方</li>
 * </ul>
 */
public final class AscensionHandler {

    /** 注册在 NeoForge.EVENT_BUS */
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        if (!(left.getItem() instanceof HomaStaffItem)) return;
        if (!event.getRight().is(ModItems.ASCENSION_STONE.get())) return;

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

        // 精锻魔矿：免费升 1 级（材料路线，1 级经验手续费）
        if (event.getRight().is(ModItems.ENHANCEMENT_ORE.get())) {
            if (!data.canLevelUp()) return;
            ItemStack output = left.copy();
            output.set(ModComponents.WEAPON_DATA, data.levelUp());
            event.setOutput(output);
            event.setCost(1);
            event.setMaterialCost(1);
        }
    }

    private AscensionHandler() {}
}
