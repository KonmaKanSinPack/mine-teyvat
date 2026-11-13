package com.teyvat.elements.entity;

import com.teyvat.elements.element.Element;
import com.teyvat.elements.logic.ElementSystem;
import com.teyvat.elements.registry.ModEntities;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;

/**
 * 元素史莱姆：继承原版史莱姆的全部 AI 与分裂行为，
 * 按实体类型决定元素，攻击命中时给目标施加元素。
 *
 * <p>自元素免疫在 {@code ElementSystem.isImmuneToElement} 中统一处理
 * （火史莱姆挂不了火、冰史莱姆挂不了冰，与原神一致）。</p>
 */
public class ElementalSlimeEntity extends Slime {
    public ElementalSlimeEntity(EntityType<? extends Slime> type, Level level) {
        super(type, level);
    }

    /** 元素由实体类型决定（分裂产生的小史莱姆也走这里，元素不会丢） */
    public Element element() {
        EntityType<?> type = this.getType();
        if (type == ModEntities.PYRO_SLIME.get()) return Element.PYRO;
        if (type == ModEntities.HYDRO_SLIME.get()) return Element.HYDRO;
        if (type == ModEntities.ELECTRO_SLIME.get()) return Element.ELECTRO;
        if (type == ModEntities.CRYO_SLIME.get()) return Element.CRYO;
        return Element.PHYSICAL;
    }

    /** 攻击命中 → 给目标施加自身元素 + 家族技能 */
    @Override
    protected void dealDamage(LivingEntity target) {
        super.dealDamage(target);
        if (element() == Element.PHYSICAL || target.level().isClientSide) return;

        ElementSystem.applyElement(target, element(), 1.0, "mob:slime", this);

        switch (element()) {
            case ELECTRO -> {
                // 雷史莱姆电击链：主目标周围 3 格内其他生物一并挂雷
                for (LivingEntity nearby : level().getEntitiesOfClass(LivingEntity.class,
                        target.getBoundingBox().inflate(3.0))) {
                    if (nearby != target) {
                        ElementSystem.applyElement(nearby, Element.ELECTRO, 0.5, "mob:slime_chain", this);
                    }
                }
            }
            case HYDRO -> {
                // 水史莱姆溅射：主目标周围 2.5 格内其他生物挂水
                for (LivingEntity nearby : level().getEntitiesOfClass(LivingEntity.class,
                        target.getBoundingBox().inflate(2.5))) {
                    if (nearby != target) {
                        ElementSystem.applyElement(nearby, Element.HYDRO, 0.5, "mob:slime_splash", this);
                    }
                }
            }
            case CRYO -> {
                // 冰史莱姆命中附带 2 秒缓慢
                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 40, 0));
            }
            default -> {}
        }
    }

    /** 火史莱姆死亡自爆：半径 3 格火伤 + 挂火附着 */
    @Override
    public void die(net.minecraft.world.damagesource.DamageSource source) {
        super.die(source);
        if (element() == Element.PYRO && !level().isClientSide) {
            for (LivingEntity nearby : level().getEntitiesOfClass(LivingEntity.class,
                    getBoundingBox().inflate(3.0))) {
                nearby.hurt(damageSources().magic(), 4.0f);
                ElementSystem.applyElement(nearby, Element.PYRO, 0.5, "mob:slime_explode", this);
            }
            if (level() instanceof net.minecraft.server.level.ServerLevel level) {
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION,
                        getX(), getY() + 0.5, getZ(), 2, 0.3, 0.3, 0.3, 0.0);
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME,
                        getX(), getY() + 0.5, getZ(), 30, 1.0, 0.6, 1.0, 0.08);
                level.playSound(null, blockPosition(),
                        net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE.value(),
                        net.minecraft.sounds.SoundSource.HOSTILE, 0.6f, 1.3f);
            }
        }
    }

    /** 跳跃/落地/死亡的碎屑粒子改为元素色（原版固定绿色 ITEM_SLIME） */
    @Override
    protected net.minecraft.core.particles.ParticleOptions getParticleType() {
        return element().orbitParticle() != null
                ? element().orbitParticle()
                : net.minecraft.core.particles.ParticleTypes.ITEM_SLIME;
    }

    /** 冰史莱姆本体在水中时，周期性把周围水方块冻成冰（用户实测期望的行为） */
    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;

        // 常驻自身元素附着：图标与环绕粒子常驻显示
        // 不走 applyElement——史莱姆对自身元素免疫，附着由数据层直接维护（每 20 tick 检查一次）
        if (element() != Element.PHYSICAL && tickCount % 20 == 0) {
            com.teyvat.elements.data.ElementalData data =
                    this.getData(com.teyvat.elements.data.ModAttachments.ELEMENTAL_DATA);
            if (data.getAura(element()) == null) {
                data.applyAura(element(), 1.0, "mob:self");
                this.setData(com.teyvat.elements.data.ModAttachments.ELEMENTAL_DATA, data);
                com.teyvat.elements.network.ModNetworking.syncAura(this);
            }
        }

        // 冰史莱姆本体在水中时，周期性把周围水方块冻成冰
        if (element() == Element.CRYO && isInWater() && tickCount % 20 == 0) {
            ElementSystem.freezeWaterAround(this);
        }
    }
}
