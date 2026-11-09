package com.teyvat.elements.element;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * 元素定义（枚举 + 建造者）。
 *
 * <p>设计说明：一个元素的全部属性集中在一行声明里，新增元素只改本文件。
 * 关键语义来自原神的元素量理论（Elemental Gauge Theory），骨架阶段仅保留核心字段：</p>
 * <ul>
 *   <li>{@code canBeAura} —— 能否作为"附着元素"留在目标身上。风/岩在原神中只能触发反应，
 *       不能作为附着元素残留，因此为 {@code false}。</li>
 *   <li>{@code priority} —— 元素优先级，决定多个附着共存时的反应与展示顺序（数值小者先反应）。</li>
 *   <li>{@code linkedElement} —— 复合元素（燃烧/冻结/激化）与母体元素的量值联动：
 *       削减冻结量会同步削减冰量等。骨架阶段先建模字段，联动逻辑留待后续实现。</li>
 *   <li>{@code bypassesIcd} —— 复合元素由反应产生而非外部施加，不受附着冷却（ICD）限制。</li>
 * </ul>
 */
public enum Element {
    /** 物理不是元素，仅作占位（例如未来的伤害类型标识） */
    PHYSICAL(Settings.create().color(0xFFFFFF).priority(0).canBeAura(false)),
    PYRO(Settings.create().color(0xFF5A3C).priority(2).orbitParticle(ParticleTypes.FLAME)),
    HYDRO(Settings.create().color(0x3C8CFF).priority(2).orbitParticle(ParticleTypes.FALLING_WATER)),
    ANEMO(Settings.create().color(0x5AE0C8).priority(2).canBeAura(false)),
    ELECTRO(Settings.create().color(0xB15CFF).priority(2).orbitParticle(ParticleTypes.ELECTRIC_SPARK)),
    DENDRO(Settings.create().color(0x5AC83C).priority(2).orbitParticle(ParticleTypes.HAPPY_VILLAGER)),
    CRYO(Settings.create().color(0x9FD8E8).priority(2).orbitParticle(ParticleTypes.SNOWFLAKE)),
    GEO(Settings.create().color(0xE8B53C).priority(2).canBeAura(false)),

    // ---- 复合元素：由反应产生，绕过 ICD，量值与母体元素联动 ----
    BURNING(Settings.create().color(0xFF5A3C).priority(1).bypassesIcd(true).linkedTo(PYRO)
            .orbitParticle(ParticleTypes.FLAME)),
    FREEZE(Settings.create().color(0xB4FFFF).priority(2).bypassesIcd(true).linkedTo(CRYO)
            .orbitParticle(ParticleTypes.SNOWFLAKE)),
    QUICKEN(Settings.create().color(0x01E858).priority(2).bypassesIcd(true).linkedTo(DENDRO)
            .orbitParticle(ParticleTypes.HAPPY_VILLAGER));

    private final Settings settings;

    Element(Settings settings) {
        this.settings = settings;
    }

    public int color() {
        return settings.color;
    }

    public int priority() {
        return settings.priority;
    }

    public boolean canBeAura() {
        return settings.canBeAura;
    }

    public boolean bypassesIcd() {
        return settings.bypassesIcd;
    }

    /** 母体元素；非复合元素返回自身 */
    public Element linkedElement() {
        return settings.linkedElement != null ? settings.linkedElement : this;
    }

    public String id() {
        return name().toLowerCase();
    }

    /** 附着期间环绕实体的粒子；为 null 表示不生成 */
    public @org.jetbrains.annotations.Nullable SimpleParticleType orbitParticle() {
        return settings.orbitParticle;
    }

    public static final class Settings {
        private int color = 0xFFFFFF;
        private int priority = 2;
        private boolean canBeAura = true;
        private boolean bypassesIcd = false;
        private Element linkedElement = null;
        private SimpleParticleType orbitParticle = null;

        private Settings() {}

        public static Settings create() {
            return new Settings();
        }

        /** 伤害数字与粒子的主题色（RGB） */
        public Settings color(int color) {
            this.color = color;
            return this;
        }

        public Settings priority(int priority) {
            this.priority = priority;
            return this;
        }

        /** 能否作为附着元素残留在目标身上 */
        public Settings canBeAura(boolean canBeAura) {
            this.canBeAura = canBeAura;
            return this;
        }

        /** 是否绕过附着冷却（反应产生的复合元素应为 true） */
        public Settings bypassesIcd(boolean bypassesIcd) {
            this.bypassesIcd = bypassesIcd;
            return this;
        }

        /** 复合元素声明其母体元素 */
        public Settings linkedTo(Element parent) {
            this.linkedElement = parent;
            return this;
        }

        /** 附着期间环绕目标实体的粒子 */
        public Settings orbitParticle(SimpleParticleType particle) {
            this.orbitParticle = particle;
            return this;
        }
    }
}
