package com.teyvat.elements.weapon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 武器养成数据（存在物品组件里，跟随 ItemStack：掉落/装箱/交易都不丢）。
 *
 * @param level     当前等级（1 起）
 * @param ascension 突破阶数：0 阶上限 Lv.20，1 阶 Lv.40，2 阶 Lv.60，
 *                  3 阶 Lv.90 满级（60 级突破时换升阶模型）
 */
public record WeaponData(int level, int ascension) {
    public static final WeaponData DEFAULT = new WeaponData(1, 0);

    public static final Codec<WeaponData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("level").forGetter(WeaponData::level),
            Codec.INT.fieldOf("ascension").forGetter(WeaponData::ascension)
    ).apply(instance, WeaponData::new));

    /** 当前阶数下的等级上限 */
    public int maxLevel() {
        return ascension >= 3 ? 90 : 20 * (ascension + 1);
    }

    public boolean canLevelUp() {
        return level < maxLevel();
    }

    /** 到上限且未满阶 = 可以突破 */
    public boolean canAscend() {
        return level >= maxLevel() && ascension < 3;
    }

    /** 加 n 级，到顶截断 */
    public WeaponData addLevels(int n) {
        return new WeaponData(Math.min(level + n, maxLevel()), ascension);
    }

    public WeaponData levelUp() {
        return new WeaponData(level + 1, ascension);
    }

    public WeaponData ascend() {
        return new WeaponData(level, ascension + 1);
    }

    /** 等级攻击加成：+0.2 × (等级-1) */
    public double attackBonus() {
        return (level - 1) * 0.2;
    }
}
