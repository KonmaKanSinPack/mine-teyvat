package com.teyvat.elements.client;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.teyvat.elements.TeyvatElements;
import com.teyvat.elements.element.Element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

/**
 * 客户端表现层：头顶元素图标渲染 + 缓存清理。
 *
 * <p>本类引用 Minecraft 客户端类，<b>只能在物理客户端加载</b>——
 * 注册入口由 {@code TeyvatElements} 用 Dist 判断守护。</p>
 */
public final class ClientEvents {
    private static final float ICON_SIZE = 0.26f;
    private static final float ICON_GAP = 0.32f;
    private static final float HEIGHT_OFFSET = 0.4f;

    /** 由主类在 Dist.CLIENT 下调用 */
    public static void register(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.addListener(ClientEvents::onRenderLivingPost);
        NeoForge.EVENT_BUS.addListener(ClientEvents::onEntityLeaveLevel);
        // 冰壳渲染层挂载（mod 总线事件）
        modEventBus.addListener(EntityRenderersEvent.AddLayers.class, ClientEvents::onAddLayers);
        // 草原核渲染器注册
        modEventBus.addListener(EntityRenderersEvent.RegisterRenderers.class,
                event -> {
                    event.registerEntityRenderer(
                            com.teyvat.elements.registry.ModEntities.DENDRO_CORE.get(),
                            DendroCoreRenderer::new);
                    // 原神箭矢渲染器（复用原版箭矢渲染器）
                    event.registerEntityRenderer(
                            com.teyvat.elements.registry.ModEntities.GENSHIN_ARROW.get(),
                            net.minecraft.client.renderer.entity.ArrowRenderer::new);
                });
        // 刻印界面注册
        modEventBus.addListener(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent.class,
                event -> event.register(com.teyvat.elements.registry.ModMenus.WEAPON_IMPRINT.get(),
                        WeaponImprintScreen::new));
    }

    /** 给所有生物渲染器（含玩家）挂冻结冰壳层 */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (var entityType : BuiltInRegistries.ENTITY_TYPE) {
            if (event.getRenderer(entityType) instanceof LivingEntityRenderer<?, ?> renderer) {
                renderer.addLayer(new FrozenLayer(renderer));
            }
        }
        // 玩家模型走独立的皮肤渲染器列表（NeoForge 21.1 API：getSkin）
        for (var skin : event.getSkins()) {
            if (event.getSkin(skin) instanceof LivingEntityRenderer<?, ?> renderer) {
                renderer.addLayer(new FrozenLayer(renderer));
            }
        }
    }

    /** 实体离开客户端世界时清缓存，防止 entityId 复用后串数据 */
    private static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (!event.getLevel().isClientSide()) return;
        ClientAuraCache.remove(event.getEntity().getId());
    }

    private static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        List<ClientAuraCache.Entry> auras = ClientAuraCache.get(event.getEntity().getId());
        if (auras.isEmpty()) return;

        PoseStack pose = event.getPoseStack();
        MultiBufferSource buffer = event.getMultiBufferSource();
        int light = event.getPackedLight();

        pose.pushPose();
        // pose 已处于实体脚底位置：抬到头顶，再面向相机（公告板）
        pose.translate(0, event.getEntity().getBbHeight() + HEIGHT_OFFSET, 0);
        pose.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());

        int n = auras.size();
        for (int i = 0; i < n; i++) {
            ClientAuraCache.Entry aura = auras.get(i);
            // 多附着横向排开，整体居中
            float x = (i - (n - 1) / 2.0f) * ICON_GAP;
            // 剩余量值映射为透明度：快衰减完的元素图标变淡，直观传达"快没了"
            float alpha = Math.min(1.0f, Math.max(0.25f, aura.gauge()));
            drawIcon(pose, buffer, light, aura.element(), x, alpha);
        }
        pose.popPose();
    }

    private static void drawIcon(PoseStack pose, MultiBufferSource buffer, int light,
                                 Element element, float offsetX, float alpha) {
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(
                TeyvatElements.MOD_ID, "textures/element/" + element.id() + ".png");
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(texture));

        float half = ICON_SIZE / 2;
        float x0 = offsetX - half, x1 = offsetX + half;
        // 公告板平面：法线朝 +Z（旋转后正对相机）
        vc.addVertex(pose.last(), x0, -half, 0).setColor(1f, 1f, 1f, alpha)
                .setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(pose.last(), 0, 1, 0);
        vc.addVertex(pose.last(), x1, -half, 0).setColor(1f, 1f, 1f, alpha)
                .setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(pose.last(), 0, 1, 0);
        vc.addVertex(pose.last(), x1, half, 0).setColor(1f, 1f, 1f, alpha)
                .setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(pose.last(), 0, 1, 0);
        vc.addVertex(pose.last(), x0, half, 0).setColor(1f, 1f, 1f, alpha)
                .setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(pose.last(), 0, 1, 0);
    }

    private ClientEvents() {}
}
