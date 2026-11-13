package com.teyvat.elements.logic;

import com.teyvat.elements.registry.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 岩造物共鸣：玩家附近有岩造物时缓慢回复吸收心（护盾）。
 *
 * <p>让贯虹的岩造物从一次性柱子变成阵地掩体：插柱站旁边就有持续护盾收益。
 * 每秒回 0.5，上限 10（与结晶片拾取同一上限）。</p>
 */
public final class GeoResonanceHandler {
    private static final int SCAN_RADIUS = 6;
    private static final int SCAN_HEIGHT_UP = 3;
    private static final int SCAN_HEIGHT_DOWN = 2;
    private static final float REGEN_PER_SECOND = 0.5f;
    private static final float MAX_ABSORPTION = 10.0f;

    /** 注册在 NeoForge.EVENT_BUS（每秒一次） */
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;
        if (player.tickCount % 20 != 0) return;
        if (player.getAbsorptionAmount() >= MAX_ABSORPTION) return;

        BlockPos construct = findConstruct(player);
        if (construct == null) return;

        player.setAbsorptionAmount(Math.min(
                player.getAbsorptionAmount() + REGEN_PER_SECOND, MAX_ABSORPTION));

        // 共鸣表现：岩造物顶部金色光点 + 玩家周围少量金屑
        if (player.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.FIREWORK,
                    construct.getX() + 0.5, construct.getY() + 2.1, construct.getZ() + 0.5,
                    3, 0.2, 0.2, 0.2, 0.01);
            level.sendParticles(ParticleTypes.WAX_ON,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    2, 0.3, 0.3, 0.3, 0.0);
        }
    }

    /** 在玩家周围的小范围内找岩造物（每秒每玩家扫描约 1300 格，开销可控） */
    private static BlockPos findConstruct(Player player) {
        BlockPos center = player.blockPosition();
        for (int x = -SCAN_RADIUS; x <= SCAN_RADIUS; x++) {
            for (int z = -SCAN_RADIUS; z <= SCAN_RADIUS; z++) {
                for (int y = -SCAN_HEIGHT_DOWN; y <= SCAN_HEIGHT_UP; y++) {
                    BlockPos pos = center.offset(x, y, z);
                    if (player.level().getBlockState(pos).is(ModBlocks.GEO_CONSTRUCT.get())) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }

    private GeoResonanceHandler() {}
}
