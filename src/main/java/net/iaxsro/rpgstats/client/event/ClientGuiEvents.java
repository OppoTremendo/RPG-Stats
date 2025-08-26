package net.iaxsro.rpgstats.client.event;

import net.iaxsro.rpgstats.RpgStatsMod;
import net.iaxsro.rpgstats.client.gui.AttributePointsOverlay;
import net.iaxsro.rpgstats.client.gui.AttributeValuesOverlay;
import net.iaxsro.rpgstats.client.gui.ModifiedAttributesOverlay;
import net.iaxsro.rpgstats.client.gui.SimpleTestOverlay;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maneja los eventos del cliente relacionados con la GUI.
 * Registra los overlays de puntos de atributos, valores de atributos y test overlay.
 */
@Mod.EventBusSubscriber(modid = RpgStatsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientGuiEvents {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientGuiEvents.class);

    /**
     * Registra los overlays de GUI personalizados
     */
    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        try {
            LOGGER.info("Iniciando registro de overlays de GUI...");

            // Registrar el overlay de puntos de atributos (esquina inferior derecha)
            event.registerAbove(
                    VanillaGuiOverlay.HOTBAR.id(), // Renderizar encima del hotbar
                    "attribute_points", // Nombre del overlay
                    new AttributePointsOverlay()
            );
            LOGGER.info("Overlay de puntos de atributos registrado exitosamente");

            // Registrar el overlay de valores de atributos (esquina inferior izquierda)
            event.registerAbove(
                    VanillaGuiOverlay.HOTBAR.id(), // Renderizar encima del hotbar
                    "attribute_values", // Nombre del overlay
                    new AttributeValuesOverlay()
            );
            LOGGER.info("Overlay de valores de atributos registrado exitosamente");

            // Registrar el overlay de atributos modificados (esquina superior derecha)
            event.registerAbove(
                    VanillaGuiOverlay.HOTBAR.id(), // Renderizar encima del hotbar
                    "modified_attributes", // Nombre del overlay
                    new ModifiedAttributesOverlay()
            );
            LOGGER.info("Overlay de atributos modificados registrado exitosamente");

            // Registrar el overlay de test (para debugging - oculto por defecto)
            event.registerAbove(
                    VanillaGuiOverlay.HOTBAR.id(),
                    "test_overlay",
                    new SimpleTestOverlay()
            );
            LOGGER.info("Test overlay registrado exitosamente (oculto por defecto)");

            LOGGER.info("Todos los overlays registrados exitosamente:");
            LOGGER.info("  - Overlay de puntos de atributos (esquina inferior derecha)");
            LOGGER.info("  - Overlay de valores de atributos (esquina inferior izquierda)");
            LOGGER.info("  - Overlay de atributos modificados (esquina superior derecha)");
            LOGGER.info("  - Test overlay (debugging - oculto por defecto)");

        } catch (Exception e) {
            LOGGER.error("Error al registrar overlays de GUI: {}", e.getMessage(), e);
        }
    }
}