package net.iaxsro.rpgstats.system;

import net.iaxsro.rpgstats.capabilities.IPlayerStats;
import net.iaxsro.rpgstats.capabilities.PlayerStats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Clase de utilidad (fachada) para acceder de forma conveniente a la capacidad IPlayerStats de una entidad.
 * Proporciona métodos estáticos para obtener el LazyOptional o ejecutar acciones si la capacidad está presente.
 */
public final class PlayerDataHandler {

    // Constructor privado para prevenir instanciación
    private PlayerDataHandler() {
    }

    /**
     * Obtiene el LazyOptional de la capacidad IPlayerStats para una entidad dada.
     * Devuelve un Optional vacío si la entidad no es un jugador o no tiene la capacidad.
     *
     * @param entity La entidad (idealmente un Player).
     * @return Un LazyOptional que contiene IPlayerStats si está presente.
     */
    @NotNull
    public static LazyOptional<IPlayerStats> getStatsLazy(Entity entity) {
        if (entity instanceof Player) {
            return entity.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY);
        }
        return LazyOptional.empty();
    }

    /**
     * Obtiene la instancia de IPlayerStats directamente, o null si no está presente.
     * ¡PRECAUCIÓN! El llamador es responsable de manejar el caso nulo.
     * Generalmente es más seguro usar getStatsLazy(entity).ifPresent(...)
     *
     * @param entity La entidad (idealmente un Player).
     * @return La instancia de IPlayerStats o null.
     */
    @Nullable
    public static IPlayerStats getStatsUnsafe(Entity entity) {
        return getStatsLazy(entity).orElse(null);
    }

    /**
     * Ejecuta una acción si la capacidad IPlayerStats está presente en la entidad.
     * Forma segura y conveniente de operar sobre la capacidad.
     *
     * @param entity La entidad (idealmente un Player).
     * @param action El Consumer que se ejecutará con la instancia de IPlayerStats.
     * @return true si la acción se ejecutó (la capacidad estaba presente), false en caso contrario.
     */
    public static boolean executeOnStats(Entity entity, Consumer<IPlayerStats> action) {
        LazyOptional<IPlayerStats> statsOptional = getStatsLazy(entity);
        statsOptional.ifPresent((NonNullConsumer<? super IPlayerStats>) action);
        return statsOptional.isPresent(); // Devuelve si la acción se llegó a ejecutar
    }

    // --- Ejemplo de uso en otra clase ---
    /*
    public void algunaFuncion(Player player) {
        // Forma segura usando executeOnStats
        PlayerDataHandler.executeOnStats(player, stats -> {
            stats.addStrengthPoints(5.0);
            System.out.println("Puntos añadidos. Nivel actual: " + stats.getLevel());
            // No necesitas manejar null aquí dentro
        });

        // Forma alternativa obteniendo el Optional
        PlayerDataHandler.getStatsLazy(player).ifPresent(stats -> {
            // ... operar sobre stats ...
        });

        // Forma insegura (¡requiere chequeo de null!)
        IPlayerStats stats = PlayerDataHandler.getStatsUnsafe(player);
        if (stats != null) {
            // ... operar sobre stats ...
        }
    }
    */

}