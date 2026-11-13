package com.teyvat.elements.entity;

import com.teyvat.elements.element.Element;
import com.teyvat.elements.item.ElementWandItem;
import com.teyvat.elements.registry.ModEntities;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.level.Level;

/**
 * 深渊法师：带元素护盾的远程法师怪。
 *
 * <p>护盾机制（还原原神）：20 点盾值，有盾不掉血；克制元素命中盾伤 ×2
 * （火盾怕水、水盾怕雷、雷盾怕冰、冰盾怕火——四元素克制环）。
 * 盾破后眩晕 5 秒且永久破盾，此时才能正常击杀。</p>
 *
 * <p>远程攻击复用 {@link GenshinArrowEntity}（弹带法师元素，命中挂元素）。</p>
 */
public class AbyssMageEntity extends AbstractIllager implements RangedAttackMob {
    public static final int MAX_SHIELD = 20;
    /** 破盾后眩晕时长：5 秒 */
    private static final int STUN_TICKS = 100;

    private int shield = MAX_SHIELD;
    private long stunUntil;

    public AbyssMageEntity(EntityType<? extends AbyssMageEntity> type, Level level) {
        super(type, level);
    }

    /** 袭击庆祝音（Raider 接口要求，法师不参战但必须有实现） */
    @Override
    public net.minecraft.sounds.SoundEvent getCelebrateSound() {
        return net.minecraft.sounds.SoundEvents.ILLUSIONER_AMBIENT;
    }

    /** 法师不参与袭击，袭击 buff 为空实现 */
    @Override
    public void applyRaidBuffs(net.minecraft.server.level.ServerLevel level, int count, boolean arg2) {}

    /** 元素由实体类型决定 */
    public Element element() {
        EntityType<?> type = getType();
        if (type == ModEntities.PYRO_ABYSS_MAGE.get()) return Element.PYRO;
        if (type == ModEntities.HYDRO_ABYSS_MAGE.get()) return Element.HYDRO;
        if (type == ModEntities.ELECTRO_ABYSS_MAGE.get()) return Element.ELECTRO;
        if (type == ModEntities.CRYO_ABYSS_MAGE.get()) return Element.CRYO;
        return Element.PHYSICAL;
    }

    /** 破盾克制元素：火盾怕水、水盾怕雷、雷盾怕冰、冰盾怕火 */
    public Element counterElement() {
        return switch (element()) {
            case PYRO -> Element.HYDRO;
            case HYDRO -> Element.ELECTRO;
            case ELECTRO -> Element.CRYO;
            case CRYO -> Element.PYRO;
            default -> Element.PHYSICAL;
        };
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        // 近身避让：玩家贴脸就退
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Player.class, 4.0f, 1.0, 1.2));
        // 远程元素弹：40 tick 一次，射程 12
        this.goalSelector.addGoal(3, new RangedAttackGoal(this, 1.0, 40, 12.0f));
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /** 有盾不掉血；克制元素命中盾伤 ×2；盾破眩晕 5 秒且永久破盾 */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide) return false;

        if (shield > 0) {
            Element attackElement = getAttackElement(source);
            double multiplier = attackElement == counterElement() ? 2.0 : 1.0;
            shield -= (int) (amount * multiplier);

            if (level() instanceof ServerLevel level) {
                level.sendParticles(ParticleTypes.CRIT,
                        getX(), getY() + getBbHeight() * 0.6, getZ(),
                        6, 0.3, 0.3, 0.3, 0.05);
                level.playSound(null, blockPosition(),
                        SoundEvents.SHIELD_BLOCK, SoundSource.HOSTILE, 0.8f, 1.0f);
            }

            if (shield <= 0) {
                shield = 0;
                stunUntil = level().getGameTime() + STUN_TICKS;
                if (level() instanceof ServerLevel level) {
                    level.sendParticles(ParticleTypes.EXPLOSION,
                            getX(), getY() + getBbHeight() * 0.6, getZ(),
                            1, 0.0, 0.0, 0.0, 0.0);
                    level.playSound(null, blockPosition(),
                            SoundEvents.SHIELD_BREAK, SoundSource.HOSTILE, 1.0f, 1.0f);
                }
            }
            return true;
        }
        return super.hurt(source, amount);
    }

    /** 从伤害来源提取攻击元素（元素武器/法杖命中可读） */
    private static Element getAttackElement(DamageSource source) {
        if (source.getEntity() instanceof LivingEntity attacker
                && attacker.getMainHandItem().getItem() instanceof ElementWandItem wand) {
            return wand.element();
        }
        return null;
    }

    /** 眩晕期间停 AI（破盾惩罚窗口） */
    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;
        boolean stunned = isStunned();
        if (stunned != isNoAi()) {
            setNoAi(stunned);
        }
    }

    public boolean isStunned() {
        return shield <= 0 && level().getGameTime() < stunUntil;
    }

    /** 远程元素弹：带法师元素，命中挂元素 */
    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        GenshinArrowEntity bolt = new GenshinArrowEntity(this, level(), element(), 1.0);
        bolt.setBaseDamage(4.0);
        double dx = target.getX() - getX();
        double dy = target.getY(0.5) - bolt.getY();
        double dz = target.getZ() - getZ();
        bolt.shoot(dx, dy, dz, 1.6f, 2.0f);
        level().addFreshEntity(bolt);
        level().playSound(null, blockPosition(),
                SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.HOSTILE, 1.0f, 1.0f);
    }
}
