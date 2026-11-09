package com.teyvat.elements.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.teyvat.elements.TeyvatElements;
import com.teyvat.elements.element.Element;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/**
 * 冻结冰壳渲染层：目标带 FREEZE 附着时，用冰蓝色半透明色重绘一遍模型，
 * 形成"裹在冰里"的外壳观感（等价原神冻结目标的裹冰表现）。
 *
 * <p>实现要点：不新建贴图集，用一张纯白小贴图 + 顶点色（ARGB）染色；
 * 父模型在主渲染流程中已完成动画姿态设置，本层直接复用其姿态重绘。</p>
 */
public class FrozenLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    /** 通用白色载体贴图（scripts/gen_frozen_overlay.py 生成） */
    private static final ResourceLocation ICE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TeyvatElements.MOD_ID, "textures/entity/frozen_overlay.png");
    /** 冰蓝色外壳：alpha 0x66（约 40%）保证能看清本体，又不至于像没冻住 */
    private static final int ICE_COLOR = 0x66A9DFF5;

    public FrozenLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTicks,
                       float ageInTicks, float netHeadYaw, float headPitch) {
        boolean frozen = ClientAuraCache.get(entity.getId()).stream()
                .anyMatch(entry -> entry.element() == Element.FREEZE);
        if (!frozen) return;

        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(ICE_TEXTURE));
        getParentModel().renderToBuffer(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY, ICE_COLOR);
    }
}
