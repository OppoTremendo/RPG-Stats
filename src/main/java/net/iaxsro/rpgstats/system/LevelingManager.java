package net.iaxsro.rpgstats.system;

import net.iaxsro.rpgstats.RpgStatsMod;
import net.iaxsro.rpgstats.capabilities.IPlayerStats;
import net.iaxsro.rpgstats.capabilities.PlayerStats;
import net.iaxsro.rpgstats.network.ClientboundSyncPlayerStatsPacket;
import net.iaxsro.rpgstats.network.PacketHandler;
import net.iaxsro.rpgstats.registry.AttributeRegistry;
import net.iaxsro.rpgstats.util.AttributeUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.common.util.LazyOptional;
import org.slf4j.Logger;

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
                UUID previousLevelUUID = stats.getCurrentLevelUUID();
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

                // 4. Actualizar Nivel y UUID en la Capacidad
                stats.setLevel(newLevelNumber);
                stats.setCurrentLevelUUID(newLevelUUID);

                // 5. Recalcular Bonificaciones
                AttributeCalculator.CalculatedBonuses newBonuses = AttributeCalculator.calculateBonuses(player);

                // 6. Aplicar Modificadores
                AttributeCalculator.applyAttributeModifiers(player, newBonuses, newLevelNumber, newLevelUUID, previousLevelUUID);

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
                UUID currentUUID = stats.getCurrentLevelUUID();

                if (currentUUID != null && currentLevel > 0) {
                    AttributeCalculator.CalculatedBonuses currentBonuses = AttributeCalculator.calculateBonuses(player);
                    // Elimina modificadores existentes del mismo nivel y vuelve a aplicarlos
                    AttributeCalculator.applyAttributeModifiers(player, currentBonuses, currentLevel, currentUUID, currentUUID);
                    LOGGER.debug("Modificadores reaplicados post-respawn para nivel {}.", currentLevel);
                } else if (currentLevel > 0) {
                    // Caso anómalo: tiene nivel pero no UUID.
                    LOGGER.warn("Jugador {} tiene nivel {} pero no UUID de nivel al respawnear. Reaplicación de modificadores puede ser incompleta.", player.getName().getString(), currentLevel);
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


}