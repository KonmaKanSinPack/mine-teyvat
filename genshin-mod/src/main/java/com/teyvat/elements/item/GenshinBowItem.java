package com.teyvat.elements.item;

import com.teyvat.elements.entity.GenshinArrowEntity;
import com.teyvat.elements.element.Element;
import com.teyvat.elements.registry.ModComponents;
import com.teyvat.elements.weapon.WeaponData;

import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;

/**
 * 原神弓：右键拉弓（原版 UseAnim.BOW 动画），松开放箭。
 *
 * <p>蓄力 1 秒满弓；伤害 =（基础面板 + 等级加成）× 蓄力比例；满蓄箭带暴击轨迹。
 * 潜行+右键仍开刻印界面（基类实现）。</p>
 */
public class GenshinBowItem extends GenshinWeaponItem {
    /** 满蓄所需 tick（1 秒） */
    private static final int FULL_CHARGE_TICKS = 20;

    public GenshinBowItem(Element element, double gauge, Properties properties) {
        super(element, gauge, properties);
    }

    /** 右键开弓；潜行+右键走基类的刻印界面 */
    @Override
    public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level level, Player player,
                                                                      net.minecraft.world.InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            return super.use(level, player, hand);
        }
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return net.minecraft.world.InteractionResultHolder.consume(stack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000; // 持续拉弓（原版弓同值）
    }

    /** 松开放箭 */
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (level.isClientSide || !(entity instanceof Player player)) return;

        int useTicks = getUseDuration(stack, entity) - timeLeft;
        float charge = Math.min(1.0f, (float) useTicks / FULL_CHARGE_TICKS);
        if (charge < 0.15f) return; // 拉弓不足不放箭

        GenshinArrowEntity arrow = new GenshinArrowEntity(player, level, element(), gauge());
        arrow.setBaseDamage(arrowDamage(stack, charge));
        arrow.setCritArrow(charge >= 1.0f); // 满蓄暴击轨迹
        // 沿视线方向射出（速度随蓄力增加）
        net.minecraft.world.phys.Vec3 look = player.getLookAngle();
        arrow.shoot(look.x, look.y, look.z, 1.0f + charge * 2.0f, 1.0f);
        level.addFreshEntity(arrow);

        level.playSound(null, player.blockPosition(), SoundEvents.ARROW_SHOOT,
                SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    /** 箭伤 =（1 + 武器面板攻击 + 等级加成）× 蓄力比例 */
    private double arrowDamage(ItemStack stack, float charge) {
        double base = 0;
        ItemAttributeModifiers modifiers = stack.getOrDefault(
                DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        for (var entry : modifiers.modifiers()) {
            if (entry.attribute().equals(Attributes.ATTACK_DAMAGE)
                    && entry.slot() == EquipmentSlotGroup.MAINHAND) {
                base += entry.modifier().amount();
            }
        }
        WeaponData data = stack.getOrDefault(ModComponents.WEAPON_DATA, WeaponData.DEFAULT);
        return (1.0 + base + data.attackBonus()) * charge;
    }
}
