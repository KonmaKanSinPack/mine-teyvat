package com.teyvat.elements.reaction.impl;

import org.jetbrains.annotations.Nullable;

import com.teyvat.elements.TeyvatElements;
import com.teyvat.elements.data.ElementalData;
import com.teyvat.elements.data.ModAttachments;
import com.teyvat.elements.element.Element;
import com.teyvat.elements.reaction.ElementalReaction;
import com.teyvat.elements.registry.ModAttributes;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.phys.AABB;

/**
 * 超导（Superconduct）：冰 × 雷，双向。
 *
 * <p>范围冰伤 + 给所有命中目标挂"物理抗性 -40%"的时限 debuff
 * （持续 量值×8 秒）。减抗本身不造成伤害增益——它放大的是<b>之后的物理攻击</b>，
 * 由 {@code ResistanceHandler} 在伤害结算时读取属性完成。</p>
 */
public class SuperconductReaction extends ElementalReaction {
    /** 减抗修改器与 debuff 记录的统一 ID */
    public static final ResourceLocation DEBUFF_ID =
            ResourceLocation.fromNamespaceAndPath(TeyvatElements.MOD_ID, "superconduct");
    public static final double RESISTANCE_SHRED = -0.4;
    private static final double SECONDS_PER_GAUGE = 8.0;
    private static final double AOE_RADIUS = 2.0;
    private static final double DAMAGE_PER_GAUGE = 3.0;

    /** @param cryoAura true = 冰附着被雷触发；false = 雷附着被冰触发 */
    public SuperconductReaction(boolean cryoAura) {
        super(
            cryoAura ? "superconduct_electro_trigger" : "superconduct_cryo_trigger",
            cryoAura ? Element.CRYO : Element.ELECTRO,
            cryoAura ? Element.ELECTRO : Element.CRYO,
            1.0,
            1.0,
            Type.TRANSFORMATIVE
        );
    }

    @Override
    protected void onReact(LivingEntity target, double reducedGauge, @Nullable LivingEntity origin) {
        if (!(target.level() instanceof ServerLevel level)) return;

        float damage = (float) (reducedGauge * DAMAGE_PER_GAUGE);
        long durationTicks = (long) (reducedGauge * SECONDS_PER_GAUGE * 20);

        AABB box = target.getBoundingBox().inflate(AOE_RADIUS);
        for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (origin != null) {
                nearby.hurt(target.damageSources().indirectMagic(origin, origin), damage);
            } else {
                nearby.hurt(target.damageSources().magic(), damage);
            }
            applyDebuff(nearby, durationTicks);
        }

        level.sendParticles(ParticleTypes.SNOWFLAKE,
                target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                14, 0.4, 0.4, 0.4, 0.04);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                14, 0.4, 0.4, 0.4, 0.06);
        level.playSound(null, target.blockPosition(),
                SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.4f, 1.2f);
    }

    /** 给目标挂减抗（修改器瞬态，时限记录在 Attachment 的 debuffs 里，由 tick 到期移除） */
    public static void applyDebuff(LivingEntity entity, long durationTicks) {
        AttributeInstance resistance = entity.getAttribute(ModAttributes.PHYSICAL_RESISTANCE);
        if (resistance != null) {
            resistance.addOrUpdateTransientModifier(new AttributeModifier(
                    DEBUFF_ID, RESISTANCE_SHRED, AttributeModifier.Operation.ADD_VALUE));
        }
        ElementalData data = entity.getData(ModAttachments.ELEMENTAL_DATA);
        data.setDebuff(DEBUFF_ID, entity.level().getGameTime() + durationTicks);
        entity.setData(ModAttachments.ELEMENTAL_DATA, data);
    }

    /** 到期移除减抗（由 ElementSystem tick 调用） */
    public static void removeDebuff(LivingEntity entity) {
        AttributeInstance resistance = entity.getAttribute(ModAttributes.PHYSICAL_RESISTANCE);
        if (resistance != null) {
            resistance.removeModifier(DEBUFF_ID);
        }
    }
}
