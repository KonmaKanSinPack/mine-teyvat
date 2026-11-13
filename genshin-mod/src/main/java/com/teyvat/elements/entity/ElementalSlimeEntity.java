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

    /** 攻击命中 → 给目标施加自身元素 */
    @Override
    protected void doHurtTarget(net.minecraft.world.entity.Entity target) {
        super.doHurtTarget(target);
        if (element() != Element.PHYSICAL && target instanceof LivingEntity living
                && !target.level().isClientSide) {
            ElementSystem.applyElement(living, element(), 1.0, "mob:slime", this);
        }
    }
}
