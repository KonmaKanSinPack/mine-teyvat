package com.teyvat.elements.logic;

import com.teyvat.elements.TeyvatElements;
import com.teyvat.elements.data.ElementalData;
import com.teyvat.elements.data.ModAttachments;
import com.teyvat.elements.element.Element;
import com.teyvat.elements.element.ElementalApplication;
import com.teyvat.elements.network.ModNetworking;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * 碎冰（Shatter）：冻结目标受到重击时触发，解除冻结并追加物理伤害。
 *
 * <p>为什么不走 {@code ElementalReaction} 框架：碎冰的触发条件不是
 * "附着元素 × 触发元素"，而是伤害事件的性质（重击/爆炸）。它是反应家族里的
 * 事件驱动型特例，独立成处理器更诚实，强行套三元组框架反而扭曲。</p>
 *
 * <p>追加伤害直接叠加在事件伤害上（{@code event.setAmount}），而不是另发一次
 * {@code hurt}——这样碎冰伤害归属于触发它的那一击，且天然杜绝事件递归。</p>
 */
public final class ShatterHandler {
    /** 追加伤害倍率：冻结剩余量值 × 4 */
    public static final double DAMAGE_MULTIPLIER = 4.0;

    /** 注册在 NeoForge.EVENT_BUS */
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) return;
        if (!target.hasData(ModAttachments.ELEMENTAL_DATA)) return;
        if (!isHeavyHit(event.getSource())) return;

        ElementalData data = target.getData(ModAttachments.ELEMENTAL_DATA);
        ElementalApplication freeze = data.getAura(Element.FREEZE);
        if (freeze == null) return;

        // 追加物理伤害 = 冻结剩余量值 × 倍率（冻得越牢，碎得越痛）
        float bonus = (float) (freeze.gauge() * DAMAGE_MULTIPLIER);
        event.setAmount(event.getAmount() + bonus);

        // 解除冻结：移除附着、还原 AI/移速、通知客户端摘冰壳
        data.removeAura(Element.FREEZE);
        if (data.isEmpty()) {
            target.removeData(ModAttachments.ELEMENTAL_DATA);
        } else {
            target.setData(ModAttachments.ELEMENTAL_DATA, data);
        }
        FreezeController.update(target, false);
        ModNetworking.syncAura(target);

        if (target.level() instanceof ServerLevel level) {
            // 碎冰观感：冰方块碎裂粒子 + 玻璃破碎音
            level.sendParticles(
                    new net.minecraft.core.particles.BlockParticleOption(
                            ParticleTypes.BLOCK, Blocks.ICE.defaultBlockState()),
                    target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(),
                    30, 0.3, 0.5, 0.3, 0.15);
            level.playSound(null, target.blockPosition(),
                    SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0f, 1.1f);
        }

        Entity attacker = event.getSource().getEntity();
        TeyvatElements.LOGGER.info("Reaction [shatter] triggered on {} (+{} dmg)",
                target.getName().getString(), String.format("%.1f", bonus));
        if (attacker instanceof ServerPlayer player) {
            player.displayClientMessage(Component.translatable("reaction.teyvat_elements.shatter"), true);
        }
    }

    /**
     * 重击判定：爆炸，或玩家跳劈暴击（下落中、离地、近战直接命中）。
     * 岩元素触发碎冰留待岩系武器管线实现。
     */
    private static boolean isHeavyHit(DamageSource source) {
        if (source.is(DamageTypeTags.IS_EXPLOSION)) return true;

        Entity attacker = source.getEntity();
        return attacker instanceof Player player
                && source.getDirectEntity() == player  // 近战直接命中（排除弹射物）
                && player.fallDistance > 0.0F
                && !player.onGround();
    }

    private ShatterHandler() {}
}
