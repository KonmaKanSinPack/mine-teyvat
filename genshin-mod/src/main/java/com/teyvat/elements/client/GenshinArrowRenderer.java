package com.teyvat.elements.client;

import com.teyvat.elements.entity.GenshinArrowEntity;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * 原神箭矢渲染器：直接复用原版箭矢纹理。
 */
public class GenshinArrowRenderer extends ArrowRenderer<GenshinArrowEntity> {
    private static final ResourceLocation ARROW_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/entity/projectiles/arrow.png");

    public GenshinArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(GenshinArrowEntity entity) {
        return ARROW_TEXTURE;
    }
}
