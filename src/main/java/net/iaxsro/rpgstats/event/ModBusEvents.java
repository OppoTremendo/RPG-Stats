package net.iaxsro.rpgstats.event;

import net.iaxsro.rpgstats.RpgStatsMod;
import net.iaxsro.rpgstats.network.PacketHandler;
import net.iaxsro.rpgstats.registry.AttributeRegistry;
import net.iaxsro.rpgstats.registry.ItemRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

// Escucha eventos solo en el bus de MOD
@Mod.EventBusSubscriber(modid = RpgStatsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModBusEvents {

    private static final Logger LOGGER = RpgStatsMod.LOGGER;

    @SubscribeEvent
    public static void onCommonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("MOD BUS EVENT: FMLCommonSetupEvent");
        event.enqueueWork(() -> {
            PacketHandler.register();
            LOGGER.debug("PacketHandler registrado desde CommonSetup.");
        });
    }

    @SubscribeEvent
    public static void addCustomAttributes(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, AttributeRegistry.STRENGTH.get());
        event.add(EntityType.PLAYER, AttributeRegistry.DEXTERITY.get());
        event.add(EntityType.PLAYER, AttributeRegistry.VITALITY.get());
        event.add(EntityType.PLAYER, AttributeRegistry.CONSTITUTION.get());
        event.add(EntityType.PLAYER, AttributeRegistry.INTELLIGENCE.get());
        RpgStatsMod.LOGGER.info("Atributos de RPGStats añadidos a EntityType.PLAYER.");
    }

    @SubscribeEvent
    public static void onModifyEntityAttributes(final EntityAttributeModificationEvent event) {
        LOGGER.info("MOD BUS EVENT: EntityAttributeModificationEvent");
        for (EntityType<?> entityType : event.getTypes()) {
            if (LivingEntity.class.isAssignableFrom(entityType.getBaseClass())) {
                try {
                    @SuppressWarnings("unchecked")
                    EntityType<? extends LivingEntity> livingEntityType = (EntityType<? extends LivingEntity>) entityType;
                    LOGGER.trace("Añadiendo atributos RPG a: {}", ForgeRegistries.ENTITY_TYPES.getKey(livingEntityType));
                    event.add(livingEntityType, AttributeRegistry.STRENGTH.get());
                    event.add(livingEntityType, AttributeRegistry.DEXTERITY.get());
                    event.add(livingEntityType, AttributeRegistry.VITALITY.get());
                    event.add(livingEntityType, AttributeRegistry.CONSTITUTION.get());
                    event.add(livingEntityType, AttributeRegistry.INTELLIGENCE.get());
                } catch (Exception e) {
                    LOGGER.error("Error añadiendo atributos RPG a {}: {}", ForgeRegistries.ENTITY_TYPES.getKey(entityType), e.getMessage());
                }
            }
        }
        LOGGER.debug("Atributos RPG añadidos a los tipos de LivingEntity.");
    }

    @SubscribeEvent
    public static void onBuildCreativeTabs(final BuildCreativeModeTabContentsEvent event) {
        LOGGER.info("MOD BUS EVENT: BuildCreativeModeTabContentsEvent para Tab: {}", event.getTabKey().location());
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            LOGGER.debug("Añadiendo {} a la pestaña COMBAT", ItemRegistry.ATTRIBUTE_POINT_GRANT.getId());
            event.accept(ItemRegistry.ATTRIBUTE_POINT_GRANT.get());
        }
    }
}