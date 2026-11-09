package com.teyvat.elements.logic;

import com.teyvat.elements.TeyvatElements;
import com.teyvat.elements.item.CrystalShardItem;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.common.util.TriState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

/**
 * 结晶片拾取：转化为吸收心（护盾）而不是进入背包。
 *
 * <p>吸收心是 MC 原生的"护盾"等价物——会被伤害优先消耗，正好对应原神结晶盾
 * "盾破为止"的行为。公式：2 + 反应量值 × 4，上限 10（5 颗黄心）。</p>
 *
 * <p>用 {@code ItemEntityPickupEvent.Pre} 拦截：禁止拾取入包、发效果、销毁实体。</p>
 */
public final class ShardPickupHandler {
    private static final float BASE_SHIELD = 2.0f;
    private static final float SHIELD_PER_GAUGE = 4.0f;
    private static final float MAX_ABSORPTION = 10.0f;

    /** 注册在 NeoForge.EVENT_BUS */
    public static void onPickup(ItemEntityPickupEvent.Pre event) {
        ItemStack stack = event.getItemEntity().getItem();
        if (!(stack.getItem() instanceof CrystalShardItem)) return;

        Player player = event.getPlayer();
        if (player.level().isClientSide) return;

        double gauge = 0.5;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null) {
            CompoundTag tag = data.copyTag();
            if (tag.contains("gauge")) gauge = tag.getDouble("gauge");
        }

        float total = Math.min(player.getAbsorptionAmount() + BASE_SHIELD + (float) (gauge * SHIELD_PER_GAUGE),
                MAX_ABSORPTION);
        player.setAbsorptionAmount(total);

        // 结晶片转化为护盾，不进背包
        event.setCanPickup(TriState.FALSE);
        event.getItemEntity().discard();

        TeyvatElements.LOGGER.info("crystallize shard picked: +{} absorption (total {})",
                String.format("%.1f", BASE_SHIELD + gauge * SHIELD_PER_GAUGE), total);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.6f, 1.4f);
    }

    private ShardPickupHandler() {}
}
