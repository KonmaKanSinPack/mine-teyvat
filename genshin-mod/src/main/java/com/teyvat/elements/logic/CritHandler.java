package com.teyvat.elements.logic;

import com.teyvat.elements.registry.ModAttributes;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * 暴击结算：攻击者的 crit_rate 属性掷骰，暴击 ×1.5。
 *
 * <p>作用于所有直接攻击（近战/法杖命中都过这里；反应造成的 magic 间接伤害不掷——
 * 那些由元素精通加成，两条增益路线分开）。默认 5% 还原原神初始面板。</p>
 */
public final class CritHandler {
    /** 暴击伤害倍率（原神初始 50% 暴伤） */
    private static final float CRIT_MULTIPLIER = 1.5f;

    /** 注册在 NeoForge.EVENT_BUS */
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        if (attacker.level().isClientSide) return;
        // 只对直接攻击掷骰（反应间接伤害走精通路线）
        if (event.getSource().getDirectEntity() != attacker) return;

        AttributeInstance critRate = attacker.getAttribute(ModAttributes.CRIT_RATE);
        if (critRate == null || critRate.getValue() <= 0) return;
        if (attacker.getRandom().nextDouble() >= critRate.getValue()) return;

        event.setAmount(event.getAmount() * CRIT_MULTIPLIER);

        if (event.getEntity().level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.CRIT,
                    event.getEntity().getX(),
                    event.getEntity().getY() + event.getEntity().getBbHeight() * 0.6,
                    event.getEntity().getZ(),
                    10, 0.3, 0.3, 0.3, 0.15);
            level.playSound(null, event.getEntity().blockPosition(),
                    SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 0.9f, 1.0f);
        }
    }

    private CritHandler() {}
}
