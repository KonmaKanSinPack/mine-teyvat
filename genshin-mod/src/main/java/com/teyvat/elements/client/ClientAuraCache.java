package com.teyvat.elements.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.teyvat.elements.element.Element;

/**
 * 客户端附着缓存：entityId → 当前附着列表。
 *
 * <p>刻意保持为纯数据类（只依赖 java.* 与本 mod 的 Element）：
 * 服务端注册网络处理器时会加载引用本类的 lambda，若本类引用 Minecraft 客户端类
 * （Minecraft/RenderType 等），专用服务器启动即崩。客户端专属的渲染代码全部
 * 放在 {@link ClientEvents}。</p>
 */
public final class ClientAuraCache {
    /** 一条附着：元素 + 剩余量值（0~1+，用于渲染透明度） */
    public record Entry(Element element, float gauge) {}

    private static final Map<Integer, List<Entry>> AURAS = new ConcurrentHashMap<>();

    /** 网络包处理器调用（在客户端主线程 enqueueWork 后执行） */
    public static void put(int entityId, String csv) {
        if (csv == null || csv.isEmpty()) {
            AURAS.remove(entityId);
            return;
        }
        List<Entry> entries = new ArrayList<>();
        for (String pair : csv.split(",")) {
            String[] kv = pair.split(":");
            if (kv.length != 2) continue;
            try {
                entries.add(new Entry(Element.valueOf(kv[0].toUpperCase()), Float.parseFloat(kv[1])));
            } catch (IllegalArgumentException ignored) {
                // 未知元素（版本不匹配时）：跳过而不是崩客户端
            }
        }
        AURAS.put(entityId, entries);
    }

    public static List<Entry> get(int entityId) {
        return AURAS.getOrDefault(entityId, List.of());
    }

    public static void remove(int entityId) {
        AURAS.remove(entityId);
    }

    private ClientAuraCache() {}
}
