package com.teyvat.elements.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Slime;

/**
 * 元素史莱姆渲染器：复用原版史莱姆模型，仅换元素配色贴图。
 */
public class ElementalSlimeRenderer extends SlimeRenderer {
    private final ResourceLocation texture;

    public ElementalSlimeRenderer(EntityRendererProvider.Context context, ResourceLocation texture) {
        super(context);
        this.texture = texture;
    }

    @Override
    public ResourceLocation getTextureLocation(Slime entity) {
        return texture;
    }
}
