package com.teyvat.elements.item;

import com.teyvat.elements.TeyvatElements;
import com.teyvat.elements.element.Element;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 护摩之杖：火元素长枪。
 *
 * <p>元素判定完全复用 {@link ElementWandItem}（伤害管线读主手元素），
 * 本类只加护摩的招牌被动：<b>生命低于 50% 时攻击力 +50%</b>。</p>
 *
 * <p>被动用 {@code inventoryTick} + 瞬态属性修改器实现：每 tick 校验
 * "主手持握且低生命"两个条件，不满足即移除，没有状态残留风险。</p>
 */
public class HomaStaffItem extends ElementWandItem {
    private static final ResourceLocation HOMA_PASSIVE_ID =
            ResourceLocation.fromNamespaceAndPath(TeyvatElements.MOD_ID, "homa_passive");
    /** 低生命阈值与增益倍率（还原护摩被动） */
    private static final float LOW_HP_THRESHOLD = 0.5f;
    private static final double ATTACK_BONUS = 0.5;

    public HomaStaffItem(Properties properties) {
        // 护摩是强附着：1.5 元素量
        super(Element.PYRO, 1.5, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                java.util.List<net.minecraft.network.chat.Component> tooltip,
                                net.minecraft.world.item.TooltipFlag flag) {
        tooltip.add(net.minecraft.network.chat.Component
                .translatable("item.teyvat_elements.homa_staff.tooltip")
                .withStyle(net.minecraft.ChatFormatting.GRAY));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide || !(entity instanceof LivingEntity living)) return;

        AttributeInstance attack = living.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack == null) return;

        // 引用比较是有意的：inventoryTick 传入的就是手持的那个 ItemStack 实例
        boolean wielding = selected && living.getMainHandItem() == stack;
        boolean lowHp = living.getHealth() < living.getMaxHealth() * LOW_HP_THRESHOLD;

        if (wielding && lowHp) {
            attack.addOrUpdateTransientModifier(new AttributeModifier(
                    HOMA_PASSIVE_ID, ATTACK_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        } else {
            attack.removeModifier(HOMA_PASSIVE_ID);
        }
    }
}
