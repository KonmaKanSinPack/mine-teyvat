package com.teyvat.elements.reaction.impl;

import org.jetbrains.annotations.Nullable;

import com.teyvat.elements.element.Element;
import com.teyvat.elements.reaction.ElementalReaction;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 超载（Overloaded）：火 × 雷，可逆。
 *
 * <p>原神中超载是"剧变反应"：造成独立的范围火伤并击退。
 * 本实现：以目标为中心 2 格范围伤害（含目标自身），并给目标一个向上击退。</p>
 */
public class OverloadedReaction extends ElementalReaction {
    private static final double AOE_RADIUS = 2.0;

    /** @param reversed false = 火附着被雷触发；true = 雷附着被火触发 */
    public OverloadedReaction(boolean reversed) {
        super(
            reversed ? "overloaded_reversed" : "overloaded",
            reversed ? Element.ELECTRO : Element.PYRO,
            reversed ? Element.PYRO : Element.ELECTRO,
            1.0,
            1.0,
            Type.TRANSFORMATIVE
        );
    }

    @Override
    protected void onReact(LivingEntity target, double reducedGauge, @Nullable LivingEntity origin) {
        float damage = (float) (reducedGauge * 4.0 * multiplier());

        AABB box = target.getBoundingBox().inflate(AOE_RADIUS);
        for (LivingEntity nearby : target.level().getEntitiesOfClass(LivingEntity.class, box)) {
            if (origin != null) {
                nearby.hurt(target.damageSources().indirectMagic(origin, origin), damage);
            } else {
                nearby.hurt(target.damageSources().magic(), damage);
            }
        }

        // 超载的击退感：向上弹起
        Vec3 motion = target.getDeltaMovement();
        target.setDeltaMovement(motion.x, Math.min(motion.y + 0.5, 0.8), motion.z);
        target.hurtMarked = true;

        if (target.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.EXPLOSION,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                    3, 0.2, 0.2, 0.2, 0.0);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                    16, 0.4, 0.4, 0.4, 0.05);
            level.playSound(null, target.blockPosition(),
                    SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.5f, 1.8f);
        }
    }
}
