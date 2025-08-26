package net.iaxsro.rpgstats;

import com.mojang.logging.LogUtils;
import net.iaxsro.rpgstats.client.ClientSetup;
import net.iaxsro.rpgstats.event.ModBusEvents;
import net.iaxsro.rpgstats.network.PacketHandler;
import net.iaxsro.rpgstats.registry.AttributeRegistry;
import net.iaxsro.rpgstats.registry.CreativeTabRegistry;
import net.iaxsro.rpgstats.registry.ItemRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// El valor aquí debe coincidir con tu mod id en mods.toml
@Mod(RpgStatsMod.MOD_ID)
public class RpgStatsMod {

    // Define el Mod ID como una constante pública
    public static final String MOD_ID = "rpgstats"; // Cambiado de "rpg_attribute_system"

    // Logger para el mod
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(6);

    public RpgStatsMod() {
        // --- Inicialización Principal ---

        // Obtiene el bus de eventos del Mod (para registros y FML lifecycle events)
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // --- Registro Diferido ---
        // Llama a los métodos 'register' de tus clases de registro.
        // Estas clases contendrán las instancias de DeferredRegister.
        LOGGER.info("Registrando Items para {}", MOD_ID);
        ItemRegistry.register(modEventBus);
        LOGGER.info("Registrando Atributos para {}", MOD_ID);
        AttributeRegistry.register(modEventBus);
        LOGGER.info("Registrando Pestañas Creativas para {}", MOD_ID);
        CreativeTabRegistry.register(modEventBus); // Asegúrate que esta clase exista

        // --- Registro de Eventos del Mod ---
        // Registra el listener para eventos como FMLCommonSetupEvent.
        // Moveremos la lógica de `FMLCommonSetupEvent` a ModBusEvents.
        modEventBus.addListener(this::commonSetup);
        // También puedes registrar clases enteras que contengan @SubscribeEvent para el Mod Bus:
        modEventBus.register(ModBusEvents.class); // Registra todos los @SubscribeEvent estáticos en ModBusEvents

        // --- Registro de Eventos de Forge ---
        // Registra manejadores para eventos del juego (ticks, login, etc.).
        // Es más limpio registrar clases dedicadas en lugar de 'this'.
        MinecraftForge.EVENT_BUS.register(new ModBusEvents()); // Registra una instancia de la clase que manejará eventos de Forge

        // --- Inicialización del Cliente ---
        // Solo ejecutar en el lado del cliente
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientSetup::init);

        // --- Registro de Configuración ---
        // La configuración se registra a través de su propia clase, usualmente en el Mod Bus.
        // ConfigRegistration.register(); // No es necesario llamar aquí si se hace con @Mod.EventBusSubscriber en ConfigRegistration

        LOGGER.info("{} Mod inicializado.", MOD_ID);
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        // Shuts down the executor, allowing it to complete any queued tasks
        // and then terminate its threads.
        EXECUTOR.shutdown();
    }

    /**
     * Método para FMLCommonSetupEvent. Se ejecuta después de que los registros están completos.
     * Bueno para registrar network handlers o configurar cosas que dependen de registros.
     * La lógica específica se moverá a ModBusEvents.
     */
    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Iniciando Common Setup para {}", MOD_ID);
        event.enqueueWork(() -> {

            // --- Configuración de Red ---
            // La inicialización del PacketHandler (registro de mensajes) va aquí.
            PacketHandler.register();
            LOGGER.info("Network Handler registrado.");

            // Otras inicializaciones que deben ocurrir después del registro, como:
            // - Spawn Placements para entidades (se hará en ModBusEvents con SpawnPlacementRegisterEvent)
            // - Modificaciones de Trades de Aldeanos
            // - Configuración de relaciones entre pociones
        });
    }

    // --- Lógica Movida ---
    // - El SimpleChannel (PACKET_HANDLER) y addNetworkMessage se moverán a network/PacketHandler.java
    // - La cola de trabajo (workQueue, queueServerWork, tick) se moverá a event/ForgeBusEvents.java (o una clase similar).
    // - TextboxSetMessage se moverá a network/TextboxSetMessagePacket.java (o similar, si se mantiene).
    // - La lógica dentro de la clase interna 'initer' se moverá a event/ModBusEvents.java.
}