package net.iaxsro.rpgstats.capabilities.util;

import net.iaxsro.rpgstats.capabilities.IPlayerStats;
import net.iaxsro.rpgstats.capabilities.PlayerStats;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Proporciona una fachada para acceder de forma sencilla a las capacidades de IPlayerStats.
 * Permite obtener y establecer valores de estadísticas de una entidad de forma fluida.
 * Ejemplo: CapabilitiesAccesor.playerLevel.get(entity);
 * CapabilitiesAccesor.playerLevel.set(entity, 10);
 */
public class CapabilitiesAccessor {

    // --- Puntos de Atributo ---
    public static final StatAccessor<Double> strengthPoints = new StatAccessor<>(
            entity -> entity.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY).map(IPlayerStats::getStrengthPoints).orElse(0.0),
            (entity, value) -> entity.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY).ifPresent(stats -> stats.setStrengthPoints(value))
    );
    public static final StatAccessor<Double> dexterityPoints = new StatAccessor<>(
            entity -> entity.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY).map(IPlayerStats::getDexterityPoints).orElse(0.0),
            (entity, value) -> entity.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY).ifPresent(stats -> stats.setDexterityPoints(value))
    );
    public static final StatAccessor<Double> vitalityPoints = new StatAccessor<>(
            entity -> entity.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY).map(IPlayerStats::getVitalityPoints).orElse(0.0),
            (entity, value) -> entity.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY).ifPresent(stats -> stats.setVitalityPoints(value))
    );
    public static final StatAccessor<Double> constitutionPoints = new StatAccessor<>(
            entity -> entity.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY).map(IPlayerStats::getConstitutionPoints).orElse(0.0),
            (entity, value) -> entity.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY).ifPresent(stats -> stats.setConstitutionPoints(value))
    );
    public static final StatAccessor<Double> intelligencePoints = new StatAccessor<>(
            entity -> entity.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY).map(IPlayerStats::getIntelligencePoints).orElse(0.0),
            (entity, value) -> entity.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY).ifPresent(stats -> stats.setIntelligencePoints(value))
    );
    // --- Iteraciones de Atributo ---
    public static final StatAccessor<Double> strengthIterations = new StatAccessor<>(
            entity -> entity.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY).map(IPlayerStats::getStrengthIterations).orElse(0.0),
            (entity, value) -> entity.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY).ifPresent(stats -> stats.setStrengthIterations(value))
    );
    public static final StatAccessor<Double> dexterityIterations = new StatAccessor<>(
            entity -> entity.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY).map(IPlayerStats::getDexterityIterations).orElse(0.0),
            (entity, value) -> entity.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY).ifPresent(stats -> stats.setDexterityIterations(value))
    );
    public static final StatAccessor<Double> vitalityIterations = new StatAccessor<>(
            entity -> entity.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY).map(IPlayerStats::getVitalityIterations).orElse(0.0),
            (entity, value) -> entity.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY).ifPresent(stats -> stats.setVitalityIterations(value))
    );
    public static final StatAccessor<Double> constitutionIterations = new StatAccessor<>(
            entity -> entity.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY).map(IPlayerStats::getConstitutionIterations).orElse(0.0),
            (entity, value) -> entity.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY).ifPresent(stats -> stats.setConstitutionIterations(value))
    );
    public static final StatAccessor<Double> intelligenceIterations = new StatAccessor<>(
            entity -> entity.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY).map(IPlayerStats::getIntelligenceIterations).orElse(0.0),
            (entity, value) -> entity.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY).ifPresent(stats -> stats.setIntelligenceIterations(value))
    );
    // --- Información del Jugador ---
    public static final StatAccessor<Integer> playerLevel = new StatAccessor<>(
            entity -> entity.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY).map(IPlayerStats::getLevel).orElse(0),
            (entity, value) -> entity.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY).ifPresent(stats -> stats.setLevel(value))
    );
    public static final StatAccessor<String> nickname = new StatAccessor<>(
            entity -> entity.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY).map(IPlayerStats::getNickname).orElse(""),
            (entity, value) -> entity.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY).ifPresent(stats -> stats.setNickname(value))
    );
    public static final StatAccessor<Boolean> firstTimeJoining = new StatAccessor<>(
            entity -> entity.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY).map(IPlayerStats::isFirstTimeJoining).orElse(true),
            (entity, value) -> entity.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY).ifPresent(stats -> stats.setFirstTimeJoining(value))
    );

    /**
     * Clase interna que encapsula la lógica de getter/setter para una propiedad de capacidad.
     *
     * @param <T> el tipo de dato de la propiedad.
     */
    public static class StatAccessor<T> {
        private final Function<LivingEntity, T> getter;
        private final BiConsumer<LivingEntity, T> setter;

        public StatAccessor(Function<LivingEntity, T> getter, BiConsumer<LivingEntity, T> setter) {
            this.getter = getter;
            this.setter = setter;
        }

        public T get(LivingEntity entity) {
            return getter.apply(entity);
        }

        public void set(LivingEntity entity, T value) {
            setter.accept(entity, value);
        }
    }
}
