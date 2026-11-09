package com.teyvat.elements.logic;

import com.teyvat.elements.data.ModAttachments;
import com.teyvat.elements.item.HomaStaffItem;
import com.teyvat.elements.registry.ModComponents;
import com.teyvat.elements.weapon.WeaponData;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/**
 * 武器经验：用养成的武器击杀生物获得经验。
 *
 * <p>经验量 = 目标最大生命 / 2（下限 5）；目标死时带元素附着 ×1.5
 * （鼓励打反应连招而不是平砍）。到上限后经验不再累积，必须铁砧突破。</p>
 */
public final class WeaponXpHandler {

    /** 注册在 NeoForge.EVENT_BUS */
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;

        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof HomaStaffItem)) return;

        WeaponData data = held.getOrDefault(ModComponents.WEAPON_DATA, WeaponData.DEFAULT);
        if (!data.canLevelUp()) return; // 到顶了，等突破

        int gain = Math.max(5, (int) (event.getEntity().getMaxHealth() / 2));
        // 带附着的目标（说明打了反应连招）经验 ×1.5
        if (event.getEntity().hasData(ModAttachments.ELEMENTAL_DATA)) {
            gain = gain * 3 / 2;
        }

        int xp = data.xp() + gain;
        int level = data.level();
        boolean leveledUp = false;
        while (data.canLevelUp() && xp >= new WeaponData(level, xp, data.ascension()).xpToNext()) {
            xp -= new WeaponData(level, xp, data.ascension()).xpToNext();
            level++;
            leveledUp = true;
        }
        // 顶到上限后多余经验不保留（逼玩家去突破）
        if (!new WeaponData(level, xp, data.ascension()).canLevelUp()) {
            xp = 0;
        }

        held.set(ModComponents.WEAPON_DATA, new WeaponData(level, xp, data.ascension()));

        if (leveledUp && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(Component.translatable(
                    "weapon.teyvat_elements.level_up",
                    held.getHoverName(), level), true);
        }
    }

    private WeaponXpHandler() {}
}
