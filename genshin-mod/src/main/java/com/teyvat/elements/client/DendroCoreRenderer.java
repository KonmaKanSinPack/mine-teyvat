package com.teyvat.elements.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.teyvat.elements.TeyvatElements;
import com.teyvat.elements.entity.DendroCoreEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * 草原核渲染器：公告板贴图（面向相机的绿色种子图标）。
 * 与头顶图标共用同一套公告板画法。
 */
public class DendroCoreRenderer extends EntityRenderer<DendroCoreEntity> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TeyvatElements.MOD_ID, "textures/entity/dendro_core.png");
    private static final float SIZE = 0.4f;

    public DendroCoreRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(DendroCoreEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        // 抬到碰撞箱中心并面向相机
        poseStack.translate(0, entity.getBbHeight() * 0.5, 0);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());

        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutout(TEXTURE));
        float h = SIZE / 2;
        vc.addVertex(poseStack.last(), -h, -h, 0).setColor(1f, 1f, 1f, 1f)
                .setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
                .setNormal(poseStack.last(), 0, 1, 0);
        vc.addVertex(poseStack.last(), h, -h, 0).setColor(1f, 1f, 1f, 1f)
                .setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
                .setNormal(poseStack.last(), 0, 1, 0);
        vc.addVertex(poseStack.last(), h, h, 0).setColor(1f, 1f, 1f, 1f)
                .setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
                .setNormal(poseStack.last(), 0, 1, 0);
        vc.addVertex(poseStack.last(), -h, h, 0).setColor(1f, 1f, 1f, 1f)
                .setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
                .setNormal(poseStack.last(), 0, 1, 0);
        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(DendroCoreEntity entity) {
        return TEXTURE;
    }
}
