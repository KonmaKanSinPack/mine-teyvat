package com.teyvat.elements.item;

import java.util.List;

import com.teyvat.elements.element.Element;
import com.teyvat.elements.logic.ElementSystem;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * 元素法杖：本身是"元素标记物"——近战命中时由 {@code ElementalCombatHandler}
 * 读取主手法杖的元素并在伤害结算前处理（增幅/附着）。
 *
 * <p>为什么命中判定不在 hurtEnemy 里做：hurtEnemy 是伤害结算后的回调，
 * 增幅反应来不及放大伤害。法杖只负责声明元素，判定统一在伤害管线。</p>
 */
public class ElementWandItem extends Item {
    private final Element element;
    private final double gauge;

    public ElementWandItem(Element element, double gauge, Properties properties) {
        super(properties);
        this.element = element;
        this.gauge = gauge;
    }

    public Element element() {
        return element;
    }

    public double gauge() {
        return gauge;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {
        tooltip.add(Component.translatable("item.teyvat_elements.wand.tooltip",
                        Component.translatable("element.teyvat_elements." + element.id()))
                .withStyle(ChatFormatting.GRAY));
    }

    /** 右键：对玩家自身施加元素（测试用，便于观察衰减与附着图标） */
    @Override
    public net.minecraft.world.InteractionResultHolder<ItemStack> use(net.minecraft.world.level.Level level,
                                                                      Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ElementSystem.applyElement(player, element, gauge, toString(), player);
        return net.minecraft.world.InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
