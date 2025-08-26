package net.iaxsro.rpgstats.event;

import net.iaxsro.rpgstats.RpgStatsMod;
import net.iaxsro.rpgstats.capabilities.PlayerStats;
import net.iaxsro.rpgstats.network.ClientboundSyncPlayerStatsPacket;
import net.iaxsro.rpgstats.network.PacketHandler;
import net.iaxsro.rpgstats.system.AttributeCalculator;
import net.iaxsro.rpgstats.system.PersistenceService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.UUID;

// Escucha eventos en el bus de FORGE (implícito)
@Mod.EventBusSubscriber(modid = RpgStatsMod.MOD_ID)
public class PlayerLifecycleEvents {

    private static final org.slf4j.Logger LOGGER = RpgStatsMod.LOGGER;

    /**
     * Se dispara cuando un jugador inicia sesión en el servidor.
     * Sincroniza la capacidad completa al cliente que se conecta.
     */
    @SubscribeEvent
    public static void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        // Asegurarse de que es un jugador del servidor y no un FakePlayer o del lado cliente
        if (player instanceof ServerPlayer serverPlayer) {
            LOGGER.debug("Jugador {} iniciando sesión. Sincronizando PlayerStats.", serverPlayer.getName().getString());
            syncPlayerStats(serverPlayer);
        }
    }

    /**
     * Se dispara cuando un jugador reaparece (después de morir).
     * Sincroniza la capacidad al cliente y aplica lógica post-respawn.
     */
    @SubscribeEvent
    public static void onPlayerRespawn(final PlayerEvent.PlayerRespawnEvent event) {
        // Es mejor asegurarse de que es un ServerPlayer y no un FakePlayer
        if (event.getEntity() instanceof ServerPlayer player && !event.isEndConquered()) { // event.isEndConquered() evita ejecutar en respawn tras matar al dragón

            // Obtener la capacidad del jugador
            player.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY).ifPresent(stats -> {
                int currentLevel = stats.getLevel();
                UUID currentUUID = stats.getCurrentLevelUUID();

                // Calcular bonificaciones actuales
                AttributeCalculator.CalculatedBonuses currentBonuses = AttributeCalculator.calculateBonuses(player);

                // Obtener el UUID del nivel anterior (puede ser null si es nivel 0 o no se encuentra)
                @Nullable UUID previousUUID = (currentLevel > 0) ? PersistenceService.getUUIDForLevel(player, currentLevel - 1) : null;

                // ¡Llamada completa a applyAttributeModifiers!
                AttributeCalculator.applyAttributeModifiers(player, currentBonuses, currentLevel, currentUUID, previousUUID);
                RpgStatsMod.LOGGER.debug("Modificadores reaplicados para nivel {} (UUID: {}) tras respawn.", currentLevel, currentUUID); // Log para confirmar

                // Restaurar la salud al máximo DESPUÉS de aplicar modificadores
                player.setHealth(player.getMaxHealth());
                RpgStatsMod.LOGGER.debug("Salud restaurada tras respawn para {}.", player.getName().getString());

            }); // Fin de ifPresent

        } // Fin de if ServerPlayer

        Player player = event.getEntity();
        // El evento PlayerRespawnEvent solo se dispara en el servidor
        if (player instanceof ServerPlayer serverPlayer) {
            LOGGER.debug("Jugador {} reapareciendo. Sincronizando PlayerStats y aplicando lógica de respawn.", serverPlayer.getName().getString());
            // 1. Sincronizar datos al cliente
            syncPlayerStats(serverPlayer);

            // 2. Lógica adicional post-respawn (que estaba en PlayerDataClone.onPlayerRespawn)
            // Esta lógica debería idealmente estar en una clase de servicio como LevelingManager
            // para mantener los manejadores de eventos limpios.
            serverPlayer.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY).ifPresent(stats -> {
                // TODO: Mover esta lógica a LevelingManager.applyPostRespawnEffects(serverPlayer, stats);
                // Re-aplicar modificadores basados en el nivel actual (usando el UUID guardado en stats)
                if (stats.getCurrentLevelUUID() != null) {
                    // Asumiendo que existe algo como:
                    // LevelingManager.reapplyAttributeModifiers(serverPlayer, stats.getCurrentLevelUUID());
                    LOGGER.debug("Intentando reaplicar modificadores para el nivel UUID: {}", stats.getCurrentLevelUUID());
                    // Placeholder: Aquí iría la llamada real cuando LevelingManager exista
                } else {
                    LOGGER.warn("No se encontró UUID de nivel en PlayerStats para {} al respawnear.", serverPlayer.getName().getString());
                }
                // Restaurar vida al máximo después de aplicar modificadores de vida máxima
                // Es importante hacerlo DESPUÉS de reaplicar modificadores que afecten MaxHealth.
                serverPlayer.setHealth(serverPlayer.getMaxHealth());
                LOGGER.debug("Vida restaurada para {}.", serverPlayer.getName().getString());
            });
        }
    }

    /**
     * Se dispara cuando un jugador cambia de dimensión.
     * Sincroniza la capacidad al cliente.
     */
    @SubscribeEvent
    public static void onPlayerChangeDimension(final PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        // Asegurarse de que es un jugador del servidor
        if (player instanceof ServerPlayer serverPlayer) {
            LOGGER.debug("Jugador {} cambió de dimensión. Sincronizando PlayerStats.", serverPlayer.getName().getString());
            syncPlayerStats(serverPlayer);
        }
    }

    /**
     * Método helper para obtener la capacidad y enviar el paquete de sincronización.
     *
     * @param serverPlayer El jugador al que sincronizar los datos.
     */
    private static void syncPlayerStats(ServerPlayer serverPlayer) {
        serverPlayer.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY).ifPresent(stats -> {
            ClientboundSyncPlayerStatsPacket packet = new ClientboundSyncPlayerStatsPacket(stats.writeNBT());
            PacketHandler.sendToPlayer(serverPlayer, packet);
            LOGGER.trace("Paquete SyncPlayerStats enviado a {}", serverPlayer.getName().getString());
        });
    }
}