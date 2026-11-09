package com.teyvat.elements.reaction.impl;

import org.jetbrains.annotations.Nullable;

import com.teyvat.elements.data.ElementalData;
import com.teyvat.elements.data.ModAttachments;
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
 * 感电（Electro-Charged）：水 × 雷，双向。
 *
 * <p>原神中的感电有持续 DoT 和水雷共存规则；骨架期只保留它最有辨识度的
 * 两个特征：瞬时雷伤 + <b>闪电链</b>——向附近"带水附着或身处水中/雨中"的
 * 生物传导伤害。DoT 留待调度器基础设施。</p>
 */
public class ElectroChargedReaction extends ElementalReaction {
    /** 闪电链传导半径 */
    private static final double CHAIN_RADIUS = 3.0;
    /** 主目标伤害倍率（每点消耗量值） */
    private static final double TARGET_DAMAGE = 4.0;
    /** 连锁目标伤害倍率 */
    private static final double CHAIN_DAMAGE = 3.0;

    /** @param hydroAura true = 水附着被雷触发；false = 雷附着被水触发 */
    public ElectroChargedReaction(boolean hydroAura) {
        super(
            hydroAura ? "electro_charged_electro_trigger" : "electro_charged_hydro_trigger",
            hydroAura ? Element.HYDRO : Element.ELECTRO,
            hydroAura ? Element.ELECTRO : Element.HYDRO,
            1.0,
            1.0,
            Type.TRANSFORMATIVE
        );
    }

    @Override
    protected void onReact(LivingEntity target, double reducedGauge, @Nullable LivingEntity origin) {
        hurt(target, reducedGauge * TARGET_DAMAGE, origin);

        if (!(target.level() instanceof ServerLevel level)) return;

        // 闪电链：只传给"湿的"目标（带水附着或在水中/雨中），这是感电的传播规则
        AABB box = target.getBoundingBox().inflate(CHAIN_RADIUS);
        for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (nearby == target) continue;
            if (!isWet(nearby)) continue;

            hurt(nearby, reducedGauge * CHAIN_DAMAGE, origin);
            drawLightningArc(level, target, nearby);
        }

        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(),
                24, 0.4, 0.5, 0.4, 0.08);
        level.playSound(null, target.blockPosition(),
                SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.4f, 1.6f);
    }

    /** 水附着或身处水中/雨中均视为"湿" */
    private static boolean isWet(LivingEntity entity) {
        if (entity.isInWaterOrRain()) return true;
        if (!entity.hasData(ModAttachments.ELEMENTAL_DATA)) return false;
        ElementalData data = entity.getData(ModAttachments.ELEMENTAL_DATA);
        return data.getAura(Element.HYDRO) != null;
    }

    /** 在两个实体之间画一条电光粒子折线（中点随机抖动模拟闪电的不规则感） */
    private static void drawLightningArc(ServerLevel level, LivingEntity from, LivingEntity to) {
        Vec3 a = from.position().add(0, from.getBbHeight() * 0.6, 0);
        Vec3 b = to.position().add(0, to.getBbHeight() * 0.6, 0);
        int segments = 6;
        for (int i = 0; i <= segments; i++) {
            double t = i / (double) segments;
            Vec3 p = a.lerp(b, t);
            // 两端不抖、中间抖，形成折线感
            double jitter = (i == 0 || i == segments) ? 0 : 0.15;
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    p.x + (level.random.nextDouble() - 0.5) * jitter,
                    p.y + (level.random.nextDouble() - 0.5) * jitter,
                    p.z + (level.random.nextDouble() - 0.5) * jitter,
                    2, 0.02, 0.02, 0.02, 0.0);
        }
    }

    private static void hurt(LivingEntity entity, double amount, @Nullable LivingEntity origin) {
        if (origin != null) {
            entity.hurt(entity.damageSources().indirectMagic(origin, origin), (float) amount);
        } else {
            entity.hurt(entity.damageSources().magic(), (float) amount);
        }
    }
}
