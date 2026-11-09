package com.teyvat.elements.item;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * 刻印：嵌入武器的增益件（对应原神圣遗物五件套）。
 *
 * <p>刻印本身不带效果，嵌入武器后属性生效（见 {@code HomaStaffItem.inventoryTick}）。
 * 每个刻印只能进对应槽位（GUI 槽位校验）。</p>
 */
public class ImprintItem extends Item {
    public enum Slot {
        FLOWER, PLUME, SANDS, GOBLET, CIRCLET
    }

    private final Slot slot;

    public ImprintItem(Slot slot, Properties properties) {
        super(properties);
        this.slot = slot;
    }

    public Slot slot() {
        return slot;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {
        tooltip.add(Component.translatable("imprint.teyvat_elements." + slot.name().toLowerCase() + ".effect")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("imprint.teyvat_elements.slot_hint",
                        Component.translatable("imprint.teyvat_elements." + slot.name().toLowerCase()))
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
