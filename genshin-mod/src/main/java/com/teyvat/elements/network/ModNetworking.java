package com.teyvat.elements.network;

import com.teyvat.elements.client.ClientAuraCache;
import com.teyvat.elements.data.ElementalData;
import com.teyvat.elements.data.ModAttachments;
import com.teyvat.elements.element.Element;
import com.teyvat.elements.element.ElementalApplication;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/**
 * 网络层注册与发送助手。
 *
 * <p>注意：{@code playToClient} 的处理器引用 {@link ClientAuraCache}——
 * 它是纯数据类（不 import 任何客户端专属类），因此服务端加载本类时不会拉进
 * Minecraft 客户端类，专用服务器安全。</p>
 */
public final class ModNetworking {

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(RegisterPayloadHandlersEvent.class, event -> {
            event.registrar("1").playToClient(
                    AuraSyncPayload.TYPE,
                    AuraSyncPayload.STREAM_CODEC,
                    (payload, context) -> context.enqueueWork(
                            () -> ClientAuraCache.put(payload.entityId(), payload.data())));
        });
    }

    /** 把实体当前附着推给所有正在追踪它的玩家（以及实体本人，如果是玩家） */
    public static void syncAura(LivingEntity entity) {
        if (entity.level().isClientSide) return;

        AuraSyncPayload payload = new AuraSyncPayload(entity.getId(), encode(entity));
        PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
        if (entity instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, payload);
        }
    }

    /** 编码为 "element:gauge,..." ；无附着时为空串 */
    private static String encode(LivingEntity entity) {
        if (!entity.hasData(ModAttachments.ELEMENTAL_DATA)) return "";

        ElementalData data = entity.getData(ModAttachments.ELEMENTAL_DATA);
        StringBuilder sb = new StringBuilder();
        for (Element element : data.auraElements()) {
            ElementalApplication app = data.getAura(element);
            if (app == null) continue;
            if (sb.length() > 0) sb.append(',');
            sb.append(element.id()).append(':').append(String.format("%.3f", app.gauge()));
        }
        return sb.toString();
    }

    private ModNetworking() {}
}
