package com.teyvat.elements.registry;

import java.util.function.Supplier;

import com.teyvat.elements.TeyvatElements;
import com.teyvat.elements.entity.DendroCoreEntity;
import com.teyvat.elements.entity.ElementalSlimeEntity;
import com.teyvat.elements.entity.AbyssMageEntity;

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

    /** 注册元素史莱姆（实体类型决定元素，继承原版史莱姆 AI） */
    private static Supplier<EntityType<ElementalSlimeEntity>> registerSlime(String name) {
        return ENTITY_TYPES.register(name, () -> EntityType.Builder
                .<ElementalSlimeEntity>of(ElementalSlimeEntity::new, MobCategory.MONSTER)
                .sized(2.04f, 2.04f)
                .clientTrackingRange(10)
                .build("teyvat_elements:" + name));
    }

    public static final Supplier<EntityType<ElementalSlimeEntity>> PYRO_SLIME = registerSlime("pyro_slime");
    public static final Supplier<EntityType<ElementalSlimeEntity>> HYDRO_SLIME = registerSlime("hydro_slime");
    public static final Supplier<EntityType<ElementalSlimeEntity>> ELECTRO_SLIME = registerSlime("electro_slime");
    public static final Supplier<EntityType<ElementalSlimeEntity>> CRYO_SLIME = registerSlime("cryo_slime");

    /** 注册深渊法师（元素由实体类型决定） */
    private static Supplier<EntityType<AbyssMageEntity>> registerMage(String name) {
        return ENTITY_TYPES.register(name, () -> EntityType.Builder
                .<AbyssMageEntity>of(AbyssMageEntity::new, MobCategory.MONSTER)
                .sized(0.6f, 1.8f)
                .clientTrackingRange(10)
                .build("teyvat_elements:" + name));
    }

    public static final Supplier<EntityType<AbyssMageEntity>> PYRO_ABYSS_MAGE = registerMage("pyro_abyss_mage");
    public static final Supplier<EntityType<AbyssMageEntity>> HYDRO_ABYSS_MAGE = registerMage("hydro_abyss_mage");
    public static final Supplier<EntityType<AbyssMageEntity>> ELECTRO_ABYSS_MAGE = registerMage("electro_abyss_mage");
    public static final Supplier<EntityType<AbyssMageEntity>> CRYO_ABYSS_MAGE = registerMage("cryo_abyss_mage");
}
