package com.teyvat.elements.entity;

import com.teyvat.elements.element.Element;
import com.teyvat.elements.item.ElementWandItem;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * 草原核（Dendro Core）：绽放反应生成的延迟爆炸种子。
 *
 * <p>设计要点：</p>
 * <ul>
 *   <li>继承 Entity 而非 LivingEntity——刻意隔离元素伤害管线
 *       （LivingIncomingDamageEvent 只针对生物），草原核不是"活物"</li>
 *   <li>普通攻击免疫，只响应元素法杖：雷→超绽放（单体高伤）、火→烈绽放（大范围）</li>
 *   <li>noSave 瞬态战场实体：引信只有 5 秒，入存档没有价值</li>
 * </ul>
 */
public class DendroCoreEntity extends Entity {
    /** 引信：5 秒 */
    private static final int FUSE_TICKS = 100;

    private int fuse = FUSE_TICKS;
    /** 反应消耗的元素量，决定爆炸伤害 */
    private double gauge = 1.0;

    public DendroCoreEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public void setGauge(double gauge) {
        this.gauge = gauge;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;

        // 存续期间的呼吸粒子（提示"这颗种子快炸了"）
        if (level() instanceof ServerLevel serverLevel && tickCount % 5 == 0) {
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    getX(), getY() + 0.3, getZ(), 1, 0.15, 0.15, 0.15, 0.0);
        }

        if (--fuse <= 0) {
            explode(2.5, 4.0, ExplosionKind.BLOOM);
        }
    }

    /** 普通攻击免疫；只对元素法杖响应（超绽放/烈绽放） */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide) return false;
        if (!(source.getEntity() instanceof LivingEntity attacker)) return false;
        if (!(attacker.getMainHandItem().getItem() instanceof ElementWandItem wand)) return false;

        if (wand.element() == Element.ELECTRO) {
            // 超绽放：聚变小范围高伤
            explode(1.5, 10.0, ExplosionKind.HYPERBLOOM);
        } else if (wand.element() == Element.PYRO) {
            // 烈绽放：大范围爆炸
            explode(4.0, 8.0, ExplosionKind.BURGEON);
        }
        return false; // 无论哪种都不受常规伤害
    }

    private enum ExplosionKind { BLOOM, HYPERBLOOM, BURGEON }

    private void explode(double radius, double damagePerGauge, ExplosionKind kind) {
        if (!(level() instanceof ServerLevel level)) { discard(); return; }

        float damage = (float) (gauge * damagePerGauge);
        AABB box = getBoundingBox().inflate(radius);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, box)) {
            victim.hurt(damageSources().magic(), damage);
        }

        switch (kind) {
            case BLOOM -> {
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY(), getZ(),
                        30, radius * 0.4, radius * 0.4, radius * 0.4, 0.1);
                level.playSound(null, blockPosition(), SoundEvents.GRASS_BREAK, SoundSource.PLAYERS, 1.0f, 0.6f);
            }
            case HYPERBLOOM -> {
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, getX(), getY(), getZ(),
                        40, radius * 0.5, radius * 0.5, radius * 0.5, 0.15);
                level.playSound(null, blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.6f, 1.8f);
            }
            case BURGEON -> {
                level.sendParticles(ParticleTypes.EXPLOSION, getX(), getY(), getZ(),
                        2, 0.2, 0.2, 0.2, 0.0);
                level.sendParticles(ParticleTypes.FLAME, getX(), getY(), getZ(),
                        40, radius * 0.4, radius * 0.4, radius * 0.4, 0.1);
                level.playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.7f, 1.2f);
            }
        }
        discard();
    }

    @Override
    public boolean isPickable() {
        return true; // 可被选中和"攻击"（用于触发超/烈绽放）
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {}

    @Override
    protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {}

    @Override
    protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {}
}
