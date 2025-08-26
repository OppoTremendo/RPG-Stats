package net.iaxsro.rpgstats.client.gui;

import net.iaxsro.rpgstats.util.AttributeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Overlay GUI que muestra los atributos de Minecraft que han sido modificados por las bonificaciones del mod RPGStats.
 * Se posiciona en la parte superior derecha y muestra valores como vida máxima, daño de ataque, armadura, etc.
 */
public class ModifiedAttributesOverlay implements IGuiOverlay {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModifiedAttributesOverlay.class);

    // Configuración de posición y estilo
    private static final int MARGIN_RIGHT = 10;
    private static final int MARGIN_TOP = 10;
    private static final int LINE_HEIGHT = 12;
    private static final int BACKGROUND_PADDING = 4;
    private static final int BACKGROUND_COLOR = 0x80000000; // Negro semi-transparente
    private static final int TEXT_COLOR = 0xFFFFFF; // Blanco
    private static final int HEADER_COLOR = 0xFF32CD32; // Verde lima
    private static final int VALUE_COLOR = 0xFFFF6347; // Tomate/naranja para los valores
    private static final long UPDATE_INTERVAL = 2000; // Actualizar cada 2 segundos (reducido spam)
    // Atributos opcionales (EpicFight, Forge) - Obtenidos de forma segura
    private static final Supplier<Attribute> EF_IMPACT = AttributeUtil.registrySupplier(new ResourceLocation("epicfight:impact"));
    private static final Supplier<Attribute> EF_ARMOR_NEGATION = AttributeUtil.registrySupplier(new ResourceLocation("epicfight:armor_negation"));
    private static final Supplier<Attribute> EF_STUN_ARMOR = AttributeUtil.registrySupplier(new ResourceLocation("epicfight:stun_armor"));
    private static final Supplier<Attribute> EF_MAX_STAMINA = AttributeUtil.registrySupplier(new ResourceLocation("epicfight:staminar")); // nombre original 'staminar'
    private static final Supplier<Attribute> FORGE_SWIM_SPEED = AttributeUtil.registrySupplier(new ResourceLocation("forge:swim_speed"));
    // Variables para optimización y cache
    private static boolean isVisible = true;
    private static double lastMaxHealth = -1;
    private static double lastAttackDamage = -1;
    private static double lastArmor = -1;
    private static double lastMovementSpeed = -1;
    private static double lastAttackSpeed = -1;
    private static double lastKnockbackResistance = -1;
    private static double lastArmorToughness = -1;
    private static double lastSwimSpeed = -1;
    private static double lastImpact = -1;
    private static double lastArmorNegation = -1;
    private static double lastStunArmor = -1;
    private static double lastMaxStamina = -1;
    private static String[] cachedLines = null;
    private static long lastUpdateTime = 0;

    /**
     * Método para obtener el estado de visibilidad.
     */
    public static boolean isVisible() {
        return isVisible;
    }

    /**
     * Método para mostrar u ocultar el overlay.
     */
    public static void setVisible(boolean visible) {
        isVisible = visible;
        LOGGER.debug("ModifiedAttributesOverlay: Visibilidad cambiada a {}", visible);
    }

    /**
     * Método para forzar la actualización del cache.
     */
    public static void forceUpdate() {
        cachedLines = null;
        lastUpdateTime = 0;
        LOGGER.debug("ModifiedAttributesOverlay: Forzada actualización del cache");
    }

    /**
     * Método para limpiar el cache.
     */
    public static void clearCache() {
        cachedLines = null;
        lastMaxHealth = -1;
        lastAttackDamage = -1;
        lastArmor = -1;
        lastMovementSpeed = -1;
        lastAttackSpeed = -1;
        lastKnockbackResistance = -1;
        lastArmorToughness = -1;
        lastSwimSpeed = -1;
        lastImpact = -1;
        lastArmorNegation = -1;
        lastStunArmor = -1;
        lastMaxStamina = -1;
        lastUpdateTime = 0;
        LOGGER.debug("ModifiedAttributesOverlay: Cache limpiado");
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
                    renderModifiedAttributes(guiGraphics, minecraft.font, screenWidth, screenHeight);
                }
            } catch (Exception e) {
                LOGGER.error("Error al renderizar overlay de atributos modificados: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Actualiza el cache de atributos si han cambiado los valores.
     */
    private void updateAttributeCache(LocalPlayer player) {
        try {
            // Obtener valores actuales de los atributos vanilla
            double currentMaxHealth = player.getAttributeValue(Attributes.MAX_HEALTH);
            double currentAttackDamage = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            double currentArmor = player.getAttributeValue(Attributes.ARMOR);
            double currentMovementSpeed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
            double currentAttackSpeed = player.getAttributeValue(Attributes.ATTACK_SPEED);
            double currentKnockbackResistance = player.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
            double currentArmorToughness = player.getAttributeValue(Attributes.ARMOR_TOUGHNESS);

            // Obtener valores de atributos adicionales
            double currentSwimSpeed = 0.0;
            double currentImpact = 0.0;
            double currentArmorNegation = 0.0;
            double currentStunArmor = 0.0;
            double currentMaxStamina = 0.0;

            // Obtener valores de Forge
            Attribute forgeSwimSpeedAttr = FORGE_SWIM_SPEED.get();
            if (forgeSwimSpeedAttr != null) {
                currentSwimSpeed = player.getAttributeValue(forgeSwimSpeedAttr);
            }

            // Obtener valores de Epic Fight
            Attribute efImpactAttr = EF_IMPACT.get();
            if (efImpactAttr != null) {
                currentImpact = player.getAttributeValue(efImpactAttr);
            }

            Attribute efArmorNegationAttr = EF_ARMOR_NEGATION.get();
            if (efArmorNegationAttr != null) {
                currentArmorNegation = player.getAttributeValue(efArmorNegationAttr);
            }

            Attribute efStunArmorAttr = EF_STUN_ARMOR.get();
            if (efStunArmorAttr != null) {
                currentStunArmor = player.getAttributeValue(efStunArmorAttr);
            }

            Attribute efMaxStaminaAttr = EF_MAX_STAMINA.get();
            if (efMaxStaminaAttr != null) {
                currentMaxStamina = player.getAttributeValue(efMaxStaminaAttr);
            }

            // Verificar si algún valor ha cambiado significativamente
            boolean hasChanged = Math.abs(currentMaxHealth - lastMaxHealth) > 0.01 ||
                    Math.abs(currentAttackDamage - lastAttackDamage) > 0.01 ||
                    Math.abs(currentArmor - lastArmor) > 0.01 ||
                    Math.abs(currentMovementSpeed - lastMovementSpeed) > 0.0001 || // Velocidad de movimiento es muy pequeña
                    Math.abs(currentAttackSpeed - lastAttackSpeed) > 0.01 ||
                    Math.abs(currentKnockbackResistance - lastKnockbackResistance) > 0.01 ||
                    Math.abs(currentArmorToughness - lastArmorToughness) > 0.01 ||
                    Math.abs(currentSwimSpeed - lastSwimSpeed) > 0.01 ||
                    Math.abs(currentImpact - lastImpact) > 0.01 ||
                    Math.abs(currentArmorNegation - lastArmorNegation) > 0.01 ||
                    Math.abs(currentStunArmor - lastStunArmor) > 0.01 ||
                    Math.abs(currentMaxStamina - lastMaxStamina) > 0.01;

            if (hasChanged || cachedLines == null) {
                // Actualizar valores almacenados
                lastMaxHealth = currentMaxHealth;
                lastAttackDamage = currentAttackDamage;
                lastArmor = currentArmor;
                lastMovementSpeed = currentMovementSpeed;
                lastAttackSpeed = currentAttackSpeed;
                lastKnockbackResistance = currentKnockbackResistance;
                lastArmorToughness = currentArmorToughness;
                lastSwimSpeed = currentSwimSpeed;
                lastImpact = currentImpact;
                lastArmorNegation = currentArmorNegation;
                lastStunArmor = currentStunArmor;
                lastMaxStamina = currentMaxStamina;

                // Crear las líneas de texto con los valores actuales
                List<String> linesList = new ArrayList<>();

                linesList.add(Component.translatable("overlay.modified_attributes.header").getString());
                linesList.add(String.format("%s: %.1f", Component.translatable("attribute.minecraft.max_health").getString(), currentMaxHealth));
                linesList.add(String.format("%s: %.1f", Component.translatable("attribute.minecraft.attack_damage").getString(), currentAttackDamage));
                linesList.add(String.format("%s: %.1f", Component.translatable("attribute.minecraft.armor").getString(), currentArmor));
                linesList.add(String.format("%s: %.3f", Component.translatable("attribute.minecraft.movement_speed").getString(), currentMovementSpeed));
                linesList.add(String.format("%s: %.2f", Component.translatable("attribute.minecraft.attack_speed").getString(), currentAttackSpeed));
                linesList.add(String.format("%s: %.2f", Component.translatable("attribute.minecraft.knockback_resistance").getString(), currentKnockbackResistance));
                linesList.add(String.format("%s: %.1f", Component.translatable("attribute.minecraft.armor_toughness").getString(), currentArmorToughness));

                // Atributos adicionales solo si tienen valor > 0
                if (currentSwimSpeed > 0.0) {
                    linesList.add(String.format("%s: %.2f", Component.translatable("attribute.forge.swim_speed").getString(), currentSwimSpeed));
                }

                if (currentImpact > 0.0) {
                    linesList.add(String.format("%s: %.1f", Component.translatable("attribute.epicfight.impact").getString(), currentImpact));
                }

                if (currentArmorNegation > 0.0) {
                    linesList.add(String.format("%s: %.1f", Component.translatable("attribute.epicfight.armor_negation").getString(), currentArmorNegation));
                }

                if (currentStunArmor > 0.0) {
                    linesList.add(String.format("%s: %.1f", Component.translatable("attribute.epicfight.stun_armor").getString(), currentStunArmor));
                }

                if (currentMaxStamina > 0.0) {
                    linesList.add(String.format("%s: %.1f", Component.translatable("attribute.epicfight.max_stamina").getString(), currentMaxStamina));
                }

                // Convertir la lista a array
                cachedLines = linesList.toArray(new String[0]);
            }
        } catch (Exception e) {
            LOGGER.error("Error al actualizar cache de atributos modificados: {}", e.getMessage(), e);
            // En caso de error, crear un cache básico
            cachedLines = new String[]{
                    Component.translatable("overlay.modified_attributes.header").getString(),
                    Component.translatable("overlay.modified_attributes.error").getString()
            };
        }
    }

    /**
     * Renderiza el overlay con los atributos modificados.
     */
    private void renderModifiedAttributes(GuiGraphics guiGraphics, Font font, int screenWidth, int screenHeight) {
        if (cachedLines == null || cachedLines.length == 0) {
            return;
        }

        // Calcular el ancho máximo del texto
        int maxWidth = 0;
        for (String line : cachedLines) {
            int lineWidth = font.width(line);
            if (lineWidth > maxWidth) {
                maxWidth = lineWidth;
            }
        }

        // Calcular dimensiones del fondo
        int backgroundWidth = maxWidth + (BACKGROUND_PADDING * 2);
        int backgroundHeight = (cachedLines.length * LINE_HEIGHT) + (BACKGROUND_PADDING * 2);

        // Calcular posición (esquina superior derecha)
        int startX = screenWidth - backgroundWidth - MARGIN_RIGHT;
        int startY = MARGIN_TOP;

        // Dibujar fondo
        guiGraphics.fill(startX, startY, startX + backgroundWidth, startY + backgroundHeight, BACKGROUND_COLOR);

        // Dibujar borde
        guiGraphics.fill(startX - 1, startY - 1, startX + backgroundWidth + 1, startY, 0xFFFFFFFF); // Borde superior
        guiGraphics.fill(startX - 1, startY + backgroundHeight, startX + backgroundWidth + 1, startY + backgroundHeight + 1, 0xFFFFFFFF); // Borde inferior
        guiGraphics.fill(startX - 1, startY, startX, startY + backgroundHeight, 0xFFFFFFFF); // Borde izquierdo
        guiGraphics.fill(startX + backgroundWidth, startY, startX + backgroundWidth + 1, startY + backgroundHeight, 0xFFFFFFFF); // Borde derecho

        // Renderizar las líneas de texto
        int textX = startX + BACKGROUND_PADDING;
        int textY = startY + BACKGROUND_PADDING;

        for (int i = 0; i < cachedLines.length; i++) {
            String line = cachedLines[i];

            if (i == 0) {
                // Título en verde lima
                guiGraphics.drawString(font, line, textX, textY, HEADER_COLOR, false);
            } else {
                // Separar nombre del atributo del valor
                String[] parts = line.split(": ");
                if (parts.length == 2) {
                    // Renderizar nombre del atributo en blanco
                    guiGraphics.drawString(font, parts[0] + ": ", textX, textY, TEXT_COLOR, false);

                    // Calcular posición para el valor
                    int nameWidth = font.width(parts[0] + ": ");

                    // Renderizar valor en naranja
                    guiGraphics.drawString(font, parts[1], textX + nameWidth, textY, VALUE_COLOR, false);
                } else {
                    // Línea completa en color por defecto
                    guiGraphics.drawString(font, line, textX, textY, TEXT_COLOR, false);
                }
            }

            textY += LINE_HEIGHT;
        }
    }
}