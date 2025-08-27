package net.iaxsro.rpgstats.event;

import net.iaxsro.rpgstats.RpgStatsMod;
import net.iaxsro.rpgstats.capabilities.PlayerStats;
import net.iaxsro.rpgstats.network.ClientboundSyncPlayerStatsPacket;
import net.iaxsro.rpgstats.network.PacketHandler;
import net.iaxsro.rpgstats.registry.AttributeRegistry;
import net.iaxsro.rpgstats.system.LevelingManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.iaxsro.rpgstats.attributesystem.EventListeners;


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
     * Se dispara cuando un jugador es clonado (generalmente al morir y respawnear).
     * Copia los datos de la capacidad y los atributos base personalizados al nuevo jugador.
     */
    @SubscribeEvent
    public static void onPlayerClone(final PlayerEvent.Clone event) {
        if (!event.isWasDeath()) {
            return; // Solo nos interesa cuando el jugador murió
        }

        Player original = event.getOriginal();
        Player clone = event.getEntity();

        // Asegurarnos de que las capacidades originales estén accesibles
        original.reviveCaps();
        original.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY).ifPresent(oldStats -> {
            clone.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY).ifPresent(newStats -> {
                newStats.readNBT(oldStats.writeNBT());
            });
        });

        // Copiar valores base de los atributos personalizados registrados por el mod
        copyBaseAttribute(original, clone, AttributeRegistry.STRENGTH.get());
        copyBaseAttribute(original, clone, AttributeRegistry.DEXTERITY.get());
        copyBaseAttribute(original, clone, AttributeRegistry.VITALITY.get());
        copyBaseAttribute(original, clone, AttributeRegistry.CONSTITUTION.get());
        copyBaseAttribute(original, clone, AttributeRegistry.INTELLIGENCE.get());

        original.invalidateCaps(); // Evitar fugas de memoria
    }

    private static void copyBaseAttribute(Player from, Player to, Attribute attribute) {
        AttributeInstance fromInst = from.getAttribute(attribute);
        AttributeInstance toInst = to.getAttribute(attribute);
        if (fromInst != null && toInst != null) {
            toInst.setBaseValue(fromInst.getBaseValue());
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

        // Reaplicar modificadores y restaurar salud usando el LevelingManager
        LevelingManager.applyPostRespawnEffects(player);

        // Finalmente sincronizar los datos al cliente
        syncPlayerStats(player);
        EventListeners.applyAllEventListeners(player);
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
