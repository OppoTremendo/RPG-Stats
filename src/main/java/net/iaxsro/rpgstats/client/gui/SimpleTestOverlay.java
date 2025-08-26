// Corrected imports adding missing Component import

package net.iaxsro.rpgstats.client.gui;

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
 * Overlay GUI simple para testing que muestra información básica del jugador.
 * Se posiciona en la esquina inferior izquierda.
 */
public class SimpleTestOverlay implements IGuiOverlay {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleTestOverlay.class);

    // Configuración de posición y estilo
    private static final int MARGIN_LEFT = 10;
    private static final int MARGIN_BOTTOM = 10;
    private static final int LINE_HEIGHT = 12;
    private static final int BACKGROUND_PADDING = 4;
    private static final int BACKGROUND_COLOR = 0x80000000; // Negro semi-transparente
    private static final int TEXT_COLOR = 0xFFFFFF; // Blanco
    private static final int HEADER_COLOR = 0xFF00FF00; // Verde brillante para testing

    // Variables de control
    private static boolean isVisible = false; // Oculto por defecto

    /**
     * Alterna la visibilidad del overlay
     */
    public static void toggleVisibility() {
        isVisible = !isVisible;
        LOGGER.debug("Test overlay {}", isVisible ? "activado" : "desactivado");
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
            try {
                renderTestInfo(guiGraphics, minecraft.font, screenWidth, screenHeight, player);
            } catch (Exception e) {
                LOGGER.error("Error al renderizar overlay de test: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Renderiza información de test en la pantalla
     */
    private void renderTestInfo(GuiGraphics guiGraphics, Font font, int screenWidth, int screenHeight, LocalPlayer player) {
        // Información simple para mostrar
        String[] lines = {
                Component.translatable("overlay.test.header").getString(),
                String.format("%s: %s", Component.translatable("overlay.test.player").getString(), player.getName().getString()),
                String.format("%s: %.1f", Component.translatable("overlay.test.health").getString(), player.getHealth()),
                String.format("%s: %d", Component.translatable("overlay.test.level").getString(), player.experienceLevel),
                Component.translatable("overlay.test.working").getString()
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

        // Calcular posición (esquina inferior izquierda)
        int x = MARGIN_LEFT;
        int y = screenHeight - backgroundHeight - MARGIN_BOTTOM;

        // Renderizar fondo semi-transparente con borde
        guiGraphics.fill(x, y, x + backgroundWidth, y + backgroundHeight, BACKGROUND_COLOR);

        // Borde brillante para testing
        guiGraphics.fill(x, y, x + backgroundWidth, y + 1, 0xFF00FF00); // Top
        guiGraphics.fill(x, y, x + 1, y + backgroundHeight, 0xFF00FF00); // Left
        guiGraphics.fill(x + backgroundWidth - 1, y, x + backgroundWidth, y + backgroundHeight, 0xFF00FF00); // Right
        guiGraphics.fill(x, y + backgroundHeight - 1, x + backgroundWidth, y + backgroundHeight, 0xFF00FF00); // Bottom

        // Renderizar texto
        int textX = x + BACKGROUND_PADDING;
        int textY = y + BACKGROUND_PADDING;

        for (int i = 0; i < lines.length; i++) {
            int color = (i == 0) ? HEADER_COLOR : TEXT_COLOR;
            guiGraphics.drawString(font, lines[i], textX, textY + (i * LINE_HEIGHT), color, false);
        }
    }
}