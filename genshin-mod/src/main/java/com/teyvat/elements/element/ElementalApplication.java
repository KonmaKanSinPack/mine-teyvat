package com.teyvat.elements.element;

/**
 * 一次元素附着：记录当前剩余元素量（Gauge Units, GU）并按 tick 线性衰减。
 *
 * <p>衰减模型（骨架简化版）：附着总时长 = 附着的元素量 × 9.5 秒，
 * 即每 tick 衰减 {@code 初始量 / 190}（9.5s × 20 tick/s）。
 * 这是原神"弱附着约 9.5 秒消失"经验的线性近似；后续实现完整的
 * 分档衰减率（A/B/C 档）时应替换为按强度分档的衰减表。</p>
 */
public final class ElementalApplication {
    /** 原神经验值：每单位元素量的附着时长（秒） */
    public static final double SECONDS_PER_GAUGE = 9.5;
    public static final int TICKS_PER_SECOND = 20;

    private final Element element;
    private final String sourceTag;
    private double gauge;
    private double decayPerTick;

    public ElementalApplication(Element element, double gauge, String sourceTag) {
        this.element = element;
        this.sourceTag = sourceTag;
        this.refresh(gauge);
    }

    /** 重新附着同元素：量值取新旧较大者，衰减率随最新附着重置（与原神行为一致） */
    public void refresh(double newGauge) {
        this.gauge = Math.max(this.gauge, newGauge);
        this.decayPerTick = newGauge / (SECONDS_PER_GAUGE * TICKS_PER_SECOND);
    }

    public void tick() {
        this.gauge -= this.decayPerTick;
    }

    /**
     * 削减元素量（反应消耗或母体联动），返回实际削减量。
     */
    public double reduce(double amount) {
        double reduced = Math.min(this.gauge, Math.max(0, amount));
        this.gauge -= reduced;
        return reduced;
    }

    public boolean isEmpty() {
        return this.gauge <= 1.0E-4;
    }

    public Element element() {
        return element;
    }

    public double gauge() {
        return gauge;
    }

    public String sourceTag() {
        return sourceTag;
    }
}
