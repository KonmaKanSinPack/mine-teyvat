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
 * 冻结（Frozen）：水 × 冰，双向同效。
 *
 * <p>不产生伤害，而是给目标挂上 FREEZE 复合附着（量值 = 反应消耗量，
 * 绕过 ICD）。定身效果由 {@code FreezeController} 在附着变化后统一施加——
 * 反应类只管附着与表现，不碰属性，保证"冻结状态只有一个权威来源"。</p>
 */
public class FrozenReaction extends ElementalReaction {
    /** @param hydroAura true = 水附着被冰触发；false = 冰附着被水触发 */
    public FrozenReaction(boolean hydroAura) {
        super(
            hydroAura ? "frozen_cryo_trigger" : "frozen_hydro_trigger",
            hydroAura ? Element.HYDRO : Element.CRYO,
            hydroAura ? Element.CRYO : Element.HYDRO,
            1.0,
            1.0,
            Type.TRANSFORMATIVE
        );
    }

    @Override
    protected void onReact(LivingEntity target, double reducedGauge, @Nullable LivingEntity origin) {
        // FREEZE 是复合元素（母体冰），量值取反应消耗量；定身随附着存续
        ElementalData data = target.getData(ModAttachments.ELEMENTAL_DATA);
        data.applyAura(Element.FREEZE, reducedGauge, "reaction:frozen");
        target.setData(ModAttachments.ELEMENTAL_DATA, data);

        if (target.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.SNOWFLAKE,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                    20, 0.35, 0.5, 0.35, 0.03);
            level.playSound(null, target.blockPosition(),
                    SoundEvents.PLAYER_HURT_FREEZE, SoundSource.PLAYERS, 0.8f, 1.0f);
        }
    }
}
