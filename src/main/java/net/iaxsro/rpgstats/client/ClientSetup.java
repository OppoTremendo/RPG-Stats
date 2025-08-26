package net.iaxsro.rpgstats.client;

import net.iaxsro.rpgstats.RpgStatsMod;
import net.iaxsro.rpgstats.client.event.ClientAttributeEvents;
import net.iaxsro.rpgstats.client.event.ClientDataEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase principal para la configuración del lado del cliente.
 * Maneja la inicialización de elementos específicos del cliente como GUIs y overlays.
 */
@Mod.EventBusSubscriber(modid = RpgStatsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientSetup.class);

    /**
     * Inicializa la configuración del cliente
     */
    public static void init() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        LOGGER.info("Iniciando configuración del cliente para RPG Stats...");

        // Registrar eventos del mod bus (para registro de overlays)
        modEventBus.addListener(ClientSetup::onClientSetup);

        // Registrar eventos de Forge (para monitoreo de datos)
        LOGGER.info("Registrando eventos de Forge del cliente...");
        MinecraftForge.EVENT_BUS.register(ClientDataEvents.class);
        MinecraftForge.EVENT_BUS.register(ClientAttributeEvents.class);

        LOGGER.info("Configuración del cliente inicializada para RPG Stats");
    }

    /**
     * Evento de configuración del cliente
     */
    private static void onClientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            try {
                LOGGER.info("Ejecutando configuración del cliente...");

                // Aquí se pueden agregar configuraciones adicionales del cliente
                LOGGER.info("Configuración del cliente completada exitosamente");

            } catch (Exception e) {
                LOGGER.error("Error durante la configuración del cliente: {}", e.getMessage(), e);
            }
        });
    }
}