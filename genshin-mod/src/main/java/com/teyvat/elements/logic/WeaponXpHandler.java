package com.teyvat.elements.logic;

import com.teyvat.elements.item.HomaStaffItem;
import com.teyvat.elements.registry.ModComponents;
import com.teyvat.elements.weapon.WeaponData;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;

/**
 * 武器经验：主手持养成武器时，捡到的经验球喂给武器而不是玩家。
 *
 * <p>用户决策（替代击杀经验）：和"经验修补"同思路——武器在手时经验球归武器。
 * 武器到当前阶数上限后放行给玩家（不拦截，不会浪费经验）。</p>
 */
public final class WeaponXpHandler {

    /** 注册在 NeoForge.EVENT_BUS */
    public static void onPickupXp(PlayerXpEvent.PickupXp event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof HomaStaffItem)) return;

        WeaponData data = held.getOrDefault(ModComponents.WEAPON_DATA, WeaponData.DEFAULT);
        if (!data.canLevelUp()) return; // 到顶了：经验放行给玩家，提示该突破了

        int xp = data.xp() + event.getOrb().value;
        int level = data.level();
        boolean leveledUp = false;
        WeaponData current = new WeaponData(level, xp, data.ascension());
        while (current.canLevelUp() && xp >= current.xpToNext()) {
            xp -= current.xpToNext();
            level++;
            leveledUp = true;
            current = new WeaponData(level, xp, data.ascension());
        }
        // 顶到上限后多余经验不保留（逼玩家去突破）
        if (!current.canLevelUp()) {
            xp = 0;
            current = new WeaponData(level, 0, data.ascension());
        }

        held.set(ModComponents.WEAPON_DATA, current);

        // 拦截经验球：经验进武器，玩家不加，球消耗掉
        event.setCanceled(true);
        event.getOrb().discard();

        if (leveledUp && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(Component.translatable(
                    "weapon.teyvat_elements.level_up", held.getHoverName(), level), true);
        }
    }

    private WeaponXpHandler() {}
}
