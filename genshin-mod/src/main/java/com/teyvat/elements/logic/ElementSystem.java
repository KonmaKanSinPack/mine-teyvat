package com.teyvat.elements.logic;

import org.jetbrains.annotations.Nullable;

import com.teyvat.elements.TeyvatElements;
import com.teyvat.elements.data.ElementalData;
import com.teyvat.elements.data.ModAttachments;
import com.teyvat.elements.element.Element;
import com.teyvat.elements.network.ModNetworking;
import com.teyvat.elements.reaction.ElementalReaction;
import com.teyvat.elements.reaction.ReactionRegistry;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.List;

/**
 * 元素系统对外入口与每 tick 衰减驱动。
 *
 * <p>所有元素施加都必须走 {@link #applyElement}，它统一处理三件事：
 * ICD → 反应判定 → 残留附着（含附着税）。绕过它直接改 Attachment 会丢规则。</p>
 */
public final class ElementSystem {
    /** 附着税：新元素作为附着残留时量值打 8 折（原神元素量理论的 Aura Tax） */
    public static final double AURA_TAX = 0.8;

    private static final org.slf4j.Logger LOGGER =
            org.slf4j.LoggerFactory.getLogger(ElementSystem.class);

    /**
     * 向目标施加元素。
     *
     * @param target    目标生物
     * @param element   施加的元素
     * @param rawGauge  施加的元素量（1.0 ≈ 弱附着）
     * @param sourceTag 来源标签（决定 ICD 分组，通常用物品/技能 ID）
     * @param origin    施加者（用于反应伤害归属与反馈），可为 null
     */
    public static void applyElement(LivingEntity target, Element element, double rawGauge,
                                    String sourceTag, @Nullable LivingEntity origin) {
        applyElement(target, element, rawGauge, sourceTag, origin, false);
    }

    /**
     * @param icdAlreadyChecked true 表示调用方（伤害管线）已完成 ICD 判定，这里不再重复消耗
     */
    public static void applyElement(LivingEntity target, Element element, double rawGauge,
                                    String sourceTag, @Nullable LivingEntity origin,
                                    boolean icdAlreadyChecked) {
        if (target.level().isClientSide) return;

        // 0) 元素免疫：对某属性免伤的怪挂不了对应附着（火免挂不了火、冻免挂不了冰）。
        //    只挡附着不挡反应——火免怪带水附着被火打仍可蒸发（反应收益归触发方）。
        if (isImmuneToElement(target, element)) {
            LOGGER.info("applyElement [{}] blocked: {} is immune", element.id(), target.getName().getString());
            return;
        }

        ElementalData data = target.getData(ModAttachments.ELEMENTAL_DATA);
        long now = target.level().getGameTime();

        // 1) ICD：同一来源的连续附着受"3 次命中 / 2.5 秒"限制；复合元素豁免
        if (!icdAlreadyChecked && !element.bypassesIcd() && !data.icd(sourceTag).tryConsume(now)) {
            LOGGER.info("applyElement [{}] blocked by ICD (tag={})", element.id(), sourceTag);
            return;
        }

        // 2) 同元素重复附着：刷新为较强者，不触发反应、不吃附着税
        if (data.getAura(element) != null) {
            data.applyAura(element, rawGauge, sourceTag);
            target.setData(ModAttachments.ELEMENTAL_DATA, data);
            LOGGER.info("applyElement [{}] refreshed on {}", element.id(), target.getName().getString());
            // 刷新也改变了量值：同步客户端（图标透明度）并维护冻结状态
            ModNetworking.syncAura(target);
            FreezeController.update(target, data.getAura(Element.FREEZE) != null);
            return;
        }

        // 3) 与现有附着尝试反应（附着量被消耗的部分不再残留）
        double remainingGauge = rawGauge;
        boolean reacted = false;
        for (Element auraElement : data.auraElements()) {
            ElementalReaction reaction = ReactionRegistry.find(auraElement, element);
            if (reaction == null) continue;

            double reduced = reaction.trigger(target, data, remainingGauge, origin);
            if (reduced > 0) {
                reacted = true;
                notifyReaction(origin, reaction, target);
                // 触发量按系数折算消耗，余量才可能继续反应或残留
                remainingGauge -= reduced / reaction.coefficient();
                break; // 骨架：一次施加最多触发一个反应；完整版需按优先级遍历
            }
        }

        // 风/岩等不可附着元素既没触发反应也不会残留——给施加者一句提示，避免"打了没反应"的困惑
        if (!reacted && !element.canBeAura() && origin instanceof ServerPlayer player) {
            player.displayClientMessage(Component.translatable("apply.teyvat_elements.no_aura",
                    Component.translatable("element.teyvat_elements." + element.id())), true);
        }

        // 4) 余量残留为新附着（仅可附着元素，且过附着税）
        if (remainingGauge > 1.0E-4 && element.canBeAura()) {
            double auraGauge = remainingGauge * AURA_TAX;
            data.applyAura(element, auraGauge, sourceTag);
            LOGGER.info("applyElement [{}] applied aura {} GU on {}",
                    element.id(), String.format("%.2f", auraGauge), target.getName().getString());
            if (origin instanceof ServerPlayer player) {
                player.displayClientMessage(Component.translatable(
                        "apply.teyvat_elements.aura",
                        Component.translatable("element.teyvat_elements." + element.id()),
                        String.format("%.2f", auraGauge)), true);
            }
        }
        target.setData(ModAttachments.ELEMENTAL_DATA, data);
        // 冰元素碰到水 → 周围水方块结冰（环境交互）
        if (element == Element.CRYO && target.isInWater()) {
            freezeWaterAround(target);
        }
        // 注意：点燃地面是燃烧反应的产物（BurningReaction），不是挂火就点
        // 附着状态可能已变化（新增/刷新/反应消耗），推给追踪者刷新头顶图标
        ModNetworking.syncAura(target);
        // 冻结定身随 FREEZE 附着存续
        FreezeController.update(target, data.getAura(Element.FREEZE) != null);
    }

    /** 火附着：点燃目标脚下 2 格内的地面（空气下方为实心面则生成火焰）。
     *  仅供燃烧反应调用（BurningReaction.onReact）。 */
    public static void igniteGroundAround(LivingEntity target) {
        if (!(target.level() instanceof net.minecraft.server.level.ServerLevel level)) return;
        BlockPos center = target.blockPosition();
        int lit = 0;
        for (BlockPos pos : BlockPos.withinManhattan(center, 2, 1, 2)) {
            if (!level.getBlockState(pos).isAir()) continue;
            // 下方是实心面才能站住火（等价打火石逻辑）
            if (level.getBlockState(pos.below()).isSolidRender(level, pos.below())) {
                level.setBlock(pos, net.minecraft.world.level.block.Blocks.FIRE.defaultBlockState(), 3);
                lit++;
            }
        }
        if (lit > 0) {
            level.playSound(null, center, net.minecraft.sounds.SoundEvents.FIRECHARGE_USE,
                    net.minecraft.sounds.SoundSource.PLAYERS, 0.8f, 1.0f);
        }
    }

    /** 冰附着碰到水：把目标周围 2 格内的水方块替换为冰块。
     *  供挂冰（applyElement）与冰史莱姆本体 tick 共用。 */
    public static void freezeWaterAround(LivingEntity target) {
        if (!(target.level() instanceof net.minecraft.server.level.ServerLevel level)) return;
        BlockPos center = target.blockPosition();
        int frozen = 0;
        for (BlockPos pos : BlockPos.withinManhattan(center, 2, 1, 2)) {
            if (level.getBlockState(pos).getBlock() == net.minecraft.world.level.block.Blocks.WATER) {
                level.setBlock(pos, net.minecraft.world.level.block.Blocks.ICE.defaultBlockState(), 3);
                frozen++;
            }
        }
        if (frozen > 0) {
            level.playSound(null, center, net.minecraft.sounds.SoundEvents.GLASS_PLACE,
                    net.minecraft.sounds.SoundSource.BLOCKS, 0.8f, 1.2f);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.SNOWFLAKE,
                    center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5,
                    16, 1.5, 0.5, 1.5, 0.05);
        }
    }

    /** 雷击 → 雷附着（环境附着之一，走正常管线，雨天的感电连招由此成立） */
    public static void onLightningStrike(net.neoforged.neoforge.event.entity.EntityStruckByLightningEvent event) {
        if (event.getEntity() instanceof LivingEntity living && !living.level().isClientSide) {
            applyElement(living, Element.ELECTRO, 1.0, "env:lightning", null, true);
        }
    }

    /** 实体 tick：环境附着 + 驱动附着衰减 + 环绕粒子。注册在 NeoForge.EVENT_BUS */
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity living)) return;
        if (living.level().isClientSide) return;

        // 环境附着（原神的"潮湿"等状态）：水中的怪带水、着火的怪带火、细雪中的怪带冰，
        // 每秒补一次。绕过 ICD（环境不是攻击），走 applyElement 让环境元素正常参与反应
        // （雷打水面的怪会感电、火里的怪被冰打会融化——环境交互自然涌现）
        if (living.tickCount % 20 == 0 && living.isAlive() && !living.isSpectator()) {
            if (living.isInWaterOrRain()) {
                applyElement(living, Element.HYDRO, 0.5, "env:water", null, true);
            }
            if (living.isInLava() || living.isOnFire()) {
                applyElement(living, Element.PYRO, 0.5, "env:fire", null, true);
            }
            if (living.isInPowderSnow) {
                applyElement(living, Element.CRYO, 0.5, "env:snow", null, true);
            }
        }

        if (!living.hasData(ModAttachments.ELEMENTAL_DATA)) return;

        ElementalData data = living.getData(ModAttachments.ELEMENTAL_DATA);

        // 环绕粒子：每 4 tick 一轮，控制发包量；角度随时间旋转
        if (living.tickCount % 4 == 0 && living.level() instanceof ServerLevel serverLevel) {
            List<Element> elements = List.copyOf(data.auraElements());
            double baseAngle = living.level().getGameTime() * 0.25;
            double radius = living.getBbWidth() * 0.5 + 0.35;
            double y = living.getY() + living.getBbHeight() * 0.5;
            for (int i = 0; i < elements.size(); i++) {
                SimpleParticleType particle = elements.get(i).orbitParticle();
                if (particle == null) continue;
                double angle = baseAngle + i * (2 * Math.PI / elements.size());
                serverLevel.sendParticles(particle,
                        living.getX() + Math.cos(angle) * radius, y,
                        living.getZ() + Math.sin(angle) * radius,
                        1, 0, 0.02, 0, 0.0);
            }
        }

        boolean auraRemoved = data.tick();

        // 时限 debuff 到期移除（超导减抗等）
        var superconductId = com.teyvat.elements.reaction.impl.SuperconductReaction.DEBUFF_ID;
        Long superconductExpiry = data.debuffExpiry(superconductId);
        if (superconductExpiry != null && living.level().getGameTime() >= superconductExpiry) {
            com.teyvat.elements.reaction.impl.SuperconductReaction.removeDebuff(living);
            data.removeDebuff(superconductId);
        } else if (superconductExpiry != null && living.tickCount % 10 == 0
                && living.level() instanceof ServerLevel debuffLevel) {
            // 减抗存续期间的视觉提示：紫色碎裂粒子（"防御被削了"的观感）
            debuffLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANTED_HIT,
                    living.getX(), living.getY() + living.getBbHeight() * 0.7, living.getZ(),
                    2, 0.25, 0.25, 0.25, 0.02);
        }

        // 冻结期间每 tick 清零水平位移（NoAi 管 AI 移动，这条管击退等物理位移）
        if (data.getAura(Element.FREEZE) != null && !(living instanceof net.minecraft.world.entity.player.Player)) {
            FreezeController.zeroHorizontalMotion(living);
        }
        if (data.isEmpty()) {
            // 附着清空后摘除 Attachment，避免空数据长期挂在实体上
            living.removeData(ModAttachments.ELEMENTAL_DATA);
            ModNetworking.syncAura(living); // 通知客户端摘掉图标
            FreezeController.update(living, false); // 保险：清空时必定解冻
        } else if (auraRemoved) {
            ModNetworking.syncAura(living);
            // FREEZE 衰减完时解冻（其它附着衰减不影响定身）
            FreezeController.update(living, data.getAura(Element.FREEZE) != null);
        }
    }

    /**
     * 元素免疫判定：把元素映射到原版伤害类型，复用原版的免疫规则
     * （火免/冻免是 EntityType 级属性，涵盖下界生物与冰雪生物）。
     */
    /** 元素免疫判定：把元素映射到原版伤害类型，复用原版的免疫规则
     * （火免/冻免是 EntityType 级属性，涵盖下界生物与冰雪生物）。
     * 伤害管线（ElementalCombatHandler）也需要用它，故为 public。
     */
    public static boolean isImmuneToElement(LivingEntity target, Element element) {
        // 元素史莱姆免疫自身元素（火史莱姆挂不了火等，与原神一致）
        if (target instanceof com.teyvat.elements.entity.ElementalSlimeEntity slime
                && slime.element().linkedElement() == element.linkedElement()) {
            return true;
        }
        var sources = target.damageSources();
        return switch (element) {
            case PYRO, BURNING -> target.isInvulnerableTo(sources.inFire());
            case CRYO, FREEZE -> target.isInvulnerableTo(sources.freeze());
            default -> false;
        };
    }

    /** 反应提示（日志 + 施加者动作条），伤害管线也会用到 */
    public static void notifyReaction(@Nullable LivingEntity origin, ElementalReaction reaction,
                                      LivingEntity target) {
        TeyvatElements.LOGGER.info("Reaction [{}] triggered on {}", reaction.id(), target.getName().getString());
        if (origin instanceof ServerPlayer player) {
            player.displayClientMessage(
                    Component.translatable("reaction.teyvat_elements." + reaction.id()), true);
        }
    }

    private ElementSystem() {}
}
