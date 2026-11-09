package com.teyvat.elements.item;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * 结晶片：结晶反应的产物。不进背包，拾取即转化为吸收心
 * （逻辑见 {@code ShardPickupHandler}）。
 */
public class CrystalShardItem extends Item {
    public CrystalShardItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {
        tooltip.add(Component.translatable("item.teyvat_elements.crystal_shard.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }
}
