package com.teyvat.elements.network;

import com.teyvat.elements.TeyvatElements;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;

/**
 * 附着同步包（S2C）：把某个生物当前的元素附着推送给正在追踪它的客户端。
 *
 * <p>数据刻意用简单 CSV（"pyro:0.8,hydro:0.32"）而不是复杂结构：
 * 骨架期字段少，字符串自描述、调试时日志里直接可读；元素数量上双位数后再换二进制编码。</p>
 */
public record AuraSyncPayload(int entityId, String data) implements CustomPacketPayload {
    public static final Type<AuraSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(TeyvatElements.MOD_ID, "aura_sync"));

    public static final StreamCodec<FriendlyByteBuf, AuraSyncPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, AuraSyncPayload::entityId,
            ByteBufCodecs.STRING_UTF8, AuraSyncPayload::data,
            AuraSyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
