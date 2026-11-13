package com.teyvat.elements.client;

import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import com.teyvat.elements.entity.AbyssMageEntity;

/**
 * 深渊法师渲染器：唤魔者模型 + 元素配色贴图（占位外观）。
 */
public class AbyssMageRenderer extends MobRenderer<AbyssMageEntity, IllagerModel<AbyssMageEntity>> {
    private final ResourceLocation texture;

    public AbyssMageRenderer(EntityRendererProvider.Context context, ResourceLocation texture) {
        super(context, new IllagerModel<>(context.bakeLayer(ModelLayers.EVOKER)), 0.6f);
        this.texture = texture;
    }

    @Override
    public ResourceLocation getTextureLocation(AbyssMageEntity entity) {
        return texture;
    }
}
