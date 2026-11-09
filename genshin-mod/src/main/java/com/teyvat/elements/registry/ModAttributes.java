package com.teyvat.elements.registry;

import com.teyvat.elements.TeyvatElements;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 自定义属性注册。
 *
 * <p>物理抗性是本 mod 第一个自定义属性：默认 0，负值=易伤（超导的 -0.4 即承伤 +40%），
 * 正值=减伤。这是后续完整伤害公式（攻击×倍率×抗性×防御）的起点。</p>
 *
 * <p>声明为 {@link DeferredHolder} 而非 Supplier：{@code getAttribute} 等 API 要 Holder。</p>
 */
public final class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, TeyvatElements.MOD_ID);

    public static final DeferredHolder<Attribute, Attribute> PHYSICAL_RESISTANCE =
            ATTRIBUTES.register("physical_resistance", () ->
                    new RangedAttribute("attribute.teyvat_elements.physical_resistance", 0.0, -1.0, 0.9)
                            .setSyncable(true));

    /** 元素精通：反应收益系数 = 1 + EM×0.002（100 EM ≈ +20%） */
    public static final DeferredHolder<Attribute, Attribute> ELEMENTAL_MASTERY =
            ATTRIBUTES.register("elemental_mastery", () ->
                    new RangedAttribute("attribute.teyvat_elements.elemental_mastery", 0.0, 0.0, 2000.0)
                            .setSyncable(true));

    /** 暴击率：默认 5%（还原原神面板），暴击伤害 ×1.5 */
    public static final DeferredHolder<Attribute, Attribute> CRIT_RATE =
            ATTRIBUTES.register("crit_rate", () ->
                    new RangedAttribute("attribute.teyvat_elements.crit_rate", 0.05, 0.0, 1.0)
                            .setSyncable(true));

    private ModAttributes() {}
}
