package com.teyvat.elements.logic;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.teyvat.elements.TeyvatElements;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

/**
 * 冻结定身控制器（加强版）。
 *
 * <p>三层手段，缺一不可：</p>
 * <ol>
 *   <li>移速属性归零 —— 挡住常规的走路/寻路移动</li>
 *   <li>NoAi 接管 —— 史莱姆跳跃、蜘蛛扑击等由 AI 直接改速度矢量，
 *       根本不走移速属性（18:25 实测"冻住了还能动"的原因），
 *       只有停掉 AI 才能定住它们；攻击意图也一并停掉，符合"冻结=完全控制"</li>
 *   <li>每 tick 清零水平速度 —— 冻结期间击退不位移（原神中冻结体不可击退，
 *       除非触发碎冰）</li>
 * </ol>
 *
 * <p>NoAi 是会被写进 NBT 的实体字段，直接改有副作用风险（比如原本就是 NoAi 的
 * 展示实体被错误"解冻"），因此用 {@link #NO_AI_HIJACKED} 记录哪些实体是我们改的，
 * 只还原本轮接管的。</p>
 */
public final class FreezeController {
    private static final ResourceLocation FROZEN_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(TeyvatElements.MOD_ID, "frozen");

    /** 本轮被冻结接管了 AI 的实体（服务端有效） */
    private static final Set<UUID> NO_AI_HIJACKED = new HashSet<>();

    /** 在附着状态变化后调用：frozen=true 定身，false 解除 */
    public static void update(LivingEntity entity, boolean frozen) {
        AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            if (frozen) {
                speed.addOrUpdateTransientModifier(new AttributeModifier(
                        FROZEN_MODIFIER_ID, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            } else {
                speed.removeModifier(FROZEN_MODIFIER_ID);
            }
        }

        if (entity instanceof Mob mob) {
            if (frozen && !mob.isNoAi()) {
                mob.setNoAi(true);
                NO_AI_HIJACKED.add(mob.getUUID());
            } else if (!frozen && NO_AI_HIJACKED.remove(mob.getUUID())) {
                mob.setNoAi(false);
            }
        }
    }

    /** 每 tick 调用：冻结期间清零水平位移（保留竖直速度让重力正常工作） */
    public static void zeroHorizontalMotion(LivingEntity entity) {
        Vec3 motion = entity.getDeltaMovement();
        if (motion.x != 0 || motion.z != 0) {
            entity.setDeltaMovement(0, motion.y, 0);
            entity.hurtMarked = true; // 强制同步速度到客户端
        }
    }

    public static boolean isFrozen(LivingEntity entity) {
        return entity instanceof Mob mob && NO_AI_HIJACKED.contains(mob.getUUID());
    }

    /**
     * 实体离开世界（死亡/卸载）时清理接管记录。
     * 没有这一步，冻结中的怪死亡或区块卸载会让 UUID 永远留在集合里（长跑服务器慢泄漏）。
     * 注意：这里只清记录，不做 setNoAi(false)——实体已经离开世界，无需还原。
     */
    public static void onEntityLeaveLevel(net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getEntity() instanceof Mob mob) {
            NO_AI_HIJACKED.remove(mob.getUUID());
        }
    }

    private FreezeController() {}
}
