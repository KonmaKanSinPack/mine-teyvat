package com.teyvat.elements.data;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.teyvat.elements.element.Element;
import com.teyvat.elements.element.ElementalApplication;
import com.teyvat.elements.element.InternalCooldown;

import net.minecraft.resources.ResourceLocation;

/**
 * 挂在生物身上的元素状态：当前全部附着 + 各来源标签的 ICD 窗口。
 *
 * <p>为什么不序列化：附着与 ICD 都是秒级瞬态状态，随实体 tick 快速衰减，
 * 存档/读档保留它们没有意义，反而引入兼容负担。因此 Attachment 不注册序列化器，
 * 死亡/重载后状态自然清空——这也与原神"脱战清附着"的直觉一致。</p>
 */
public final class ElementalData {
    private final Map<Element, ElementalApplication> auras = new EnumMap<>(Element.class);
    private final Map<String, InternalCooldown> icdByTag = new HashMap<>();
    /** 时限 debuff：ID → 过期时刻（gameTime tick），如超导的减抗 */
    private final Map<ResourceLocation, Long> debuffs = new HashMap<>();

    public ElementalApplication getAura(Element element) {
        return auras.get(element);
    }

    /** 当前所有附着元素（返回副本，调用方可安全遍历并修改本体） */
    public Set<Element> auraElements() {
        return Set.copyOf(auras.keySet());
    }

    public void applyAura(Element element, double gauge, String sourceTag) {
        ElementalApplication existing = auras.get(element);
        if (existing != null) {
            existing.refresh(gauge);
        } else {
            auras.put(element, new ElementalApplication(element, gauge, sourceTag));
        }
    }

    public void removeAura(Element element) {
        auras.remove(element);
    }

    public InternalCooldown icd(String sourceTag) {
        return icdByTag.computeIfAbsent(sourceTag, tag -> new InternalCooldown());
    }

    /** 记录/刷新一个时限 debuff */
    public void setDebuff(ResourceLocation id, long expiryGameTime) {
        debuffs.put(id, expiryGameTime);
    }

    /** debuff 的过期时刻；不存在返回 null */
    public Long debuffExpiry(ResourceLocation id) {
        return debuffs.get(id);
    }

    public void removeDebuff(ResourceLocation id) {
        debuffs.remove(id);
    }

    /** 每 tick 衰减所有附着，清空耗尽的附着；返回是否有附着被移除（用于决定是否同步客户端） */
    public boolean tick() {
        int before = auras.size();
        auras.values().forEach(ElementalApplication::tick);
        auras.values().removeIf(ElementalApplication::isEmpty);
        return auras.size() != before;
    }

    /** 附着与时限 debuff 都为空才算空（否则超导减抗这类 debuff 会随附着清空被丢跟踪） */
    public boolean isEmpty() {
        return auras.isEmpty() && debuffs.isEmpty();
    }
}
