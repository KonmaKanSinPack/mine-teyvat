package com.teyvat.elements.element;

/**
 * 附着冷却（Internal Cooldown, ICD）的简化实现。
 *
 * <p>窗口取 0.6 秒（对齐原版剑的有效挥剑间隔，用户决策），窗口内沿用
 * "3 次命中"计数：第 1 次命中施加元素，第 2、3 次被冷却吞掉，第 4 次
 * （或窗口结束）再次施加。</p>
 *
 * <p>后续可升级为标准的 (tag, hit-count, duration) 三元组计数型 ICD。</p>
 */
public final class InternalCooldown {
    /** 窗口长度：0.6 秒（12 tick），对齐原版剑的有效挥剑间隔 */
    public static final int WINDOW_TICKS = 12;
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
