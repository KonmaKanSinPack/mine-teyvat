package com.teyvat.elements.data;

import com.teyvat.elements.TeyvatElements;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * NeoForge Attachment 注册中心。
 *
 * <p>为什么用 Attachment 而不是 Mixin 加字段：Attachment 是 NeoForge 官方的
 * 实体扩展数据机制，不需要侵入 LivingEntity 的字节码，兼容性和可维护性都更好；
 * 这也是 NeoForge 侧替代 SevenElements 所用 CCA 组件的原生方案。</p>
 */
public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, TeyvatElements.MOD_ID);

    public static final Supplier<AttachmentType<ElementalData>> ELEMENTAL_DATA =
            ATTACHMENT_TYPES.register("elemental_data",
                    // 无序列化器 = 瞬态数据，不写入存档（原因见 ElementalData 类注释）
                    () -> AttachmentType.builder(ElementalData::new).build());

    private ModAttachments() {}
}
