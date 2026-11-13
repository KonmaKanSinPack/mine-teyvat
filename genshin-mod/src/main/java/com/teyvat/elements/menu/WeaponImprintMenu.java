package com.teyvat.elements.menu;

import java.util.ArrayList;
import java.util.List;

import com.teyvat.elements.item.ImprintItem;
import com.teyvat.elements.registry.ModComponents;
import com.teyvat.elements.registry.ModMenus;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * 武器刻印镶嵌界面（5 槽）。
 *
 * <p>数据直写主手武器的 imprints 组件：槽位一变就写回，关闭界面时物品已是最新——
 * 组件跟随物品，掉落/装箱不丢刻印。</p>
 */
public class WeaponImprintMenu extends AbstractContainerMenu {
    public static final int SLOT_COUNT = 5;

    private final ItemStack weapon;
    private final ItemStackHandler imprints;

    public WeaponImprintMenu(int containerId, Inventory playerInv) {
        super(ModMenus.WEAPON_IMPRINT.get(), containerId);
        this.weapon = playerInv.player.getMainHandItem();

        this.imprints = new ItemStackHandler(SLOT_COUNT) {
            @Override
            protected void onContentsChanged(int slot) {
                save();
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                // 槽位只收对应类型的刻印
                return stack.getItem() instanceof ImprintItem imprint
                        && imprint.slot().ordinal() == slot;
            }
        };

        // 读回已镶嵌的刻印（容错：旧数据可能不足 5 个）
        List<ItemStack> stored = weapon.getOrDefault(ModComponents.IMPRINTS, List.of());
        for (int i = 0; i < Math.min(stored.size(), SLOT_COUNT); i++) {
            imprints.setStackInSlot(i, stored.get(i));
        }

        // 刻印槽：漏斗布局（一排 5 槽，x=44+18i，y=20）
        for (int i = 0; i < SLOT_COUNT; i++) {
            addSlot(new SlotItemHandler(imprints, i, 44 + i * 18, 20));
        }
        // 玩家背包 + 快捷栏（漏斗标准位置）
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 51 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, 109));
        }
    }

    /** 槽位内容写回武器组件 */
    private void save() {
        List<ItemStack> list = new ArrayList<>(SLOT_COUNT);
        for (int i = 0; i < SLOT_COUNT; i++) {
            list.add(imprints.getStackInSlot(i));
        }
        weapon.set(ModComponents.IMPRINTS, list);
    }

    @Override
    public boolean stillValid(Player player) {
        // 手里还拿着这把武器才有效
        return player.getMainHandItem() == weapon;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // Shift 点击：背包 → 对应刻印槽；刻印槽 → 背包
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();

        if (index < SLOT_COUNT) {
            // 从刻印槽移回背包
            if (!moveItemStackTo(stack, SLOT_COUNT, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            // 从背包移到匹配槽位
            if (!(stack.getItem() instanceof ImprintItem imprint)) return ItemStack.EMPTY;
            if (!moveItemStackTo(stack, imprint.slot().ordinal(), imprint.slot().ordinal() + 1, false)) {
                return ItemStack.EMPTY;
            }
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return stack;
    }
}
