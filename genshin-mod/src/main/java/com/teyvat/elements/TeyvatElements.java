package com.teyvat.elements;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.teyvat.elements.data.ModAttachments;
import com.teyvat.elements.logic.ElementSystem;
import com.teyvat.elements.network.ModNetworking;
import com.teyvat.elements.reaction.ReactionRegistry;
import com.teyvat.elements.registry.ModItems;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(TeyvatElements.MOD_ID)
public class TeyvatElements {
    public static final String MOD_ID = "teyvat_elements";
    public static final Logger LOGGER = LoggerFactory.getLogger(TeyvatElements.class);

    public TeyvatElements(IEventBus modEventBus) {
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModItems.CREATIVE_TABS.register(modEventBus);
        com.teyvat.elements.registry.ModEntities.ENTITY_TYPES.register(modEventBus);
        com.teyvat.elements.registry.ModAttributes.ATTRIBUTES.register(modEventBus);
        com.teyvat.elements.registry.ModComponents.COMPONENTS.register(modEventBus);
        com.teyvat.elements.registry.ModBlocks.BLOCKS.register(modEventBus);
        com.teyvat.elements.registry.ModMenus.MENUS.register(modEventBus);
        ModNetworking.register(modEventBus);

        // 元素史莱姆属性注册
        modEventBus.addListener(net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent.class,
                event -> {
                    var attributes = net.minecraft.world.entity.Mob.createMobAttributes()
                            .add(Attributes.MAX_HEALTH, 20.0)
                            .add(Attributes.MOVEMENT_SPEED, 0.25)
                            .add(Attributes.ATTACK_DAMAGE, 3.0)
                            .build();
                    event.put(com.teyvat.elements.registry.ModEntities.PYRO_SLIME.get(), attributes);
                    event.put(com.teyvat.elements.registry.ModEntities.HYDRO_SLIME.get(), attributes);
                    event.put(com.teyvat.elements.registry.ModEntities.ELECTRO_SLIME.get(), attributes);
                    event.put(com.teyvat.elements.registry.ModEntities.CRYO_SLIME.get(), attributes);
                });

        // 把自定义属性挂到所有生物上（物抗/精通/暴击率）
        modEventBus.addListener(net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent.class,
                event -> event.getTypes().forEach(type -> {
                    event.add(type, com.teyvat.elements.registry.ModAttributes.PHYSICAL_RESISTANCE);
                    event.add(type, com.teyvat.elements.registry.ModAttributes.ELEMENTAL_MASTERY);
                    event.add(type, com.teyvat.elements.registry.ModAttributes.CRIT_RATE);
                }));

        ReactionRegistry.bootstrap();

        // 附着衰减 + 环境附着走游戏总线的实体 tick 事件
        NeoForge.EVENT_BUS.addListener(ElementSystem::onEntityTick);

        // 雷击 → 雷附着
        NeoForge.EVENT_BUS.addListener(ElementSystem::onLightningStrike);

        // 碎冰：伤害事件驱动的反应特例（详见 ShatterHandler 注释）
        NeoForge.EVENT_BUS.addListener(com.teyvat.elements.logic.ShatterHandler::onIncomingDamage);

        // 元素伤害管线：法杖近战命中的元素判定（增幅反应在此放大当次伤害）
        NeoForge.EVENT_BUS.addListener(com.teyvat.elements.logic.ElementalCombatHandler::onIncomingDamage);

        // 物理抗性结算（超导减抗的收益在此生效）
        NeoForge.EVENT_BUS.addListener(com.teyvat.elements.logic.ResistanceHandler::onIncomingDamage);

        // 暴击结算（直接攻击掷骰 ×1.5）
        NeoForge.EVENT_BUS.addListener(com.teyvat.elements.logic.CritHandler::onIncomingDamage);

        // 结晶片拾取 → 吸收心护盾
        NeoForge.EVENT_BUS.addListener(com.teyvat.elements.logic.ShardPickupHandler::onPickup);

        // 岩造物共鸣：附近缓慢回盾
        NeoForge.EVENT_BUS.addListener(com.teyvat.elements.logic.GeoResonanceHandler::onPlayerTick);

        // 冻结接管名单的离场清理（防止死亡/卸载导致的 UUID 泄漏）
        NeoForge.EVENT_BUS.addListener(com.teyvat.elements.logic.FreezeController::onEntityLeaveLevel);

        // 武器养成：铁砧升级（魔矿）与突破（突破石）
        NeoForge.EVENT_BUS.addListener(com.teyvat.elements.logic.AscensionHandler::onAnvilUpdate);

        // 玩家开始追踪某实体时补发附着同步（否则走近已有附着的怪看不到图标）
        NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.event.entity.player.PlayerEvent.StartTracking.class,
                event -> {
                    if (event.getTarget() instanceof net.minecraft.world.entity.LivingEntity living
                            && living.hasData(ModAttachments.ELEMENTAL_DATA)) {
                        ModNetworking.syncAura(living);
                    }
                });

        // 客户端专属渲染（图标公告板 + 冻结冰壳）仅在物理客户端注册，避免专用服务器加载客户端类
        if (net.neoforged.fml.loading.FMLEnvironment.dist == net.neoforged.api.distmarker.Dist.CLIENT) {
            com.teyvat.elements.client.ClientEvents.register(modEventBus);
            // 武器升阶模型谓词
            modEventBus.addListener(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent.class,
                    com.teyvat.elements.client.WeaponModelProperties::register);
        }

        // 探针：注册表冻结后验证物品/页签真的进了注册表（排查"创造栏搜不到"用）
        modEventBus.addListener(net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent.class,
                event -> event.enqueueWork(() -> {
                    var items = net.minecraft.core.registries.BuiltInRegistries.ITEM;
                    var tabs = net.minecraft.core.registries.BuiltInRegistries.CREATIVE_MODE_TAB;
                    LOGGER.info("PROBE pyro_wand registered = {}",
                            items.containsKey(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MOD_ID, "pyro_wand")));
                    LOGGER.info("PROBE creative tab registered = {}",
                            tabs.containsKey(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MOD_ID, "main")));
                }));

        LOGGER.info("Teyvat Elements initialized");
    }
}
