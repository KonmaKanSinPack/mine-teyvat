package com.teyvat.elements.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.teyvat.elements.menu.WeaponImprintMenu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 刻印镶嵌界面：复用原版漏斗 GUI 贴图（一排 5 槽正好配五刻印）。
 */
public class WeaponImprintScreen extends AbstractContainerScreen<WeaponImprintMenu> {
    /** 原版漏斗 GUI：176x133，槽位 y=20，背包 y=51/109 */
    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/hopper.png");

    public WeaponImprintScreen(WeaponImprintMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageHeight = 133;
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
