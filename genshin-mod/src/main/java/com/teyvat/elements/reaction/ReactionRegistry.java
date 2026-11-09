package com.teyvat.elements.reaction;

import java.util.ArrayList;
import java.util.List;

import com.teyvat.elements.element.Element;
import com.teyvat.elements.reaction.impl.BloomReaction;
import com.teyvat.elements.reaction.impl.BurningReaction;
import com.teyvat.elements.reaction.impl.CrystallizeReaction;
import com.teyvat.elements.reaction.impl.ElectroChargedReaction;
import com.teyvat.elements.reaction.impl.FrozenReaction;
import com.teyvat.elements.reaction.impl.MeltReaction;
import com.teyvat.elements.reaction.impl.OverloadedReaction;
import com.teyvat.elements.reaction.impl.QuickenReaction;
import com.teyvat.elements.reaction.impl.SpreadAggravateReaction;
import com.teyvat.elements.reaction.impl.SuperconductReaction;
import com.teyvat.elements.reaction.impl.SwirlReaction;
import com.teyvat.elements.reaction.impl.VaporizeReaction;

/**
 * 反应注册表（骨架版：静态列表 + 线性匹配）。
 *
 * <p>为什么先不用 NeoForge Registry：骨架只有几个反应，静态列表零开销、零样板；
 * 当反应数量上到两位数、或需要数据包/附属 mod 注册新反应时，再迁移到
 * 自定义 Registry（参考 SevenElements 的做法，自行实现）。</p>
 */
public final class ReactionRegistry {
    private static final List<ElementalReaction> REACTIONS = new ArrayList<>();

    /** mod 构造时调用一次 */
    public static void bootstrap() {
        // 蒸发的两个方向量值系数与倍率不同，注册为两个实例
        register(new VaporizeReaction(true));   // 水附着 + 火触发：1.5 倍
        register(new VaporizeReaction(false));  // 火附着 + 水触发：2.0 倍
        register(new OverloadedReaction(false)); // 火附着 + 雷触发
        register(new OverloadedReaction(true));  // 雷附着 + 火触发
        register(new FrozenReaction(true));      // 水附着 + 冰触发
        register(new FrozenReaction(false));     // 冰附着 + 水触发
        register(new ElectroChargedReaction(true));  // 水附着 + 雷触发
        register(new ElectroChargedReaction(false)); // 雷附着 + 水触发
        // 扩散：风 × 四种可附着元素
        register(new SwirlReaction(Element.PYRO));
        register(new SwirlReaction(Element.HYDRO));
        register(new SwirlReaction(Element.ELECTRO));
        register(new SwirlReaction(Element.CRYO));
        // 融化：三个方向（含冻结附着视作冰）
        register(new MeltReaction(MeltReaction.Direction.CRYO_AURA_PYRO_TRIGGER));
        register(new MeltReaction(MeltReaction.Direction.PYRO_AURA_CRYO_TRIGGER));
        register(new MeltReaction(MeltReaction.Direction.FREEZE_AURA_PYRO_TRIGGER));
        // 燃烧：草附着 + 火触发（单向）
        register(new BurningReaction());
        // 绽放：草×水双向，生成草原核
        register(new BloomReaction(true));
        register(new BloomReaction(false));
        // 激化体系：原激化（草×雷双向）+ 超激化/蔓激化（命中激化目标增伤）
        register(new QuickenReaction(true));
        register(new QuickenReaction(false));
        register(new SpreadAggravateReaction(true));
        register(new SpreadAggravateReaction(false));
        // 超导：冰×雷双向，AoE + 减物抗
        register(new SuperconductReaction(true));
        register(new SuperconductReaction(false));
        // 结晶：岩 × 四种可附着元素
        register(new CrystallizeReaction(Element.PYRO));
        register(new CrystallizeReaction(Element.HYDRO));
        register(new CrystallizeReaction(Element.ELECTRO));
        register(new CrystallizeReaction(Element.CRYO));
    }

    public static void register(ElementalReaction reaction) {
        REACTIONS.add(reaction);
    }

    /**
     * 查找附着系统中可直接触发的反应（剧变/控制类），未命中返回 null。
     * 增幅反应不在此返回——它们只能由伤害管线触发（见 findAmplifying）。
     */
    public static ElementalReaction find(Element aura, Element trigger) {
        for (ElementalReaction r : REACTIONS) {
            if (!r.isAmplifying() && r.auraElement() == aura && r.triggerElement() == trigger) return r;
        }
        return null;
    }

    /** 查找增幅反应（仅伤害事件中使用），未命中返回 null */
    public static ElementalReaction findAmplifying(Element aura, Element trigger) {
        for (ElementalReaction r : REACTIONS) {
            if (r.isAmplifying() && r.auraElement() == aura && r.triggerElement() == trigger) return r;
        }
        return null;
    }

    private ReactionRegistry() {}
}
