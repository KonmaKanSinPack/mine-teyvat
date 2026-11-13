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
 * 燃烧（Burning）：草附着 + 火触发（单向）。
 *
 * <p>DoT 直接复用 MC 原版点燃（用户决策：不造自定义 DoT 调度器），
 * 点燃时长随反应消耗的元素量增长。同时挂 BURNING 复合附着
 * （母体火，绕过 ICD），提供图标与环绕火星——附着存续时间与点燃时长
 * 各自独立衰减，不做强同步（骨架简化）。</p>
 */
public class BurningReaction extends ElementalReaction {
    /** 每点消耗量值对应的点燃秒数 */
    private static final float SECONDS_PER_GAUGE = 4.0f;

    public BurningReaction() {
        super("burning", Element.DENDRO, Element.PYRO, 1.0, 1.0, Type.TRANSFORMATIVE);
    }

    @Override
    protected void onReact(LivingEntity target, double reducedGauge, @Nullable LivingEntity origin) {
        // 原版点燃承担 DoT
        target.igniteForSeconds((float) (reducedGauge * SECONDS_PER_GAUGE));

        // BURNING 复合附着：驱动燃烧图标与环绕火星
        ElementalData data = target.getData(ModAttachments.ELEMENTAL_DATA);
        data.applyAura(Element.BURNING, reducedGauge, "reaction:burning");
        target.setData(ModAttachments.ELEMENTAL_DATA, data);

        // 燃烧反应点燃目标脚下的地面（这是反应产物，不是挂火就有）
        com.teyvat.elements.logic.ElementSystem.igniteGroundAround(target);

        if (target.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.FLAME,
                    target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                    20, 0.35, 0.5, 0.35, 0.05);
            level.playSound(null, target.blockPosition(),
                    SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 0.9f, 0.9f);
        }
    }
}
