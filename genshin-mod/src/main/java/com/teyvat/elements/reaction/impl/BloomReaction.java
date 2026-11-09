package com.teyvat.elements.reaction.impl;

import org.jetbrains.annotations.Nullable;

import com.teyvat.elements.element.Element;
import com.teyvat.elements.entity.DendroCoreEntity;
import com.teyvat.elements.reaction.ElementalReaction;
import com.teyvat.elements.registry.ModEntities;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * 绽放（Bloom）：草 × 水，双向。
 *
 * <p>不造成直接伤害，在目标位置生成<b>草原核</b>（5 秒后爆炸的范围草伤种子）。
 * 草原核可被雷/火法杖提前引爆成超绽放/烈绽放（见 {@link DendroCoreEntity}）。
 * 这是"反应产生实体"的第一个例子：反应的产物进入战场而非立即结算。</p>
 */
public class BloomReaction extends ElementalReaction {
    /** @param dendroAura true = 草附着被水触发；false = 水附着被草触发 */
    public BloomReaction(boolean dendroAura) {
        super(
            dendroAura ? "bloom_hydro_trigger" : "bloom_dendro_trigger",
            dendroAura ? Element.DENDRO : Element.HYDRO,
            dendroAura ? Element.HYDRO : Element.DENDRO,
            1.0,
            1.0,
            Type.TRANSFORMATIVE
        );
    }

    @Override
    protected void onReact(LivingEntity target, double reducedGauge, @Nullable LivingEntity origin) {
        if (!(target.level() instanceof ServerLevel level)) return;

        DendroCoreEntity core = new DendroCoreEntity(ModEntities.DENDRO_CORE.get(), level);
        core.setPos(target.getX(), target.getY() + 0.5, target.getZ());
        core.setGauge(reducedGauge);
        level.addFreshEntity(core);

        level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                target.getX(), target.getY() + 0.8, target.getZ(),
                12, 0.3, 0.3, 0.3, 0.05);
        level.playSound(null, target.blockPosition(),
                SoundEvents.CROP_PLANTED, SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}
