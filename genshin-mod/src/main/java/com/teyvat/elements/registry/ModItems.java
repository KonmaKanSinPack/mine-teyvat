package com.teyvat.elements.registry;

import com.teyvat.elements.TeyvatElements;
import com.teyvat.elements.TeyvatElements;
import com.teyvat.elements.element.Element;
import com.teyvat.elements.item.CrystalShardItem;
import com.teyvat.elements.item.ElementWandItem;
import com.teyvat.elements.item.GenshinWeaponItem;
import com.teyvat.elements.item.HomaStaffItem;
import com.teyvat.elements.item.ImprintItem;
import com.teyvat.elements.weapon.WeaponData;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 物品与创造模式页签注册。
 */
public final class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(TeyvatElements.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TeyvatElements.MOD_ID);

    /** 法杖基础属性：+3 攻击伤害（总 4 点），让增幅倍率（1.5/2.0x）有可感知的基数 */
    private static Item.Properties wandProps() {
        return new Item.Properties().stacksTo(1).attributes(ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 3.0,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .build());
    }

    public static final DeferredItem<ElementWandItem> PYRO_WAND = ITEMS.register("pyro_wand",
            () -> new ElementWandItem(Element.PYRO, 1.0, wandProps()));
    public static final DeferredItem<ElementWandItem> HYDRO_WAND = ITEMS.register("hydro_wand",
            () -> new ElementWandItem(Element.HYDRO, 1.0, wandProps()));
    public static final DeferredItem<ElementWandItem> ELECTRO_WAND = ITEMS.register("electro_wand",
            () -> new ElementWandItem(Element.ELECTRO, 1.0, wandProps()));
    public static final DeferredItem<ElementWandItem> CRYO_WAND = ITEMS.register("cryo_wand",
            () -> new ElementWandItem(Element.CRYO, 1.0, wandProps()));
    public static final DeferredItem<ElementWandItem> ANEMO_WAND = ITEMS.register("anemo_wand",
            () -> new ElementWandItem(Element.ANEMO, 1.0, wandProps()));
    public static final DeferredItem<ElementWandItem> DENDRO_WAND = ITEMS.register("dendro_wand",
            () -> new ElementWandItem(Element.DENDRO, 1.0, wandProps()));
    public static final DeferredItem<ElementWandItem> GEO_WAND = ITEMS.register("geo_wand",
            () -> new ElementWandItem(Element.GEO, 1.0, wandProps()));

    /** 结晶片：结晶反应产物，拾取转护盾（不进背包） */
    public static final DeferredItem<CrystalShardItem> CRYSTAL_SHARD = ITEMS.register("crystal_shard",
            () -> new CrystalShardItem(new Item.Properties()));

    /** 突破石：武器升阶材料（铁砧右槽；当前创造页签直接给，掉落来源后续加） */
    public static final DeferredItem<Item> ASCENSION_STONE = ITEMS.register("ascension_stone",
            () -> new Item(new Item.Properties()));

    /** 精锻用杂矿：+1 级 */
    public static final DeferredItem<Item> ENHANCEMENT_ORE = ITEMS.register("enhancement_ore",
            () -> new Item(new Item.Properties()));
    /** 精锻用良矿：+2 级 */
    public static final DeferredItem<Item> ENHANCEMENT_ORE_FINE = ITEMS.register("enhancement_ore_fine",
            () -> new Item(new Item.Properties()));
    /** 精锻用魔矿：+10 级 */
    public static final DeferredItem<Item> ENHANCEMENT_ORE_MYSTIC = ITEMS.register("enhancement_ore_mystic",
            () -> new Item(new Item.Properties()));

    /** 魔晶矿方块物品 */
    public static final DeferredItem<BlockItem> MAGIC_CRYSTAL_ORE_ITEM = ITEMS.register("magic_crystal_ore",
            () -> new BlockItem(ModBlocks.MAGIC_CRYSTAL_ORE.get(), new Item.Properties()));

    // ---- 刻印五件套（生/死/时/空/理） ----
    public static final DeferredItem<ImprintItem> IMPRINT_FLOWER = ITEMS.register("imprint_flower",
            () -> new ImprintItem(ImprintItem.Slot.FLOWER, new Item.Properties()));
    public static final DeferredItem<ImprintItem> IMPRINT_PLUME = ITEMS.register("imprint_plume",
            () -> new ImprintItem(ImprintItem.Slot.PLUME, new Item.Properties()));
    public static final DeferredItem<ImprintItem> IMPRINT_SANDS = ITEMS.register("imprint_sands",
            () -> new ImprintItem(ImprintItem.Slot.SANDS, new Item.Properties()));
    public static final DeferredItem<ImprintItem> IMPRINT_GOBLET = ITEMS.register("imprint_goblet",
            () -> new ImprintItem(ImprintItem.Slot.GOBLET, new Item.Properties()));
    public static final DeferredItem<ImprintItem> IMPRINT_CIRCLET = ITEMS.register("imprint_circlet",
            () -> new ImprintItem(ImprintItem.Slot.CIRCLET, new Item.Properties()));

    /** 护摩之杖：火元素长枪（第一件真武器；+8 攻击、攻速 1.2、距离 +1，低生命 +50% 攻击） */
    public static final DeferredItem<HomaStaffItem> HOMA_STAFF = ITEMS.register("homa_staff",
            () -> new HomaStaffItem(new Item.Properties().stacksTo(1)
                    .component(ModComponents.WEAPON_DATA, WeaponData.DEFAULT)
                    .attributes(ItemAttributeModifiers.builder()
                            .add(Attributes.ATTACK_DAMAGE,
                                    new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 8.0,
                                            AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.MAINHAND)
                            .add(Attributes.ATTACK_SPEED,
                                    new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -2.8,
                                            AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.MAINHAND)
                            // 长枪手感：触及距离 +1
                            .add(Attributes.ENTITY_INTERACTION_RANGE,
                                    new AttributeModifier(
                                            ResourceLocation.fromNamespaceAndPath(
                                                    TeyvatElements.MOD_ID, "homa_reach"),
                                            1.0, AttributeModifier.Operation.ADD_VALUE),
                                    EquipmentSlotGroup.MAINHAND)
                            .build())));

    // ---- 原神武器（模型引自 Genshin Nature；养成/刻印共用 GenshinWeaponItem 基类） ----
    private record WeaponSpec(String id, Element element, double gauge,
                              double damage, double speedMod, double reach) {}

    private static final java.util.List<WeaponSpec> WEAPON_SPECS = java.util.List.of(
            // 单手剑：伤害 / 攻速 1.6 / 距离 0
            new WeaponSpec("dull_blade", Element.PHYSICAL, 0, 2.0, -2.4, 0),
            new WeaponSpec("silver_sword", Element.PHYSICAL, 0, 3.0, -2.4, 0),
            new WeaponSpec("prototype_rancour", Element.PHYSICAL, 0, 6.0, -2.4, 0),
            new WeaponSpec("festering_desire", Element.PHYSICAL, 0, 6.0, -2.4, 0),
            // 双手剑：高伤 / 攻速 1.0
            new WeaponSpec("waster_greatsword", Element.PHYSICAL, 0, 3.0, -3.0, 0),
            new WeaponSpec("old_mercs_pal", Element.PHYSICAL, 0, 5.0, -3.0, 0),
            new WeaponSpec("serpent_spine", Element.PHYSICAL, 0, 8.0, -3.0, 0),
            // 长柄：攻速 1.2 / 距离 +0.5（贯虹 +1）
            new WeaponSpec("iron_point", Element.PHYSICAL, 0, 3.0, -2.8, 0.5),
            new WeaponSpec("beginners_protector", Element.PHYSICAL, 0, 4.0, -2.8, 0.5),
            new WeaponSpec("blackcliff_pole", Element.PHYSICAL, 0, 6.0, -2.8, 0.5),
            new WeaponSpec("kitain_cross_spear", Element.PHYSICAL, 0, 6.0, -2.8, 0.5),
            new WeaponSpec("vortex_vanquisher", Element.GEO, 1.0, 9.0, -2.8, 1.0),
            // 法器：低面板，攻速 1.6
            new WeaponSpec("pocket_grimoire", Element.PHYSICAL, 0, 1.0, -2.4, 0),
            new WeaponSpec("apprentices_notes", Element.PHYSICAL, 0, 2.0, -2.4, 0),
            new WeaponSpec("skyward_atlas", Element.PHYSICAL, 0, 6.0, -2.4, 0),
            // 弓：攻速 1.6（骨架期按近战处理，拉弓管线后续单独做）
            new WeaponSpec("hunters_bow", Element.PHYSICAL, 0, 1.0, -2.4, 0),
            new WeaponSpec("skyward_harp", Element.PHYSICAL, 0, 7.0, -2.4, 0)
    );

    private static Item.Properties weaponProps(WeaponSpec spec) {
        var builder = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, spec.damage,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, spec.speedMod,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND);
        if (spec.reach > 0) {
            builder.add(Attributes.ENTITY_INTERACTION_RANGE,
                    new AttributeModifier(ResourceLocation.fromNamespaceAndPath(
                                    TeyvatElements.MOD_ID, "weapon_reach"),
                            spec.reach, AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND);
        }
        return new Item.Properties().stacksTo(1)
                .component(ModComponents.WEAPON_DATA, WeaponData.DEFAULT)
                .attributes(builder.build());
    }

    public static final java.util.List<DeferredItem<?>> GENSHIN_WEAPONS = new java.util.ArrayList<>();

    static {
        for (WeaponSpec spec : WEAPON_SPECS) {
            DeferredItem<GenshinWeaponItem> item = ITEMS.register(spec.id,
                    () -> new GenshinWeaponItem(spec.element, spec.gauge, weaponProps(spec)));
            GENSHIN_WEAPONS.add(item);
        }
    }

    public static final net.neoforged.neoforge.registries.DeferredHolder<CreativeModeTab, CreativeModeTab> TEYVAT_TAB =            CREATIVE_TABS.register("main", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.teyvat_elements"))
                    .icon(() -> new ItemStack(PYRO_WAND.get()))
                    .displayItems((params, output) -> {
                        output.accept(PYRO_WAND.get());
                        output.accept(HYDRO_WAND.get());
                        output.accept(ELECTRO_WAND.get());
                        output.accept(CRYO_WAND.get());
                        output.accept(ANEMO_WAND.get());
                        output.accept(DENDRO_WAND.get());
                        output.accept(GEO_WAND.get());
                        output.accept(CRYSTAL_SHARD.get());
                        output.accept(ASCENSION_STONE.get());
                        output.accept(ENHANCEMENT_ORE.get());
                        output.accept(ENHANCEMENT_ORE_FINE.get());
                        output.accept(ENHANCEMENT_ORE_MYSTIC.get());
                        output.accept(MAGIC_CRYSTAL_ORE_ITEM.get());
                        output.accept(HOMA_STAFF.get());
                        output.accept(IMPRINT_FLOWER.get());
                        output.accept(IMPRINT_PLUME.get());
                        output.accept(IMPRINT_SANDS.get());
                        output.accept(IMPRINT_GOBLET.get());
                        output.accept(IMPRINT_CIRCLET.get());
                        GENSHIN_WEAPONS.forEach(output::accept);
                    })
                    .build());

    private ModItems() {}
}
