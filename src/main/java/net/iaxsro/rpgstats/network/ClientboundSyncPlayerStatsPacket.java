package net.iaxsro.rpgstats.network;

import net.iaxsro.rpgstats.capabilities.PlayerStats;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * Paquete enviado DESDE el servidor HACIA el cliente para sincronizar
 * los datos completos de la capacidad PlayerStats.
 */
public class ClientboundSyncPlayerStatsPacket {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientboundSyncPlayerStatsPacket.class);
    private final CompoundTag dataTag; // Usamos NBT para transferir los datos

    /**
     * Constructor para crear el paquete ANTES de enviarlo (lado servidor).
     *
     * @param dataTag El NBT completo de la capacidad PlayerStats.
     */
    public ClientboundSyncPlayerStatsPacket(CompoundTag dataTag) {
        this.dataTag = dataTag;
        LOGGER.trace("Paquete SyncPlayerStats creado con tag: {}", dataTag); // Trace para debugging
    }

    /**
     * Constructor para decodificar el paquete DESPUÉS de recibirlo (lado cliente).
     * Lee el NBT del buffer.
     *
     * @param buffer El buffer de bytes recibido.
     */
    public ClientboundSyncPlayerStatsPacket(FriendlyByteBuf buffer) {
        this.dataTag = buffer.readNbt();
        LOGGER.trace("Paquete SyncPlayerStats decodificado con tag: {}", dataTag); // Trace para debugging
        if (this.dataTag == null) {
            LOGGER.warn("Se recibió un paquete SyncPlayerStats con NBT nulo!");
            // Considera lanzar una excepción o usar un tag vacío por defecto
            // throw new IllegalStateException("Received null NBT tag in SyncPlayerStatsPacket");
        }
    }

    /**
     * Método estático para codificar el paquete en el buffer (lado servidor).
     *
     * @param message El paquete a codificar.
     * @param buffer  El buffer donde escribir.
     */
    public static void encode(ClientboundSyncPlayerStatsPacket message, FriendlyByteBuf buffer) {
        buffer.writeNbt(message.dataTag);
        LOGGER.trace("Paquete SyncPlayerStats codificado.");
    }

    /**
     * Método estático para manejar el paquete recibido (lado cliente).
     * Se asegura de ejecutarse en el hilo principal del cliente.
     *
     * @param message         El paquete recibido.
     * @param contextSupplier Supplier para el contexto de red.
     */
    public static void handle(ClientboundSyncPlayerStatsPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // Código ejecutado en el hilo principal del CLIENTE
            Player player = Minecraft.getInstance().player;
            if (player != null && message.dataTag != null) {
                // Obtiene la capacidad del jugador del lado del cliente
                player.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY).ifPresent(playerStats -> {
                    // Actualiza los datos de la capacidad usando el NBT recibido
                    playerStats.readNBT(message.dataTag); // Asume que PlayerStats tiene readNBT
                    LOGGER.debug("Capacidad PlayerStats del cliente actualizada.");
                });
            } else {
                LOGGER.warn("No se pudo manejar SyncPlayerStats: Player es null o dataTag es null.");
            }
        });
        context.setPacketHandled(true); // Marca el paquete como procesado
    }
}