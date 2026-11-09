package com.teyvat.elements.element;

/**
 * 附着冷却（Internal Cooldown, ICD）的简化实现。
 *
 * <p>原神规则：同一攻击来源的元素附着受"3 次命中 / 2.5 秒"限制——
 * 一个窗口期内第 1 次命中施加元素，第 2、3 次被冷却吞掉，第 4 次（或 2.5 秒后）
 * 才能再次施加。骨架阶段按"来源标签"分组：同一把武器/技能连续命中共享一个窗口。</p>
 *
 * <p>后续可升级为标准的 (tag, hit-count, duration) 三元组计数型 ICD。</p>
 */
public final class InternalCooldown {
    /** 窗口长度：2.5 秒 × 20 tick */
    public static final int WINDOW_TICKS = 50;
    /** 每个窗口内最多吞掉的命中数（第 1 次施加，第 2、3 次冷却） */
    public static final int HITS_PER_WINDOW = 3;

    private long windowStart;
    private int hits = 0;

    /**
     * 尝试消耗一次附着机会。
     *
     * @param gameTime 当前世界时间（tick）
     * @return true 表示本次命中允许施加元素
     */
    public boolean tryConsume(long gameTime) {
        // 用 hits==0 判断"未初始化"而不是 Long.MIN_VALUE 哨兵：
        // 哨兵值参与 gameTime-windowStart 会长整型溢出，导致窗口永远"未过期"、
        // 首次附着被误吞（16:37 实测翻车原因）
        if (hits == 0 || gameTime - windowStart >= WINDOW_TICKS) {
            windowStart = gameTime;
            hits = 1;
            return true;
        }
        hits++;
        // 窗口内第 4 次命中：重置窗口并施加（对应"每 3 次命中附着一次"）
        if (hits > HITS_PER_WINDOW) {
            windowStart = gameTime;
            hits = 1;
            return true;
        }
        return false;
    }
}
