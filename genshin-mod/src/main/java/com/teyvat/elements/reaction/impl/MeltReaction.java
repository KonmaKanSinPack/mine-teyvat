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
 * 融化（Melt）：火 × 冰。
 *
 * <p>增幅反应，三个方向：</p>
 * <ul>
 *   <li>冰附着 + 火触发：2.0 倍，系数 0.5（火克冰，1GU 火只耗 0.5GU 冰）</li>
 *   <li>火附着 + 冰触发：1.5 倍，系数 2.0（冰弱于火，1GU 冰耗 2GU 火）</li>
 *   <li>冻结附着 + 火触发：2.0 倍（FREEZE 复合附着视作冰元素，与原神一致）</li>
 * </ul>
 *
 * <p>伤害放大由 {@code ElementalCombatHandler} 在伤害管线完成，本类只管三元组与表现。</p>
 */
public class MeltReaction extends ElementalReaction {
    /** 方向枚举对应三种注册实例 */
    public enum Direction {
        CRYO_AURA_PYRO_TRIGGER,
        PYRO_AURA_CRYO_TRIGGER,
        FREEZE_AURA_PYRO_TRIGGER
    }

    public MeltReaction(Direction direction) {
        super(
            switch (direction) {
                case CRYO_AURA_PYRO_TRIGGER -> "melt_pyro_trigger";
                case PYRO_AURA_CRYO_TRIGGER -> "melt_cryo_trigger";
                case FREEZE_AURA_PYRO_TRIGGER -> "melt_pyro_on_freeze";
            },
            switch (direction) {
                case CRYO_AURA_PYRO_TRIGGER -> Element.CRYO;
                case PYRO_AURA_CRYO_TRIGGER -> Element.PYRO;
                case FREEZE_AURA_PYRO_TRIGGER -> Element.FREEZE;
            },
            switch (direction) {
                case CRYO_AURA_PYRO_TRIGGER, FREEZE_AURA_PYRO_TRIGGER -> Element.PYRO;
                case PYRO_AURA_CRYO_TRIGGER -> Element.CRYO;
            },
            switch (direction) {
                case CRYO_AURA_PYRO_TRIGGER, FREEZE_AURA_PYRO_TRIGGER -> 0.5;
                case PYRO_AURA_CRYO_TRIGGER -> 2.0;
            },
            switch (direction) {
                case CRYO_AURA_PYRO_TRIGGER, FREEZE_AURA_PYRO_TRIGGER -> 2.0;
                case PYRO_AURA_CRYO_TRIGGER -> 1.5;
            },
            Type.AMPLIFYING
        );
    }

    @Override
    protected void onReact(LivingEntity target, double reducedGauge, @Nullable LivingEntity origin) {
        if (target.level() instanceof ServerLevel level) {
            // 冰火交汇：蒸汽 + 残雪
            level.sendParticles(ParticleTypes.CLOUD,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                    10, 0.3, 0.3, 0.3, 0.02);
            level.sendParticles(ParticleTypes.SNOWFLAKE,
                    target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(),
                    8, 0.3, 0.3, 0.3, 0.03);
            level.playSound(null, target.blockPosition(),
                    SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 0.6f, 1.4f);
        }
    }
}
