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
 * 超激化（Aggravate）：激化附着 + 雷命中 → 当次伤害加算提升。
 * 蔓激化（Spread）：激化附着 + 草命中 → 当次伤害加算提升。
 *
 * <p>两者都不消耗激化附着（{@link #consumesAura()} = false）。
 * 加算数值由伤害管线计算：{@code 附着量值 × 2 × multiplier}。</p>
 */
public class SpreadAggravateReaction extends ElementalReaction {
    private final boolean aggravate;

    /** @param aggravate true = 超激化（雷命中）；false = 蔓激化（草命中） */
    public SpreadAggravateReaction(boolean aggravate) {
        super(
            aggravate ? "aggravate" : "spread",
            Element.QUICKEN,
            aggravate ? Element.ELECTRO : Element.DENDRO,
            1.0,
            aggravate ? 1.15 : 1.2,
            Type.ADDITIVE
        );
        this.aggravate = aggravate;
    }

    /** 激化状态不被后续命中消耗 */
    @Override
    public boolean consumesAura() {
        return false;
    }

    @Override
    protected void onReact(LivingEntity target, double reducedGauge, @Nullable LivingEntity origin) {
        if (target.level() instanceof ServerLevel level) {
            // 超激化偏电光、蔓激化偏草叶
            level.sendParticles(
                    aggravate ? ParticleTypes.ELECTRIC_SPARK : ParticleTypes.HAPPY_VILLAGER,
                    target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(),
                    14, 0.3, 0.4, 0.3, 0.06);
            level.playSound(null, target.blockPosition(),
                    aggravate ? SoundEvents.LIGHTNING_BOLT_IMPACT : SoundEvents.GRASS_BREAK,
                    SoundSource.PLAYERS, 0.4f, aggravate ? 2.0f : 0.7f);
        }
    }
}
