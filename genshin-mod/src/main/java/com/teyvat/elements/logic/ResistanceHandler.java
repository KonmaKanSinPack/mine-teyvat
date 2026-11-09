package com.teyvat.elements.logic;

import com.teyvat.elements.registry.ModAttributes;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * 抗性结算：物理伤害按目标的物理抗性调整。
 *
 * <p>物理伤害的判定：近战（directEntity==攻击者）或弹射物，且排除我们反应系统
 * 发出的 magic/indirectMagic（那些是元素伤害，不吃物理抗性）。</p>
 */
public final class ResistanceHandler {

    /** 注册在 NeoForge.EVENT_BUS */
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) return;

        AttributeInstance resistance = target.getAttribute(ModAttributes.PHYSICAL_RESISTANCE);
        if (resistance == null || resistance.getValue() == 0) return;
        if (!isPhysical(event.getSource())) return;

        // 抗性 -0.4 → 承伤 ×1.4；抗性 0.9 → 承伤 ×0.1
        event.setAmount(event.getAmount() * (float) (1.0 - resistance.getValue()));
    }

    private static boolean isPhysical(DamageSource source) {
        String msgId = source.getMsgId();
        if (msgId.equals("magic") || msgId.equals("indirectMagic")) return false;
        return source.is(DamageTypeTags.IS_PROJECTILE) || source.getDirectEntity() == source.getEntity();
    }

    private ResistanceHandler() {}
}
