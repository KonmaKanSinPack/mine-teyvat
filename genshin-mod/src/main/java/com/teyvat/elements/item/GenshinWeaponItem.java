package com.teyvat.elements.item;

import java.util.List;

import com.teyvat.elements.TeyvatElements;
import com.teyvat.elements.element.Element;
import com.teyvat.elements.registry.ModAttributes;
import com.teyvat.elements.registry.ModComponents;
import com.teyvat.elements.weapon.WeaponData;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 原神武器通用基类：共享武器养成（等级加成 + 刻印）与按键规范。
 *
 * <p>默认无元素（PHYSICAL，量值 0）——普通武器命中只造成物理伤害，
 * 可正常触发碎冰等物理向反应；主题元素武器用带元素的构造器。</p>
 *
 * <p>按键规范：潜行+右键 = 刻印界面；右键 = 元素战技（无战技的武器不响应）。</p>
 */
public class GenshinWeaponItem extends ElementWandItem {
    private static final ResourceLocation LEVEL_BONUS_ID =
            ResourceLocation.fromNamespaceAndPath(TeyvatElements.MOD_ID, "weapon_level_bonus");

    /** 无元素武器（物理命中，可触发碎冰） */
    public GenshinWeaponItem(Properties properties) {
        this(Element.PHYSICAL, 0.0, properties);
    }

    public GenshinWeaponItem(Element element, double gauge, Properties properties) {
        super(element, gauge, properties);
    }

    /** 潜行+右键 = 刻印界面；普通右键无战技（子类覆盖实现） */
    @Override
    public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level level, Player player,
                                                                      net.minecraft.world.InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand == net.minecraft.world.InteractionHand.MAIN_HAND && player.isShiftKeyDown()
                && !level.isClientSide) {
            player.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> new com.teyvat.elements.menu.WeaponImprintMenu(id, inv),
                    stack.getHoverName()));
            return net.minecraft.world.InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        return net.minecraft.world.InteractionResultHolder.pass(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip,
                                net.minecraft.world.item.TooltipFlag flag) {
        WeaponData data = stack.getOrDefault(ModComponents.WEAPON_DATA, WeaponData.DEFAULT);
        tooltip.add(Component.translatable("item.teyvat_elements.homa_staff.level",
                        data.level(), data.maxLevel(), data.ascension())
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.teyvat_elements.homa_staff.skill")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide || !(entity instanceof LivingEntity living)) return;

        // 引用比较：inventoryTick 传入的就是手持的 ItemStack 实例
        boolean wielding = selected && living.getMainHandItem() == stack;

        // 等级加成：+0.2 × (等级-1)，主手持握才生效
        WeaponData data = stack.getOrDefault(ModComponents.WEAPON_DATA, WeaponData.DEFAULT);
        AttributeInstance attack = living.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) {
            if (wielding && data.level() > 1) {
                attack.addOrUpdateTransientModifier(new AttributeModifier(
                        LEVEL_BONUS_ID, data.attackBonus(), AttributeModifier.Operation.ADD_VALUE));
            } else {
                attack.removeModifier(LEVEL_BONUS_ID);
            }
        }

        applyImprintStats(living, stack, wielding);
    }

    /** 刻印属性表（槽位 → 属性/数值/运算），主手持握时生效 */
    private void applyImprintStats(LivingEntity living, ItemStack stack, boolean wielding) {
        List<ItemStack> imprints = stack.getOrDefault(ModComponents.IMPRINTS, List.of());

        for (ImprintItem.Slot slot : ImprintItem.Slot.values()) {
            ItemStack imprint = imprints.size() > slot.ordinal()
                    ? imprints.get(slot.ordinal()) : ItemStack.EMPTY;
            boolean active = wielding && imprint.getItem() instanceof ImprintItem;

            var holder = switch (slot) {
                case FLOWER -> Attributes.MAX_HEALTH;
                case PLUME -> Attributes.ATTACK_DAMAGE;
                case SANDS -> Attributes.ATTACK_SPEED;
                case GOBLET -> ModAttributes.ELEMENTAL_MASTERY;
                case CIRCLET -> ModAttributes.CRIT_RATE;
            };
            AttributeInstance attr = living.getAttribute(holder);
            if (attr == null) continue;

            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                    TeyvatElements.MOD_ID, "imprint_" + slot.name().toLowerCase());
            if (active) {
                var op = slot == ImprintItem.Slot.SANDS
                        ? AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                        : AttributeModifier.Operation.ADD_VALUE;
                double value = switch (slot) {
                    case FLOWER -> 4.0;
                    case PLUME -> 1.0;
                    case SANDS -> 0.1;
                    case GOBLET -> 40.0;
                    case CIRCLET -> 0.10;
                };
                attr.addOrUpdateTransientModifier(new AttributeModifier(id, value, op));
            } else {
                attr.removeModifier(id);
            }
        }
    }
}
