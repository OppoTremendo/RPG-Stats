package net.iaxsro.rpgstats.client.gui;

import net.iaxsro.rpgstats.registry.AttributeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Overlay GUI que muestra los valores de atributos reales del jugador.
 * Se posiciona en la esquina inferior izquierda y se actualiza solo cuando cambian los atributos.
 */
public class AttributeValuesOverlay implements IGuiOverlay {

    private static final Logger LOGGER = LoggerFactory.getLogger(AttributeValuesOverlay.class);

    // Configuración de posición y estilo
    private static final int MARGIN_LEFT = 10;
    private static final int MARGIN_BOTTOM = 10;
    private static final int LINE_HEIGHT = 12;
    private static final int BACKGROUND_PADDING = 4;
    private static final int BACKGROUND_COLOR = 0x80000000; // Negro semi-transparente
    private static final int TEXT_COLOR = 0xFFFFFF; // Blanco
    private static final int HEADER_COLOR = 0xFF4169E1; // Azul real
    private static final int VALUE_COLOR = 0xFFFFD700; // Dorado para los valores
    private static final long UPDATE_INTERVAL = 2000; // Actualizar cada 2 segundos (reducido spam)
    // Variables para optimización y cache
    private static boolean isVisible = true;
    private static double lastStrength = -1;
    private static double lastDexterity = -1;
    private static double lastVitality = -1;
    private static double lastConstitution = -1;
    private static double lastIntelligence = -1;
    private static String[] cachedLines = null;
    private static long lastUpdateTime = 0;

    /**
     * Alterna la visibilidad del overlay
     */
    public static void toggleVisibility() {
        isVisible = !isVisible;
        if (!isVisible) {
            cachedLines = null; // Limpiar cache cuando se oculta
        }
        LOGGER.debug("Overlay de valores de atributos {}", isVisible ? "activado" : "desactivado");
    }

    /**
     * Actualiza el cache de atributos si han cambiado
     */

    /**
     * Obtiene el estado de visibilidad del overlay
     */
    public static boolean isVisible() {
        return isVisible;
    }

    /**
     * Establece la visibilidad del overlay
     */
    public static void setVisible(boolean visible) {
        isVisible = visible;
        if (!visible) {
            cachedLines = null; // Limpiar cache cuando se oculta
        }
    }

    /**
     * Fuerza la actualización del cache en la próxima renderización
     */
    public static void forceUpdate() {
        cachedLines = null;
        lastUpdateTime = 0;
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        if (!isVisible) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        // Verificar que el jugador existe y está en el juego
        if (player == null || minecraft.screen != null) {
            return; // No mostrar en menús
        }

        // Verificar si el HUD está oculto (F1)
        if (!minecraft.options.hideGui) {
            try {
                // Verificar si necesitamos actualizar los datos
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastUpdateTime > UPDATE_INTERVAL || cachedLines == null) {
                    updateAttributeCache(player);
                    lastUpdateTime = currentTime;
                }

                if (cachedLines != null) {
                    renderAttributeValues(guiGraphics, minecraft.font, screenWidth, screenHeight);
                }
            } catch (Exception e) {
                LOGGER.error("Error al renderizar overlay de valores de atributos: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Actualiza el cache de atributos si han cambiado
     */
    private void updateAttributeCache(LocalPlayer player) {
        try {
            // Verificar que los atributos están registrados
            if (AttributeRegistry.STRENGTH.get() == null) {
                LOGGER.warn("STRENGTH attribute is null!");
                return;
            }

            // Obtener valores actuales de atributos
            double currentStrength = 0.0;
            double currentDexterity = 0.0;
            double currentVitality = 0.0;
            double currentConstitution = 0.0;
            double currentIntelligence = 0.0;

            try {
                currentStrength = player.getAttributeValue(AttributeRegistry.STRENGTH.get());
                currentDexterity = player.getAttributeValue(AttributeRegistry.DEXTERITY.get());
                currentVitality = player.getAttributeValue(AttributeRegistry.VITALITY.get());
                currentConstitution = player.getAttributeValue(AttributeRegistry.CONSTITUTION.get());
                currentIntelligence = player.getAttributeValue(AttributeRegistry.INTELLIGENCE.get());
            } catch (Exception e) {
                LOGGER.error("Error obteniendo valores de atributos: {}", e.getMessage());
                // Usar valores por defecto para mostrar algo
                currentStrength = 0.0;
                currentDexterity = 0.0;
                currentVitality = 0.0;
                currentConstitution = 0.0;
                currentIntelligence = 0.0;
            }

            // Verificar si algún valor ha cambiado significativamente
            boolean hasChanged = Math.abs(currentStrength - lastStrength) > 0.01 ||
                    Math.abs(currentDexterity - lastDexterity) > 0.01 ||
                    Math.abs(currentVitality - lastVitality) > 0.01 ||
                    Math.abs(currentConstitution - lastConstitution) > 0.01 ||
                    Math.abs(currentIntelligence - lastIntelligence) > 0.01;

            if (hasChanged || cachedLines == null) {
                // Actualizar valores almacenados
                lastStrength = currentStrength;
                lastDexterity = currentDexterity;
                lastVitality = currentVitality;
                lastConstitution = currentConstitution;
                lastIntelligence = currentIntelligence;

                // Regenerar líneas de texto
                cachedLines = new String[]{
                        Component.translatable("overlay.attribute_values.header").getString(),
                        String.format("%s: %.1f", Component.translatable("attribute.strength").getString(), currentStrength),
                        String.format("%s: %.1f", Component.translatable("attribute.dexterity").getString(), currentDexterity),
                        String.format("%s: %.1f", Component.translatable("attribute.vitality").getString(), currentVitality),
                        String.format("%s: %.1f", Component.translatable("attribute.constitution").getString(), currentConstitution),
                        String.format("%s: %.1f", Component.translatable("attribute.intelligence").getString(), currentIntelligence)
                };
            }
        } catch (Exception e) {
            LOGGER.error("Error al actualizar cache de atributos: {}", e.getMessage(), e);

            // Crear cache de emergencia
            cachedLines = new String[]{
                    Component.translatable("overlay.attribute_values.header").getString(),
                    Component.translatable("overlay.attribute_values.error.strength").getString(),
                    Component.translatable("overlay.attribute_values.error.dexterity").getString(),
                    Component.translatable("overlay.attribute_values.error.vitality").getString(),
                    Component.translatable("overlay.attribute_values.error.constitution").getString(),
                    Component.translatable("overlay.attribute_values.error.intelligence").getString()
            };
        }
    }

    /**
     * Renderiza los valores de atributos en la pantalla
     */
    private void renderAttributeValues(GuiGraphics guiGraphics, Font font, int screenWidth, int screenHeight) {
        if (cachedLines == null) {
            return;
        }

        // Siempre mostrar los atributos, independientemente de sus valores
        // (Comentado el filtro de valores significativos para debugging)
        /*
        boolean hasSignificantValues = false;
        for (int i = 1; i < cachedLines.length; i++) { // Saltar el header
            String line = cachedLines[i];
            String[] parts = line.split(": ");
            if (parts.length == 2) {
                try {
                    double value = Double.parseDouble(parts[1]);
                    if (value > 0.1) { // Mostrar si el valor es mayor a 0.1
                        hasSignificantValues = true;
                        break;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        if (!hasSignificantValues) {
            return; // No mostrar si no hay valores significativos
        }
        */

        // Calcular dimensiones del fondo
        int maxWidth = 0;
        for (String line : cachedLines) {
            int lineWidth = font.width(line);
            if (lineWidth > maxWidth) {
                maxWidth = lineWidth;
            }
        }

        int totalHeight = cachedLines.length * LINE_HEIGHT;
        int backgroundWidth = maxWidth + (BACKGROUND_PADDING * 2);
        int backgroundHeight = totalHeight + (BACKGROUND_PADDING * 2);

        // Calcular posición (esquina inferior izquierda)
        int x = MARGIN_LEFT;
        int y = screenHeight - backgroundHeight - MARGIN_BOTTOM;

        // Renderizar fondo semi-transparente con borde
        guiGraphics.fill(x, y, x + backgroundWidth, y + backgroundHeight, BACKGROUND_COLOR);

        // Borde sutil
        guiGraphics.fill(x, y, x + backgroundWidth, y + 1, 0xFF555555); // Top
        guiGraphics.fill(x, y, x + 1, y + backgroundHeight, 0xFF555555); // Left
        guiGraphics.fill(x + backgroundWidth - 1, y, x + backgroundWidth, y + backgroundHeight, 0xFF555555); // Right
        guiGraphics.fill(x, y + backgroundHeight - 1, x + backgroundWidth, y + backgroundHeight, 0xFF555555); // Bottom

        // Renderizar texto
        int textX = x + BACKGROUND_PADDING;
        int textY = y + BACKGROUND_PADDING;

        for (int i = 0; i < cachedLines.length; i++) {
            int color;
            if (i == 0) {
                color = HEADER_COLOR; // Primera línea en azul
            } else {
                // Separar nombre y valor para diferentes colores
                String line = cachedLines[i];
                String[] parts = line.split(": ");
                if (parts.length == 2) {
                    // Renderizar nombre del atributo
                    guiGraphics.drawString(font, parts[0] + ": ", textX, textY + (i * LINE_HEIGHT), TEXT_COLOR, false);
                    // Renderizar valor en dorado
                    int nameWidth = font.width(parts[0] + ": ");
                    guiGraphics.drawString(font, parts[1], textX + nameWidth, textY + (i * LINE_HEIGHT), VALUE_COLOR, false);
                    continue;
                }
                color = TEXT_COLOR;
            }
            guiGraphics.drawString(font, cachedLines[i], textX, textY + (i * LINE_HEIGHT), color, false);
        }
    }
}