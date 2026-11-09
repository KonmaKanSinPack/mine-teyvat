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

/**
 * 原激化（Quicken）：草 × 雷，双向。
 *
 * <p>状态反应：不造成伤害，给目标挂 QUICKEN 复合附着（量值=消耗量）。
 * 激化存续期间，目标的草/雷命中会触发超激化/蔓激化（加算增伤），
 * 且不消耗激化附着——"先挂状态，后续持续受益"是激化体系的玩法核心。</p>
 */
public class QuickenReaction extends ElementalReaction {
    /** @param dendroAura true = 草附着被雷触发；false = 雷附着被草触发 */
    public QuickenReaction(boolean dendroAura) {
        super(
            dendroAura ? "quicken_electro_trigger" : "quicken_dendro_trigger",
            dendroAura ? Element.DENDRO : Element.ELECTRO,
            dendroAura ? Element.ELECTRO : Element.DENDRO,
            1.0,
            1.0,
            Type.TRANSFORMATIVE
        );
    }

    @Override
    protected void onReact(LivingEntity target, double reducedGauge, @Nullable LivingEntity origin) {
        ElementalData data = target.getData(ModAttachments.ELEMENTAL_DATA);
        data.applyAura(Element.QUICKEN, reducedGauge, "reaction:quicken");
        target.setData(ModAttachments.ELEMENTAL_DATA, data);

        if (target.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(),
                    16, 0.35, 0.4, 0.35, 0.08);
            level.playSound(null, target.blockPosition(),
                    SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.3f, 1.9f);
        }
    }
}
