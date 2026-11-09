package com.teyvat.elements.logic;

import com.teyvat.elements.data.ElementalData;
import com.teyvat.elements.data.ModAttachments;
import com.teyvat.elements.element.Element;
import com.teyvat.elements.item.ElementWandItem;
import com.teyvat.elements.network.ModNetworking;
import com.teyvat.elements.reaction.ElementalReaction;
import com.teyvat.elements.reaction.ReactionRegistry;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * 元素伤害管线：法杖近战命中的元素判定点。
 *
 * <p>为什么从 hurtEnemy 前移到伤害事件：增幅反应（蒸发）要放大<b>当次攻击的伤害</b>，
 * 必须在伤害结算前（{@code LivingIncomingDamageEvent} 可改 amount）介入；
 * hurtEnemy 是结算后才回调的，位置不对。法杖因此退化为纯"元素标记物"，
 * 命中判定统一收在这里。</p>
 *
 * <p>流程：ICD（吞整次元素命中）→ 增幅反应（放大伤害并消耗附着）→
 * 否则走 {@link ElementSystem#applyElement} 正常施加（剧变/控制/附着）。</p>
 */
public final class ElementalCombatHandler {

    /** 注册在 NeoForge.EVENT_BUS */
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        // 仅处理"主手持元素法杖的近战直接命中"；弹射物的 directEntity 是弹射物本身，天然排除
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        if (event.getSource().getDirectEntity() != attacker) return;
        if (attacker.level().isClientSide) return;
        if (!(attacker.getMainHandItem().getItem() instanceof ElementWandItem wand)) return;

        LivingEntity target = event.getEntity();
        Element element = wand.element();
        double gauge = wand.gauge();
        String sourceTag = wand.toString();
        long now = target.level().getGameTime();

        ElementalData data = target.getData(ModAttachments.ELEMENTAL_DATA);

        // 免疫检查在 ICD 之前：打免疫目标不该消耗附着窗口
        if (ElementSystem.isImmuneToElement(target, element)) return;

        // ICD：吞掉整次元素命中（不附着、不增幅；伤害本身不受影响）
        if (!element.bypassesIcd() && !data.icd(sourceTag).tryConsume(now)) return;

        // 增幅反应：消耗附着量值，放大当次伤害
        for (Element auraElement : data.auraElements()) {
            ElementalReaction reaction = ReactionRegistry.findAmplifying(auraElement, element);
            if (reaction == null) continue;

            double reduced = reaction.trigger(target, data, gauge, attacker);
            if (reduced <= 0) continue;

            // 增幅=乘算放大；加算=附加固定增伤（激化系）
            float newAmount = switch (reaction.type()) {
                case AMPLIFYING -> event.getAmount() * (float) reaction.multiplier();
                case ADDITIVE -> event.getAmount() + (float) (reduced * 2.0 * reaction.multiplier());
                default -> event.getAmount(); // TRANSFORMATIVE 不会走到这里（findAmplifying 已过滤）
            };
            event.setAmount(newAmount);
            ElementSystem.notifyReaction(attacker, reaction, target);

            // 触发量按系数折算消耗后的余量才可残留；不消耗附着的反应（激化系）无残留
            double leftover = reaction.consumesAura() ? gauge - reduced / reaction.coefficient() : 0;
            if (leftover > 1.0E-4 && element.canBeAura()) {
                data.applyAura(element, leftover * ElementSystem.AURA_TAX, sourceTag);
            }
            target.setData(ModAttachments.ELEMENTAL_DATA, data);
            ModNetworking.syncAura(target);
            FreezeController.update(target, data.getAura(Element.FREEZE) != null);
            return;
        }

        // 非增幅：走正常施加流程（剧变/控制反应、附着）；ICD 已判过，不重复消耗
        ElementSystem.applyElement(target, element, gauge, sourceTag, attacker, true);
    }

    private ElementalCombatHandler() {}
}
