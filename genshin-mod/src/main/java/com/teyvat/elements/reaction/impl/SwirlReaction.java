package com.teyvat.elements.reaction.impl;

import org.jetbrains.annotations.Nullable;

import com.teyvat.elements.element.Element;
import com.teyvat.elements.logic.ElementSystem;
import com.teyvat.elements.reaction.ElementalReaction;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 扩散（Swirl）：风 × 火/水/雷/冰附着。
 *
 * <p>风永远只能当触发元素（不能附着），所以四个方向各注册一个实例，无反向。
 * 扩散的招牌行为：把目标身上的附着元素<b>传播给周围的生物</b>——
 * 传播走 {@link ElementSystem#applyElement}，因此被波及的怪身上的附着
 * 会正常参与后续反应（比如把火扩散到带雷的怪身上会当场超载），
 * 这种链式涌现正是原神扩散的玩法核心。</p>
 */
public class SwirlReaction extends ElementalReaction {
    private static final double SPREAD_RADIUS = 3.0;
    private static final double TARGET_DAMAGE = 3.0;
    /** 扩散出去的附着量（过 applyElement 的附着税后再打折，是有意的：扩散本应弱于直接附着） */
    private static final double SPREAD_GAUGE = 0.5;

    /** @param auraElement 被扩散的附着元素（火/水/雷/冰之一） */
    public SwirlReaction(Element auraElement) {
        super(
            "swirl_" + auraElement.id(),
            auraElement,
            Element.ANEMO,
            0.5,
            1.0,
            Type.TRANSFORMATIVE
        );
        if (auraElement == Element.ANEMO || !auraElement.canBeAura()) {
            throw new IllegalArgumentException("Swirl 的附着元素必须是可附着的火/水/雷/冰");
        }
    }

    @Override
    protected void onReact(LivingEntity target, double reducedGauge, @Nullable LivingEntity origin) {
        // 风伤命中目标
        if (origin != null) {
            target.hurt(target.damageSources().indirectMagic(origin, origin),
                    (float) (reducedGauge * TARGET_DAMAGE));
        } else {
            target.hurt(target.damageSources().magic(), (float) (reducedGauge * TARGET_DAMAGE));
        }

        if (!(target.level() instanceof ServerLevel level)) return;

        // 扩散：把附着元素传播给周围的生物（目标自身除外）
        AABB box = target.getBoundingBox().inflate(SPREAD_RADIUS);
        for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (nearby == target) continue;
            ElementSystem.applyElement(nearby, auraElement(), SPREAD_GAUGE, "reaction:swirl", origin);
        }

        // 观感：风场云雾 + 被扩散元素的粒子向外散
        Vec3 center = target.position().add(0, target.getBbHeight() * 0.5, 0);
        level.sendParticles(ParticleTypes.CLOUD, center.x, center.y, center.z,
                14, 0.6, 0.4, 0.6, 0.04);
        if (auraElement().orbitParticle() != null) {
            level.sendParticles(auraElement().orbitParticle(), center.x, center.y, center.z,
                    20, 0.8, 0.6, 0.8, 0.06);
        }
        level.playSound(null, target.blockPosition(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.8f, 0.8f);
    }
}
