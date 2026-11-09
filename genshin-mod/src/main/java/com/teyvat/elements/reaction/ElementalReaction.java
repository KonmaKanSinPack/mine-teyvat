package com.teyvat.elements.reaction;

import org.jetbrains.annotations.Nullable;

import com.teyvat.elements.data.ElementalData;
import com.teyvat.elements.element.Element;
import com.teyvat.elements.element.ElementalApplication;

import net.minecraft.world.entity.LivingEntity;

/**
 * 元素反应抽象基类。
 *
 * <p>一个反应由"附着元素 + 触发元素 + 量值消耗系数"三元组定义，
 * 子类只需在 {@link #onReact} 中实现表现效果（伤害/粒子/音效）。</p>
 *
 * <p>量值消耗规则（原神元素量理论）：触发元素以系数放大后消耗附着元素的量，
 * 即 {@code 附着消耗 = min(附着量, 触发量 × 系数)}；触发量按同比例折算消耗。
 * 例如蒸发（火触发水附着，系数 0.5）：1GU 火最多消耗 0.5GU 水。</p>
 */
public abstract class ElementalReaction {
    /**
     * 反应类型（对应原神三类伤害反应）：
     * TRANSFORMATIVE 由附着系统直接触发（剧变/控制）；
     * AMPLIFYING 在伤害管线中乘算放大当次攻击（增幅）；
     * ADDITIVE 在伤害管线中给当次攻击加算固定增伤（激化系）。
     */
    public enum Type { TRANSFORMATIVE, AMPLIFYING, ADDITIVE }

    private final String id;
    private final Element auraElement;
    private final Element triggerElement;
    private final double coefficient;
    private final double multiplier;
    private final Type type;

    protected ElementalReaction(String id, Element auraElement, Element triggerElement,
                                double coefficient, double multiplier, Type type) {
        this.id = id;
        this.auraElement = auraElement;
        this.triggerElement = triggerElement;
        this.coefficient = coefficient;
        this.multiplier = multiplier;
        this.type = type;
    }

    public String id() { return id; }
    public Element auraElement() { return auraElement; }
    public Element triggerElement() { return triggerElement; }
    public double coefficient() { return coefficient; }
    public double multiplier() { return multiplier; }
    public boolean isAmplifying() { return type != Type.TRANSFORMATIVE; }

    public Type type() { return type; }

    /**
     * 触发时是否消耗附着元素的量值。激化系（超激化/蔓激化）为 false——
     * 激化状态存续期间持续受益，命中不消耗激化附着，这正是该体系的玩法核心。
     */
    public boolean consumesAura() { return true; }

    /**
     * 尝试在目标身上触发本反应。
     *
     * @return 本次反应实际消耗的附着量；<= 0 表示未触发
     */
    public final double trigger(LivingEntity target, ElementalData data, double triggerGauge,
                                @Nullable LivingEntity origin) {
        ElementalApplication aura = data.getAura(auraElement);
        if (aura == null || aura.isEmpty() || triggerGauge <= 0) return 0;

        // 不消耗附着的反应（激化系）只读取量值用于计算收益
        double reducedGauge = consumesAura()
                ? aura.reduce(triggerGauge * coefficient)
                : Math.min(aura.gauge(), triggerGauge * coefficient);
        if (reducedGauge <= 0) return 0;
        if (consumesAura() && aura.isEmpty()) data.removeAura(auraElement);

        onReact(target, scaleByMastery(reducedGauge, origin), origin);
        return reducedGauge;
    }

    /**
     * 元素精通加成：反应表现（伤害/时长等）按 1 + EM×0.002 缩放。
     * 统一挂在 trigger() 里，所有反应子类零改动获得精通收益。
     */
    private static double scaleByMastery(double reducedGauge, @Nullable LivingEntity origin) {
        if (origin == null) return reducedGauge;
        var mastery = origin.getAttribute(com.teyvat.elements.registry.ModAttributes.ELEMENTAL_MASTERY);
        if (mastery == null || mastery.getValue() <= 0) return reducedGauge;
        return reducedGauge * (1.0 + mastery.getValue() * 0.002);
    }

    /** 反应表现钩子：伤害、粒子、音效等。骨架阶段在服务端执行 */
    protected abstract void onReact(LivingEntity target, double reducedGauge, @Nullable LivingEntity origin);
}
