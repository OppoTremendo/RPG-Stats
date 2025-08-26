package net.iaxsro.rpgstats.client.event;

import net.iaxsro.rpgstats.RpgStatsMod;
import net.iaxsro.rpgstats.capabilities.IPlayerStats;
import net.iaxsro.rpgstats.capabilities.PlayerStats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maneja eventos del cliente relacionados con la actualización de datos.
 * Se asegura de que la GUI se actualice cuando cambien los datos del jugador.
 */
@Mod.EventBusSubscriber(modid = RpgStatsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientDataEvents {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientDataEvents.class);

    // Cache para detectar cambios en los datos
    private static double lastStrength = -1;
    private static double lastDexterity = -1;
    private static double lastVitality = -1;
    private static double lastConstitution = -1;
    private static double lastIntelligence = -1;
    private static boolean dataChanged = false;

    /**
     * Verifica cambios en los datos del jugador cada tick del cliente
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        if (player == null) {
            return;
        }

        // Verificar cambios en los datos cada 20 ticks (1 segundo)
        if (minecraft.level.getGameTime() % 20 == 0) {
            checkForDataChanges(player);
        }
    }

    /**
     * Verifica si han cambiado los datos del jugador
     */
    private static void checkForDataChanges(LocalPlayer player) {
        LazyOptional<IPlayerStats> statsOptional = player.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY);

        statsOptional.ifPresent(stats -> {
            double currentStrength = stats.getStrengthPoints();
            double currentDexterity = stats.getDexterityPoints();
            double currentVitality = stats.getVitalityPoints();
            double currentConstitution = stats.getConstitutionPoints();
            double currentIntelligence = stats.getIntelligencePoints();

            // Verificar si algún valor ha cambiado
            if (lastStrength != currentStrength ||
                    lastDexterity != currentDexterity ||
                    lastVitality != currentVitality ||
                    lastConstitution != currentConstitution ||
                    lastIntelligence != currentIntelligence) {

                // Actualizar cache
                lastStrength = currentStrength;
                lastDexterity = currentDexterity;
                lastVitality = currentVitality;
                lastConstitution = currentConstitution;
                lastIntelligence = currentIntelligence;

                dataChanged = true;

                // Log para debugging (opcional)
                if (RpgStatsMod.LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Datos de atributos actualizados - STR: {}, DEX: {}, VIT: {}, CON: {}, INT: {}",
                            currentStrength, currentDexterity, currentVitality, currentConstitution, currentIntelligence);
                }
            }
        });
    }

    /**
     * Getter para verificar si los datos han cambiado (usado por el overlay)
     */
    public static boolean hasDataChanged() {
        boolean changed = dataChanged;
        dataChanged = false; // Reset flag
        return changed;
    }
}