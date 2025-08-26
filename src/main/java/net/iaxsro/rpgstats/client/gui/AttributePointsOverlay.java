package net.iaxsro.rpgstats.client.gui;

import net.iaxsro.rpgstats.capabilities.IPlayerStats;
import net.iaxsro.rpgstats.capabilities.PlayerStats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.common.util.LazyOptional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Overlay GUI que muestra los puntos de atributos del jugador en la esquina inferior derecha.
 * Se actualiza automáticamente cuando cambian los valores.
 */
public class AttributePointsOverlay implements IGuiOverlay {

    private static final Logger LOGGER = LoggerFactory.getLogger(AttributePointsOverlay.class);

    // Configuración de posición y estilo
    private static final int MARGIN_RIGHT = 10;
    private static final int MARGIN_BOTTOM = 10;
    private static final int LINE_HEIGHT = 12;
    private static final int BACKGROUND_PADDING = 4;
    private static final int BACKGROUND_COLOR = 0x80000000; // Negro semi-transparente
    private static final int TEXT_COLOR = 0xFFFFFF; // Blanco
    private static final int HEADER_COLOR = 0xFFD700; // Dorado
    private static final int VALUE_COLOR = 0x00FF00; // Verde para los valores
    private static final long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL = 2000; // Actualizar cada 2 segundos (reducido spam)
    // Variables para optimización
    private static boolean isVisible = true;

    /**
     * Alterna la visibilidad del overlay
     */
    public static void toggleVisibility() {
        isVisible = !isVisible;
        LOGGER.debug("Overlay de puntos de atributos {}", isVisible ? "activado" : "desactivado");
    }

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
            // Obtener la capacidad del jugador
            LazyOptional<IPlayerStats> statsOptional = player.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY);

            if (statsOptional.isPresent()) {
                statsOptional.ifPresent(stats -> {
                    try {
                        renderAttributePoints(guiGraphics, minecraft.font, screenWidth, screenHeight, stats);
                    } catch (Exception e) {
                        LOGGER.error("Error al renderizar overlay de puntos de atributos: {}", e.getMessage(), e);
                    }
                });
            }
        }
    }

    /**
     * Renderiza los puntos de atributos en la pantalla
     */
    private void renderAttributePoints(GuiGraphics guiGraphics, Font font, int screenWidth, int screenHeight, IPlayerStats stats) {
        // Verificar si hay puntos para mostrar
        double totalPoints = stats.getStrengthPoints() + stats.getDexterityPoints() +
                stats.getVitalityPoints() + stats.getConstitutionPoints() +
                stats.getIntelligencePoints();

        if (totalPoints <= 0) {
            return; // No mostrar si no hay puntos
        }

        // Preparar las líneas de texto
        String[] lines = {
                Component.translatable("overlay.attribute_points.header").getString(),
                String.format("%s: %.1f", Component.translatable("attribute.strength").getString(), stats.getStrengthPoints()),
                String.format("%s: %.1f", Component.translatable("attribute.dexterity").getString(), stats.getDexterityPoints()),
                String.format("%s: %.1f", Component.translatable("attribute.vitality").getString(), stats.getVitalityPoints()),
                String.format("%s: %.1f", Component.translatable("attribute.constitution").getString(), stats.getConstitutionPoints()),
                String.format("%s: %.1f", Component.translatable("attribute.intelligence").getString(), stats.getIntelligencePoints())
        };

        // Calcular dimensiones del fondo
        int maxWidth = 0;
        for (String line : lines) {
            int lineWidth = font.width(line);
            if (lineWidth > maxWidth) {
                maxWidth = lineWidth;
            }
        }

        int totalHeight = lines.length * LINE_HEIGHT;
        int backgroundWidth = maxWidth + (BACKGROUND_PADDING * 2);
        int backgroundHeight = totalHeight + (BACKGROUND_PADDING * 2);

        // Calcular posición (esquina inferior derecha)
        int x = screenWidth - backgroundWidth - MARGIN_RIGHT;
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

        for (int i = 0; i < lines.length; i++) {
            int color;
            if (i == 0) {
                color = HEADER_COLOR; // Primera línea en dorado
            } else {
                // Separar nombre y valor para diferentes colores
                String line = lines[i];
                String[] parts = line.split(": ");
                if (parts.length == 2) {
                    // Renderizar nombre del atributo
                    guiGraphics.drawString(font, parts[0] + ": ", textX, textY + (i * LINE_HEIGHT), TEXT_COLOR, false);
                    // Renderizar valor en verde
                    int nameWidth = font.width(parts[0] + ": ");
                    guiGraphics.drawString(font, parts[1], textX + nameWidth, textY + (i * LINE_HEIGHT), VALUE_COLOR, false);
                    continue;
                }
                color = TEXT_COLOR;
            }
            guiGraphics.drawString(font, lines[i], textX, textY + (i * LINE_HEIGHT), color, false);
        }
    }
}