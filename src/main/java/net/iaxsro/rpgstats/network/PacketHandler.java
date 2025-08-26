package net.iaxsro.rpgstats.network;

import net.iaxsro.rpgstats.RpgStatsMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PacketHandler {

    private static final Logger LOGGER = RpgStatsMod.LOGGER;
    private static final String PROTOCOL_VERSION = "1"; // Versión del protocolo de red

    // El canal de comunicación principal para el mod
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(RpgStatsMod.MOD_ID, "main"), // Nombre único para el canal
            () -> PROTOCOL_VERSION, // Supplier para la versión del cliente
            PROTOCOL_VERSION::equals, // Predicado para aceptar versiones del cliente
            PROTOCOL_VERSION::equals  // Predicado para aceptar versiones del servidor
    );

    private static int packetId = 0; // Contador para IDs de paquetes

    // Método llamado durante FMLCommonSetupEvent para registrar todos los paquetes
    public static void register() {
        LOGGER.info("Registrando paquetes de red para {}", RpgStatsMod.MOD_ID);

        // Registra el paquete para sincronizar datos del jugador al cliente
        registerMessage(
                ClientboundSyncPlayerStatsPacket.class, // Clase del paquete
                ClientboundSyncPlayerStatsPacket::encode, // Método para codificar
                ClientboundSyncPlayerStatsPacket::new,    // Constructor/Método para decodificar (usando el constructor que acepta FriendlyByteBuf)
                ClientboundSyncPlayerStatsPacket::handle, // Método para manejar el paquete
                Optional.of(NetworkDirection.PLAY_TO_CLIENT) // Dirección del paquete (Servidor -> Cliente)
        );

        // --- Registra aquí otros paquetes si los necesitas ---
        /*
        registerMessage(ServerboundExamplePacket.class,
                        ServerboundExamplePacket::encode,
                        ServerboundExamplePacket::new,
                        ServerboundExamplePacket::handle,
                        Optional.of(NetworkDirection.PLAY_TO_SERVER));
        */

        LOGGER.info("Paquetes de red registrados.");
    }

    /**
     * Registra un tipo de mensaje (paquete) en el canal.
     *
     * @param messageType     La clase del mensaje.
     * @param encoder         Método para escribir el mensaje en el buffer.
     * @param decoder         Método para leer el mensaje del buffer.
     * @param messageConsumer Método para procesar el mensaje recibido.
     * @param direction       La dirección en la que se espera que viaje este paquete.
     * @param <MSG>           El tipo del mensaje.
     */
    private static <MSG> void registerMessage(Class<MSG> messageType,
                                              BiConsumer<MSG, FriendlyByteBuf> encoder,
                                              Function<FriendlyByteBuf, MSG> decoder,
                                              BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer,
                                              Optional<NetworkDirection> direction) {
        CHANNEL.registerMessage(packetId++, messageType, encoder, decoder, messageConsumer, direction);
        LOGGER.debug("Registrado paquete {} con ID {}", messageType.getSimpleName(), packetId - 1);
    }

    /**
     * Envía un paquete a un jugador específico.
     * Debe llamarse desde el lado lógico del servidor.
     *
     * @param player  El jugador destinatario.
     * @param message El paquete a enviar.
     * @param <MSG>   El tipo del paquete.
     */
    public static <MSG> void sendToPlayer(ServerPlayer player, MSG message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    /**
     * Envía un paquete al servidor.
     * Debe llamarse desde el lado lógico del cliente.
     *
     * @param message El paquete a enviar.
     * @param <MSG>   El tipo del paquete.
     */
    public static <MSG> void sendToServer(MSG message) {
        CHANNEL.sendToServer(message);
    }

    // Otros métodos de envío (sendToAll, sendToDimension, etc.) pueden añadirse si son necesarios.
}