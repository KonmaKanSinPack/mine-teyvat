package com.teyvat.elements.reaction.impl;

import org.jetbrains.annotations.Nullable;

import com.teyvat.elements.element.Element;
import com.teyvat.elements.reaction.ElementalReaction;
import com.teyvat.elements.registry.ModItems;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * 结晶（Crystallize）：岩 × 火/水/雷/冰附着。
 *
 * <p>岩不能附着只触发，四个方向各一个实例。产物是<b>结晶片</b>掉落物：
 * 反应量值写进物品的 custom_data，玩家拾取时转化为吸收心（MC 原生的护盾机制）。
 * 简化：原神结晶盾对同元素伤害有 250% 吸收加成，骨架期不做元素特化。</p>
 */
public class CrystallizeReaction extends ElementalReaction {
    /** @param auraElement 被结晶的附着元素（火/水/雷/冰之一） */
    public CrystallizeReaction(Element auraElement) {
        super(
            "crystallize_" + auraElement.id(),
            auraElement,
            Element.GEO,
            1.0,
            1.0,
            Type.TRANSFORMATIVE
        );
        if (!auraElement.canBeAura() || auraElement == Element.GEO) {
            throw new IllegalArgumentException("结晶的附着元素必须是可附着的火/水/雷/冰");
        }
    }

    @Override
    protected void onReact(LivingEntity target, double reducedGauge, @Nullable LivingEntity origin) {
        if (!(target.level() instanceof ServerLevel level)) return;

        // 结晶片掉落物，量值写入 custom_data（拾取时转成吸收心）
        ItemStack shard = new ItemStack(ModItems.CRYSTAL_SHARD.get());
        var tag = new net.minecraft.nbt.CompoundTag();
        tag.putDouble("gauge", reducedGauge);
        shard.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        ItemEntity drop = new ItemEntity(level,
                target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(), shard);
        drop.setDefaultPickUpDelay(); // 10 tick 拾取延迟，防止生成瞬间被吸走
        level.addFreshEntity(drop);

        level.sendParticles(ParticleTypes.FIREWORK,
                target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                12, 0.3, 0.4, 0.3, 0.05);
        level.playSound(null, target.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.9f, 1.2f);
    }
}
