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
 * 护摩之杖：火元素长枪。
 *
 * <p>三层增益管线（都在 inventoryTick 里以瞬态修改器实现，不满足条件即移除）：</p>
 * <ul>
 *   <li>护摩被动：主手持握 + 生命 <50% → 攻击 +50%</li>
 *   <li>等级加成：主手持握 → +0.2×(等级-1)</li>
 *   <li>刻印：主手持握 → 五个槽位的刻印属性生效</li>
 * </ul>
 *
 * <p>右键打开刻印镶嵌界面（护摩不继承法杖的自我附着右键——武器的右键归镶嵌）。</p>
 */
public class HomaStaffItem extends ElementWandItem {
    private static final ResourceLocation HOMA_PASSIVE_ID =
            ResourceLocation.fromNamespaceAndPath(TeyvatElements.MOD_ID, "homa_passive");
    private static final ResourceLocation LEVEL_BONUS_ID =
            ResourceLocation.fromNamespaceAndPath(TeyvatElements.MOD_ID, "weapon_level_bonus");
    private static final float LOW_HP_THRESHOLD = 0.5f;
    private static final double ATTACK_BONUS = 0.5;

    public HomaStaffItem(Properties properties) {
        // 护摩是强附着：1.5 元素量
        super(Element.PYRO, 1.5, properties);
    }

    /** 右键：打开刻印镶嵌界面 */
    @Override
    public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level level, Player player,
                                                                      net.minecraft.world.InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && hand == net.minecraft.world.InteractionHand.MAIN_HAND) {
            player.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> new com.teyvat.elements.menu.WeaponImprintMenu(id, inv),
                    stack.getHoverName()));
        }
        return net.minecraft.world.InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip,
                                net.minecraft.world.item.TooltipFlag flag) {
        WeaponData data = stack.getOrDefault(ModComponents.WEAPON_DATA, WeaponData.DEFAULT);
        tooltip.add(Component.translatable(
                        "item.teyvat_elements.homa_staff.level",
                        data.level(), data.maxLevel(), data.ascension())
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("item.teyvat_elements.homa_staff.tooltip")
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide || !(entity instanceof LivingEntity living)) return;

        AttributeInstance attack = living.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack == null) return;

        // 引用比较是有意的：inventoryTick 传入的就是手持的那个 ItemStack 实例
        boolean wielding = selected && living.getMainHandItem() == stack;

        // 护摩被动：低生命 +50% 攻击
        boolean lowHp = living.getHealth() < living.getMaxHealth() * LOW_HP_THRESHOLD;
        if (wielding && lowHp) {
            attack.addOrUpdateTransientModifier(new AttributeModifier(
                    HOMA_PASSIVE_ID, ATTACK_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        } else {
            attack.removeModifier(HOMA_PASSIVE_ID);
        }

        // 等级加成：+0.2 × (等级-1)
        WeaponData data = stack.getOrDefault(ModComponents.WEAPON_DATA, WeaponData.DEFAULT);
        if (wielding && data.level() > 1) {
            attack.addOrUpdateTransientModifier(new AttributeModifier(
                    LEVEL_BONUS_ID, data.attackBonus(), AttributeModifier.Operation.ADD_VALUE));
        } else {
            attack.removeModifier(LEVEL_BONUS_ID);
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
                    case FLOWER -> 4.0;    // +4 生命上限（2 颗心）
                    case PLUME -> 1.0;     // +1 攻击
                    case SANDS -> 0.1;     // +10% 攻速
                    case GOBLET -> 40.0;   // +40 元素精通
                    case CIRCLET -> 0.10;  // +10% 暴击率
                };
                attr.addOrUpdateTransientModifier(new AttributeModifier(id, value, op));
            } else {
                attr.removeModifier(id);
            }
        }
    }
}
