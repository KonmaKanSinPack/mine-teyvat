package com.teyvat.elements.entity;

import com.teyvat.elements.element.Element;
import com.teyvat.elements.logic.ElementSystem;
import com.teyvat.elements.registry.ModEntities;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

/**
 * 原神箭矢：命中生物时，若弓带元素则施加对应元素（元素弓的入口）。
 * 物理弓的箭就是普通箭（PHYSICAL 时不做任何元素处理）。
 */
public class GenshinArrowEntity extends AbstractArrow {
    private Element element = Element.PHYSICAL;
    private double gauge = 0.0;

    public GenshinArrowEntity(EntityType<? extends GenshinArrowEntity> type, Level level) {
        super(type, level);
    }

    public GenshinArrowEntity(LivingEntity shooter, Level level, Element element, double gauge) {
        super(ModEntities.GENSHIN_ARROW.get(), shooter, level);
        this.element = element;
        this.gauge = gauge;
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        // 元素箭：命中生物时施加元素（物理箭跳过）
        if (element != Element.PHYSICAL && gauge > 0
                && hitResult.getEntity() instanceof LivingEntity target
                && getOwner() instanceof LivingEntity owner) {
            ElementSystem.applyElement(target, element, gauge, "arrow", owner);
        }
    }

    /** 箭矢不可回收（防复制） */
    @Override
    protected ItemStack getPickupItem() {
        return ItemStack.EMPTY;
    }
}
