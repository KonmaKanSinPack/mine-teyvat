package com.teyvat.elements.registry;

import java.util.function.Supplier;

import com.teyvat.elements.TeyvatElements;
import com.teyvat.elements.entity.DendroCoreEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 实体注册。
 */
public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, TeyvatElements.MOD_ID);

    public static final Supplier<EntityType<DendroCoreEntity>> DENDRO_CORE =
            ENTITY_TYPES.register("dendro_core", () -> EntityType.Builder
                    .<DendroCoreEntity>of(DendroCoreEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(8)
                    // 瞬态战场实体：5 秒引信，入存档没有意义
                    .noSave()
                    .build("teyvat_elements:dendro_core"));

    /** 原神箭矢：弓放出的箭（命中可施加元素） */
    public static final Supplier<EntityType<com.teyvat.elements.entity.GenshinArrowEntity>> GENSHIN_ARROW =
            ENTITY_TYPES.register("genshin_arrow", () -> EntityType.Builder
                    .<com.teyvat.elements.entity.GenshinArrowEntity>of(
                            com.teyvat.elements.entity.GenshinArrowEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .noSave()
                    .build("teyvat_elements:genshin_arrow"));

    private ModEntities() {}
}
