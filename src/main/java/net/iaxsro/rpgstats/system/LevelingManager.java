package net.iaxsro.rpgstats.system;

import net.iaxsro.rpgstats.RpgStatsMod;
import net.iaxsro.rpgstats.capabilities.IPlayerStats;
import net.iaxsro.rpgstats.capabilities.PlayerStats;
import net.iaxsro.rpgstats.network.ClientboundSyncPlayerStatsPacket;
import net.iaxsro.rpgstats.network.PacketHandler;
import net.iaxsro.rpgstats.registry.AttributeRegistry;
import net.iaxsro.rpgstats.system.PersistenceService;
import net.iaxsro.rpgstats.util.AttributeUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.Map;
import java.util.UUID;

public class LevelingManager {

    private static final Logger LOGGER = RpgStatsMod.LOGGER;
    private static final UUID LEVEL_BONUS_UUID = UUID.fromString("c0ffee00-0000-0000-0000-000000000001");

    // UUID estático utilizado para los modificadores de nivel
    public static final UUID LEVEL_BONUS_MODIFIER_UUID = UUID.fromString("ec8b45a7-1cdd-45f3-9ad4-6e7f4a770e4f");

    /**
     * Procesa la subida de nivel para un jugador.
     */
    public static void processLevelUp(ServerPlayer player) {
        LOGGER.info("Procesando subida de nivel para {}", player.getName().getString());

        LazyOptional<IPlayerStats> statsOptional = player.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY);

        if (statsOptional.isPresent()) {
            statsOptional.ifPresent(stats -> {
                // --- Toda la lógica de level up va aquí dentro ---
                int currentLevel = stats.getLevel(); // Ahora es int
                int newLevelNumber = currentLevel + 1;

                LOGGER.debug("Nivel actual: {}", currentLevel);
                LOGGER.debug("Nuevo nivel: {}", newLevelNumber);

                // 2. Actualizar Atributos Base
                double strPoints = stats.getStrengthPoints();
                double dexPoints = stats.getDexterityPoints();
                double vitPoints = stats.getVitalityPoints();
                double conPoints = stats.getConstitutionPoints();
                double intPoints = stats.getIntelligencePoints();

                if (strPoints > 0) updateBaseAttribute(player, AttributeRegistry.STRENGTH.get(), strPoints);
                if (dexPoints > 0) updateBaseAttribute(player, AttributeRegistry.DEXTERITY.get(), dexPoints);
                if (vitPoints > 0) updateBaseAttribute(player, AttributeRegistry.VITALITY.get(), vitPoints);
                if (conPoints > 0) updateBaseAttribute(player, AttributeRegistry.CONSTITUTION.get(), conPoints);
                if (intPoints > 0) updateBaseAttribute(player, AttributeRegistry.INTELLIGENCE.get(), intPoints);

                // 3. Resetear Puntos Temporales en la Capacidad
                stats.setStrengthPoints(0);
                stats.setDexterityPoints(0);
                stats.setVitalityPoints(0);
                stats.setConstitutionPoints(0);
                stats.setIntelligencePoints(0);
                stats.setStrengthIterations(0);
                stats.setDexterityIterations(0);
                stats.setVitalityIterations(0);
                stats.setConstitutionIterations(0);
                stats.setIntelligenceIterations(0);

                // 4. Actualizar Nivel en la Capacidad
                stats.setLevel(newLevelNumber);

                // 5. Recalcular Bonificaciones
                AttributeCalculator.CalculatedBonuses newBonuses = AttributeCalculator.calculateBonuses(player);

                // 6. Aplicar Modificadores con UUID fijo
                AttributeCalculator.applyAttributeModifiers(player, newBonuses, newLevelNumber, LEVEL_BONUS_UUID, LEVEL_BONUS_UUID);

                // 7. Sincronizar Capacidad Actualizada al Cliente
                PacketHandler.sendToPlayer(player, new ClientboundSyncPlayerStatsPacket(stats.writeNBT()));
                LOGGER.debug("Capacidad PlayerStats sincronizada al cliente.");

                // 8. Restaurar Salud
                player.setHealth(player.getMaxHealth());
                LOGGER.debug("Salud restaurada a {}", player.getHealth());

                LOGGER.info("Subida de nivel completada para {}", player.getName().getString());
                // --- Fin de la lógica de level up ---
            }); // Fin de statsOptional.ifPresent
        } else {
            // Caso raro donde el jugador no tiene la capacidad
            LOGGER.error("¡No se pudo obtener la capacidad PlayerStats para {} durante la subida de nivel!", player.getName().getString());
        } // Fin de if (statsOptional.isPresent())
    }

    /**
     * Aplica efectos necesarios después de que un jugador reaparece (respawn).
     */
    public static void applyPostRespawnEffects(ServerPlayer player) {
        LOGGER.debug("Aplicando efectos post-respawn para {}", player.getName().getString());

        LazyOptional<IPlayerStats> statsOptional = player.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY);

        if (statsOptional.isPresent()) {
            statsOptional.ifPresent(stats -> {
                // --- Lógica de post-respawn aquí dentro ---
                int currentLevel = stats.getLevel();

                if (currentLevel > 0) {
                    AttributeCalculator.CalculatedBonuses currentBonuses = AttributeCalculator.calculateBonuses(player);
                    AttributeCalculator.applyAttributeModifiers(player, currentBonuses, currentLevel,
                            LEVEL_BONUS_MODIFIER_UUID, LEVEL_BONUS_MODIFIER_UUID);
                    LOGGER.debug("Modificadores reaplicados post-respawn para nivel {}.", currentLevel);

                }

                // Restaura la salud al máximo (después de reaplicar modificadores)
                player.setHealth(player.getMaxHealth());
                LOGGER.debug("Salud restaurada post-respawn para {}", player.getName().getString());
                // --- Fin de la lógica post-respawn ---
            }); // Fin de statsOptional.ifPresent
        } else {
            LOGGER.error("No se pudo obtener la capacidad PlayerStats para {} durante post-respawn!", player.getName().getString());
        } // Fin de if (statsOptional.isPresent())
    }

    /**
     * Helper para actualizar el valor base de un atributo.
     */
    private static void updateBaseAttribute(LivingEntity entity, Attribute attribute, double pointsToAdd) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null && pointsToAdd > 1e-6) {
            double currentBase = instance.getBaseValue();
            double newBase = currentBase + pointsToAdd;
            instance.setBaseValue(newBase);
            LOGGER.trace("Atributo base '{}' actualizado para {}: {} -> {}",
                    AttributeUtil.getAttributeName(attribute), // Usar una forma segura de obtener el nombre/key
                    entity.getName().getString(), currentBase, newBase);
        } else if (instance == null) {
            LOGGER.warn("Intento de actualizar atributo base nulo '{}' para {}", AttributeUtil.getAttributeName(attribute), entity.getName().getString());
        }
    }


    /**
     * Intenta revertir al jugador a un nivel específico.
     * ADVERTENCIA: Operación compleja que depende de datos persistidos precisos.
     *
     * @param source      El CommandSourceStack para enviar feedback.
     * @param player      El jugador a revertir.
     * @param targetLevel El nivel al que se quiere revertir.
     * @return true si la operación fue (conceptualmente) exitosa, false en caso contrario.
     */
    public static boolean revertToLevel(CommandSourceStack source, ServerPlayer player, int targetLevel) {
        LOGGER.warn("Intentando revertir a {} al nivel {}.", player.getName().getString(), targetLevel);

        LazyOptional<IPlayerStats> statsOptional = player.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY);

        // 1. Validaciones iniciales (incluyendo chequeo de capacidad)
        if (!statsOptional.isPresent()) {
            source.sendFailure(Component.literal("No se pudo obtener PlayerStats para " + player.getName().getString()));
            return false;
        }
        // ifPresent para obtener stats de forma segura
        final boolean[] success = {false}; // Usar array para modificar desde lambda
        statsOptional.ifPresent(stats -> {
            if (targetLevel < 0) {
                source.sendFailure(Component.literal("El nivel objetivo no puede ser negativo."));
                return; // Sale de la lambda
            }
            if (targetLevel >= stats.getLevel()) {
                source.sendFailure(Component.literal("El nivel objetivo debe ser menor que el nivel actual (" + stats.getLevel() + ")."));
                return; // Sale de la lambda
            }

            // 2. Cargar datos del nivel objetivo
            PersistenceService.LevelSaveData targetLevelData = PersistenceService.loadLevelSaveData(player, targetLevel);
            if (targetLevelData == null) {
                source.sendFailure(Component.literal("No se encontraron datos guardados para el nivel " + targetLevel + ". No se puede revertir."));
                return; // Sale de la lambda
            }

            // 3. Obtener UUID del nivel ANTERIOR al objetivo
            UUID previousLevelUUID = (targetLevel > 0) ? PersistenceService.getUUIDForLevel(player, targetLevel - 1) : null;

            // 4. Restaurar Atributos Base
            LOGGER.debug("Restaurando atributos base al estado del nivel {}", targetLevel);
            boolean attributesRestored = true;
            for (Map.Entry<String, Double> entry : targetLevelData.baseAttributes().entrySet()) {
                ResourceLocation attributeKey = new ResourceLocation(RpgStatsMod.MOD_ID, entry.getKey()); // Asume que la key guardada es el path
                Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(attributeKey);
                if (attribute != null) {
                    AttributeInstance instance = player.getAttribute(attribute);
                    if (instance != null) {
                        instance.setBaseValue(entry.getValue());
                        LOGGER.trace("  Base {}: {}", entry.getKey(), entry.getValue());
                    } else {
                        LOGGER.error("¡Atributo {} ({}) no encontrado en el jugador {} al intentar revertir!", entry.getKey(), attributeKey, player.getName().getString());
                        attributesRestored = false;
                    }
                } else {
                    LOGGER.error("¡Atributo {} ({}) no encontrado en el registro al intentar revertir!", entry.getKey(), attributeKey);
                    attributesRestored = false;
                }
            }
            if (!attributesRestored) {
                source.sendFailure(Component.literal("Error al restaurar uno o más atributos base. Abortando reversión. Ver logs."));
                return; // Sale de la lambda
            }

            // 5. Recalcular Bonificaciones para el nivel objetivo
            AttributeCalculator.CalculatedBonuses targetBonuses = AttributeCalculator.calculateBonuses(player);

            // 6. Aplicar Modificadores (quita los actuales, aplica los del target)
            AttributeCalculator.applyAttributeModifiers(player, targetBonuses, targetLevel, targetLevelData.levelUUID(), stats.getCurrentLevelUUID()); // Usa el UUID *actual* como 'previous'

            // 7. Actualizar la Capacidad del Jugador
            stats.setLevel(targetLevel);
            stats.setCurrentLevelUUID(targetLevelData.levelUUID());
            // Resetear puntos temporales
            stats.setStrengthPoints(0);
            stats.setDexterityPoints(0);
            stats.setVitalityPoints(0);
            stats.setConstitutionPoints(0);
            stats.setIntelligencePoints(0);
            stats.setStrengthIterations(0);
            stats.setDexterityIterations(0);
            stats.setVitalityIterations(0);
            stats.setConstitutionIterations(0);
            stats.setIntelligenceIterations(0);

            // 8. Eliminar Datos Persistentes de niveles superiores
            PersistenceService.deleteLevelDataAbove(player, targetLevel);

            // 9. Sincronizar Capacidad y Restaurar Salud
            PacketHandler.sendToPlayer(player, new ClientboundSyncPlayerStatsPacket(stats.writeNBT()));
            player.setHealth(player.getMaxHealth());

            LOGGER.info("Jugador {} revertido exitosamente al nivel {}.", player.getName().getString(), targetLevel);
            success[0] = true; // Marcar como éxito

        }); // Fin de statsOptional.ifPresent

        return success[0]; // Devuelve el resultado
    }
}