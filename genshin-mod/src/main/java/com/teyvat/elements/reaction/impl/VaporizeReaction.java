package com.teyvat.elements.reaction.impl;

import org.jetbrains.annotations.Nullable;

import com.teyvat.elements.element.Element;
import com.teyvat.elements.reaction.ElementalReaction;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * 蒸发（Vaporize）：水 × 火。
 *
 * <p>增幅反应：放大触发这次反应的那次攻击（水打火附着 2.0 倍，火打水附着 1.5 倍）。
 * 伤害放大由 {@code ElementalCombatHandler} 在伤害事件中完成，本类只声明
 * 三元组数据与表现层效果。</p>
 */
public class VaporizeReaction extends ElementalReaction {
    /**
     * @param hydroAura true = 水附着被火触发（系数 0.5，倍率 1.5）；
     *                  false = 火附着被水触发（系数 2.0，倍率 2.0）
     */
    public VaporizeReaction(boolean hydroAura) {
        super(
            hydroAura ? "vaporize_pyro_trigger" : "vaporize_hydro_trigger",
            hydroAura ? Element.HYDRO : Element.PYRO,
            hydroAura ? Element.PYRO : Element.HYDRO,
            hydroAura ? 0.5 : 2.0,
            hydroAura ? 1.5 : 2.0,
            Type.AMPLIFYING
        );
    }

    /**
     * 增幅反应的伤害放大发生在伤害事件里（ElementalCombatHandler 直接乘 event amount），
     * 本钩子只负责表现层。
     */
    @Override
    protected void onReact(LivingEntity target, double reducedGauge, @Nullable LivingEntity origin) {
        if (target.level() instanceof ServerLevel level) {
            // 蒸汽：白色云雾粒子
            level.sendParticles(ParticleTypes.CLOUD,
                    target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(),
                    12, 0.3, 0.3, 0.3, 0.02);
            level.playSound(null, target.blockPosition(),
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.7f, 1.2f);
        }
    }
}
