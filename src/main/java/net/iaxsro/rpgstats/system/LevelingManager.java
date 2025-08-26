package net.iaxsro.rpgstats.system;

import net.iaxsro.rpgstats.RpgStatsMod;
import net.iaxsro.rpgstats.capabilities.IPlayerStats;
import net.iaxsro.rpgstats.capabilities.PlayerStats;
import net.iaxsro.rpgstats.network.ClientboundSyncPlayerStatsPacket;
import net.iaxsro.rpgstats.network.PacketHandler;
import net.iaxsro.rpgstats.registry.AttributeRegistry;
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
                UUID previousLevelUUID = PersistenceService.getUUIDForLevel(player, currentLevel);
                UUID newLevelUUID = UUID.randomUUID();
                int newLevelNumber = currentLevel + 1;

                LOGGER.debug("Nivel actual: {}, UUID anterior: {}", currentLevel, previousLevelUUID);
                LOGGER.debug("Nuevo nivel: {}, Nuevo UUID: {}", newLevelNumber, newLevelUUID);

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

                // 6. Aplicar Modificadores
                AttributeCalculator.applyAttributeModifiers(player, newBonuses, newLevelNumber, newLevelUUID, previousLevelUUID);

                // 7. Persistir Datos del Nuevo Nivel
                PersistenceService.saveLevelData(player, newLevelNumber, newLevelUUID, newBonuses);
                LOGGER.debug("Datos del nivel {} persistidos.", newLevelNumber);

                // 8. Sincronizar Capacidad Actualizada al Cliente
                PacketHandler.sendToPlayer(player, new ClientboundSyncPlayerStatsPacket(stats.writeNBT()));
                LOGGER.debug("Capacidad PlayerStats sincronizada al cliente.");

                // 9. Restaurar Salud
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
                UUID currentUUID = PersistenceService.getUUIDForLevel(player, currentLevel);

                if (currentUUID != null && currentLevel > 0) {
                    UUID previousUUID = PersistenceService.getUUIDForLevel(player, currentLevel - 1);
                    LOGGER.debug("Respawn: Intentando obtener UUID para nivel previo {}: {}", currentLevel - 1, previousUUID);

                    AttributeCalculator.CalculatedBonuses currentBonuses = AttributeCalculator.calculateBonuses(player);
                    AttributeCalculator.applyAttributeModifiers(player, currentBonuses, currentLevel, currentUUID, previousUUID);
                    LOGGER.debug("Modificadores reaplicados post-respawn para nivel {}.", currentLevel);

                } else if (currentLevel > 0) {
                    // Caso anómalo: tiene nivel pero no UUID.
                    LOGGER.warn("Jugador {} tiene nivel {} pero no UUID de nivel al respawnear. Reaplicación de modificadores puede ser incompleta.", player.getName().getString(), currentLevel);
                    // Considerar si intentar aplicar sin quitar los previos:
                    // AttributeCalculator.CalculatedBonuses currentBonuses = AttributeCalculator.calculateBonuses(player);
                    // AttributeCalculator.applyAttributeModifiers(player, currentBonuses, currentLevel, UUID.randomUUID(), null); // Ojo con UUID random
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
            UUID currentLevelUUID = PersistenceService.getUUIDForLevel(player, stats.getLevel());
            AttributeCalculator.applyAttributeModifiers(player, targetBonuses, targetLevel, targetLevelData.levelUUID(), currentLevelUUID); // Usa el UUID *actual* como 'previous'

            // 7. Actualizar la Capacidad del Jugador
            stats.setLevel(targetLevel);
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