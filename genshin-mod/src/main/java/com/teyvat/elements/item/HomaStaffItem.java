package com.teyvat.elements.item;

import com.teyvat.elements.TeyvatElements;
import com.teyvat.elements.element.Element;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 护摩之杖：火元素长枪（1.5 元素量强附着）。
 *
 * <p>养成与刻印复用 {@link GenshinWeaponItem} 基类；本类只加护摩的特色：
 * 被动（生命 <50% 攻击 +50%）与元素战技（右键火环 AoE，5s 冷却）。</p>
 */
public class HomaStaffItem extends GenshinWeaponItem {
    private static final ResourceLocation HOMA_PASSIVE_ID =
            ResourceLocation.fromNamespaceAndPath(TeyvatElements.MOD_ID, "homa_passive");
    private static final float LOW_HP_THRESHOLD = 0.5f;
    private static final double ATTACK_BONUS = 0.5;
    /** 战技冷却：5 秒 */
    private static final int SKILL_COOLDOWN_TICKS = 100;

    public HomaStaffItem(Properties properties) {
        super(Element.PYRO, 1.5, properties);
    }

    /** 潜行+右键 = 刻印界面（基类实现）；右键 = 护摩战技·火环 */
    @Override
    public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level level, Player player,
                                                                      net.minecraft.world.InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != net.minecraft.world.InteractionHand.MAIN_HAND || player.isShiftKeyDown()) {
            return super.use(level, player, hand);
        }
        if (!level.isClientSide && !player.getCooldowns().isOnCooldown(this)) {
            castSkill(level, player);
            player.getCooldowns().addCooldown(this, SKILL_COOLDOWN_TICKS);
        }
        return net.minecraft.world.InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    /** 护摩战技：正前方 2.5 格、半径 2 的扇面，范围内生物附着火 1.0 */
    private void castSkill(Level level, Player player) {
        net.minecraft.world.phys.Vec3 center = player.getEyePosition()
                .add(player.getLookAngle().scale(2.5));
        net.minecraft.world.phys.AABB box = new net.minecraft.world.phys.AABB(center, center).inflate(2.0);

        int hit = 0;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (target == player) continue;
            com.teyvat.elements.logic.ElementSystem.applyElement(
                    target, Element.PYRO, 1.0, "skill:homa", player);
            hit++;
        }

        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME,
                    center.x, center.y, center.z, 30, 0.8, 0.4, 0.8, 0.08);
            serverLevel.playSound(null, player.blockPosition(),
                    net.minecraft.sounds.SoundEvents.FIRECHARGE_USE,
                    net.minecraft.sounds.SoundSource.PLAYERS, 0.9f, 1.1f);
        }
        TeyvatElements.LOGGER.info("homa skill cast by {}, {} targets", player.getName().getString(), hit);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide || !(entity instanceof LivingEntity living)) return;

        // 护摩被动：主手持握 + 生命 <50% → 攻击 +50%
        AttributeInstance attack = living.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack == null) return;

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
