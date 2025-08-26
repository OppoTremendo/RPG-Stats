package net.iaxsro.rpgstats.client.event;

import net.iaxsro.rpgstats.RpgStatsMod;
import net.iaxsro.rpgstats.client.gui.AttributeValuesOverlay;
import net.iaxsro.rpgstats.registry.AttributeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maneja eventos del cliente relacionados con la actualización de valores de atributos.
 * Monitorea cambios en los atributos reales del jugador y actualiza el overlay correspondiente.
 */
@Mod.EventBusSubscriber(modid = RpgStatsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientAttributeEvents {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientAttributeEvents.class);
    private static final long CHECK_INTERVAL = 1000; // Verificar cada segundo
    // Cache para detectar cambios en los atributos
    private static double lastStrength = -1;
    private static double lastDexterity = -1;
    private static double lastVitality = -1;
    private static double lastConstitution = -1;
    private static double lastIntelligence = -1;
    private static long lastCheckTime = 0;

    /**
     * Verifica cambios en los atributos del jugador cada cierto tiempo
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

        long currentTime = System.currentTimeMillis();

        // Verificar cambios en los atributos cada segundo
        if (currentTime - lastCheckTime >= CHECK_INTERVAL) {
            checkForAttributeChanges(player);
            lastCheckTime = currentTime;
        }
    }

    /**
     * Verifica si han cambiado los valores de atributos del jugador
     */
    private static void checkForAttributeChanges(LocalPlayer player) {
        try {
            double currentStrength = player.getAttributeValue(AttributeRegistry.STRENGTH.get());
            double currentDexterity = player.getAttributeValue(AttributeRegistry.DEXTERITY.get());
            double currentVitality = player.getAttributeValue(AttributeRegistry.VITALITY.get());
            double currentConstitution = player.getAttributeValue(AttributeRegistry.CONSTITUTION.get());
            double currentIntelligence = player.getAttributeValue(AttributeRegistry.INTELLIGENCE.get());

            // Verificar si algún valor ha cambiado (con tolerancia para errores de punto flotante)
            boolean changed = false;
            if (Math.abs(lastStrength - currentStrength) > 0.01 ||
                    Math.abs(lastDexterity - currentDexterity) > 0.01 ||
                    Math.abs(lastVitality - currentVitality) > 0.01 ||
                    Math.abs(lastConstitution - currentConstitution) > 0.01 ||
                    Math.abs(lastIntelligence - currentIntelligence) > 0.01) {

                changed = true;

                // Actualizar cache
                lastStrength = currentStrength;
                lastDexterity = currentDexterity;
                lastVitality = currentVitality;
                lastConstitution = currentConstitution;
                lastIntelligence = currentIntelligence;

                // Forzar actualización del overlay
                AttributeValuesOverlay.forceUpdate();

                // Log para debugging
                if (RpgStatsMod.LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Cambio detectado en atributos - STR: {}, DEX: {}, VIT: {}, CON: {}, INT: {}",
                            currentStrength, currentDexterity, currentVitality, currentConstitution, currentIntelligence);
                }
            }

            // Log inicial si es la primera vez
            if (lastStrength == -1) {
                lastStrength = currentStrength;
                lastDexterity = currentDexterity;
                lastVitality = currentVitality;
                lastConstitution = currentConstitution;
                lastIntelligence = currentIntelligence;

                LOGGER.info("Valores iniciales de atributos registrados");
            }

        } catch (Exception e) {
            LOGGER.error("Error al verificar cambios en atributos: {}", e.getMessage(), e);
        }
    }

    /**
     * Resetea el cache de atributos (útil para debugging o reinicios)
     */
    public static void resetCache() {
        lastStrength = -1;
        lastDexterity = -1;
        lastVitality = -1;
        lastConstitution = -1;
        lastIntelligence = -1;
        lastCheckTime = 0;
        AttributeValuesOverlay.forceUpdate();
        LOGGER.info("Cache de atributos reseteado");
    }
}