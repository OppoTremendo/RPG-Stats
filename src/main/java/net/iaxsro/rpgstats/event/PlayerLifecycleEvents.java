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
        if (!(event.getEntity() instanceof ServerPlayer player) || event.isEndConquered()) {
            return; // Evita ejecutar en respawns especiales como tras matar al dragón
        }

        LOGGER.debug("Jugador {} reapareciendo. Reaplicando atributos y sincronizando PlayerStats.",
                player.getName().getString());

        // Reaplicar modificadores y restaurar salud
        player.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY).ifPresent(stats -> {
            int currentLevel = stats.getLevel();
            UUID currentUUID = PersistenceService.getUUIDForLevel(player, currentLevel);

            AttributeCalculator.CalculatedBonuses currentBonuses = AttributeCalculator.calculateBonuses(player);
            @Nullable UUID previousUUID = currentLevel > 0
                    ? PersistenceService.getUUIDForLevel(player, currentLevel - 1)
                    : null;

            AttributeCalculator.applyAttributeModifiers(player, currentBonuses, currentLevel, currentUUID, previousUUID);
            RpgStatsMod.LOGGER.debug("Modificadores reaplicados para nivel {} (UUID: {}) tras respawn.",
                    currentLevel, currentUUID);

            player.setHealth(player.getMaxHealth());
            RpgStatsMod.LOGGER.debug("Salud restaurada tras respawn para {}.", player.getName().getString());
        });

        // Finalmente sincronizar los datos al cliente
        syncPlayerStats(player);
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