package net.iaxsro.rpgstats.util;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

import java.util.function.Supplier;

public final class ChatUtil {

    // --- Colores y Formatos Comunes ---
    public static final ChatFormatting HEADER_COLOR = ChatFormatting.GOLD;
    public static final ChatFormatting LABEL_COLOR = ChatFormatting.GRAY;
    public static final ChatFormatting VALUE_COLOR = ChatFormatting.WHITE;
    public static final ChatFormatting INFO_COLOR = ChatFormatting.YELLOW;
    public static final ChatFormatting SUCCESS_COLOR = ChatFormatting.GREEN;
    public static final ChatFormatting ERROR_COLOR = ChatFormatting.RED;
    public static final ChatFormatting EMPHASIS_COLOR = ChatFormatting.AQUA;
    // Constructor privado para prevenir instanciación
    private ChatUtil() {
    }


    // --- Envío de Mensajes ---

    /**
     * Envía un mensaje de éxito a un CommandSourceStack (normalmente un comando).
     * Usa una función Supplier para evaluación perezosa del Component.
     *
     * @param source          La fuente del comando.
     * @param messageSupplier Supplier que devuelve el Component a enviar.
     * @param notifyOps       Si se debe notificar a otros OPs.
     */
    public static void sendSuccess(CommandSourceStack source, Supplier<Component> messageSupplier, boolean notifyOps) {
        source.sendSuccess(messageSupplier, notifyOps);
    }

    /**
     * Envía un mensaje de éxito (no perezoso) a un CommandSourceStack.
     *
     * @param source    La fuente del comando.
     * @param message   El Component a enviar.
     * @param notifyOps Si se debe notificar a otros OPs.
     */
    public static void sendSuccess(CommandSourceStack source, Component message, boolean notifyOps) {
        // Llama a la versión Supplier para consistencia (aunque no sea estrictamente necesario aquí)
        source.sendSuccess(() -> message, notifyOps);
    }

    /**
     * Envía un mensaje de fallo a un CommandSourceStack.
     *
     * @param source  La fuente del comando.
     * @param message El Component a enviar.
     */
    public static void sendFailure(CommandSourceStack source, Component message) {
        source.sendFailure(message);
    }

    /**
     * Envía un mensaje directamente a un jugador.
     *
     * @param player    El jugador destinatario.
     * @param message   El Component a enviar.
     * @param actionBar Si el mensaje debe aparecer en la action bar (encima del inventario).
     */
    public static void sendToPlayer(Player player, Component message, boolean actionBar) {
        player.displayClientMessage(message, actionBar);
    }

    // --- Helpers de Formato ---

    /**
     * Crea un componente de texto formateado como un encabezado.
     * Ejemplo: "--- Título ---" en color dorado.
     *
     * @param title El texto del título.
     * @return Un MutableComponent formateado.
     */
    public static MutableComponent formatHeader(String title) {
        return Component.literal("--- " + title + " ---").withStyle(HEADER_COLOR);
    }

    /**
     * Crea un componente de texto formateado como "Etiqueta: ".
     *
     * @param label El texto de la etiqueta.
     * @return Un MutableComponent formateado.
     */
    public static MutableComponent formatLabel(String label) {
        return Component.literal(label + ": ").withStyle(LABEL_COLOR);
    }

    /**
     * Crea un componente de texto formateado para un valor, aplicando colores.
     *
     * @param value  El valor a mostrar (se convertirá a String).
     * @param format Los estilos/colores a aplicar.
     * @return Un MutableComponent formateado.
     */
    public static MutableComponent formatValue(Object value, ChatFormatting... format) {
        return Component.literal(String.valueOf(value)).withStyle(format);
    }

    /**
     * Crea un componente de texto formateado como "Etiqueta: Valor" con colores separados.
     *
     * @param label       El texto de la etiqueta.
     * @param value       El valor a mostrar.
     * @param valueFormat El formato para el valor.
     * @return Un MutableComponent formateado.
     */
    public static MutableComponent formatKeyValue(String label, Object value, ChatFormatting valueFormat) {
        return formatLabel(label).append(formatValue(value, valueFormat));
    }

    /**
     * Crea un componente de texto formateado como "Etiqueta: Valor" con colores por defecto.
     *
     * @param label El texto de la etiqueta.
     * @param value El valor a mostrar.
     * @return Un MutableComponent formateado.
     */
    public static MutableComponent formatKeyValue(String label, Object value) {
        return formatKeyValue(label, value, VALUE_COLOR);
    }

    /**
     * Formatea un número double a un número específico de decimales.
     *
     * @param value    El valor double.
     * @param decimals El número de decimales deseados.
     * @return El string formateado.
     */
    public static String formatDouble(double value, int decimals) {
        return String.format("%." + decimals + "f", value);
    }

}