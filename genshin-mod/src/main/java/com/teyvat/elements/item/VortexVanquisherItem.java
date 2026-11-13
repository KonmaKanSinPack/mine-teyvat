package com.teyvat.elements.item;

import com.teyvat.elements.registry.ModBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 贯虹之槊：岩元素长柄。
 *
 * <p>元素战技：右键在准星落点生成 1×2 岩造物柱（两格高），冷却 6 秒。
 * 潜行+右键仍为刻印界面（基类实现）。</p>
 */
public class VortexVanquisherItem extends GenshinWeaponItem {
    /** 战技冷却：6 秒 */
    private static final int SKILL_COOLDOWN_TICKS = 120;

    public VortexVanquisherItem(Properties properties) {
        // 岩元素 1.0 量值：命中触发结晶
        super(com.teyvat.elements.element.Element.GEO, 1.0, properties);
    }

    @Override
    public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level level, Player player,
                                                                      net.minecraft.world.InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != net.minecraft.world.InteractionHand.MAIN_HAND || player.isShiftKeyDown()) {
            return super.use(level, player, hand);
        }
        if (level.isClientSide) {
            return net.minecraft.world.InteractionResultHolder.sidedSuccess(stack, true);
        }
        if (player.getCooldowns().isOnCooldown(this)) {
            return net.minecraft.world.InteractionResultHolder.sidedSuccess(stack, false);
        }

        // 准星落点：取命中方块的外侧面
        BlockHitResult hit = (BlockHitResult) player.pick(6.0, 0.0F, false);
        BlockPos base = hit.getBlockPos().relative(hit.getDirection());

        // 目标位置与上方格都必须可替换（空气/草丛/藤蔓等），否则战技不发动
        if (level.getBlockState(base).canBeReplaced()
                && level.getBlockState(base.above()).canBeReplaced()) {
            var state = ModBlocks.GEO_CONSTRUCT.get().defaultBlockState();
            level.setBlock(base, state, 3);
            level.setBlock(base.above(), state, 3);
            level.playSound(null, base, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0f, 0.8f);

            player.getCooldowns().addCooldown(this, SKILL_COOLDOWN_TICKS);
            return net.minecraft.world.InteractionResultHolder.sidedSuccess(stack, false);
        }
        return net.minecraft.world.InteractionResultHolder.pass(stack);
    }
}
