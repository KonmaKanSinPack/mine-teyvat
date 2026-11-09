package com.teyvat.elements.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.teyvat.elements.TeyvatElements;
import com.teyvat.elements.menu.WeaponImprintMenu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 刻印镶嵌界面：上方 5 个刻印槽，下方玩家背包（贴图由脚本生成）。
 */
public class WeaponImprintScreen extends AbstractContainerScreen<WeaponImprintMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TeyvatElements.MOD_ID, "textures/gui/weapon_imprint.png");

    public WeaponImprintScreen(WeaponImprintMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
