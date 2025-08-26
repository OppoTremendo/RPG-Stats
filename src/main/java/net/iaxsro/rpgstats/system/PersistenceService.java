package net.iaxsro.rpgstats.system;

import com.google.gson.*;
import net.iaxsro.rpgstats.RpgStatsMod;
import net.iaxsro.rpgstats.registry.AttributeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public final class PersistenceService {

    private static final Logger LOGGER = RpgStatsMod.LOGGER;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String STATS_SUBDIR = "rpgstats_playerdata";
    private static final String FILENAME_FORMAT = "LV%d_Stats.json";

    // Constantes para claves JSON
    private static final String KEY_LEVEL_DATA_ROOT = "Level %d Data"; // Formato para la clave raíz
    private static final String KEY_UUID = "UUID";
    private static final String KEY_LEVEL_NUMBER = "LevelNumber";
    private static final String KEY_BASE_ATTRIBUTES = "BaseAttributes";
    private static final String KEY_CALCULATED_BONUSES = "CalculatedTotalBonuses";


    private PersistenceService() {
    }

    /**
     * Obtiene el directorio base para los datos de RPGStats de un jugador específico.
     * Usa el UUID del jugador para mayor robustez.
     * Ejemplo: saves/<worldname>/rpgstats_playerdata/<player_uuid>/
     */
    @Nullable
    private static Path getPlayerStatsDirectory(Player player) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            LOGGER.error("No se pudo obtener la instancia del servidor para determinar la ruta de guardado.");
            return null;
        }
        String playerUUID = player.getUUID().toString();
        Path worldSavePath = server.getWorldPath(LevelResource.ROOT);
        return worldSavePath.resolve(STATS_SUBDIR).resolve(playerUUID);
    }

    /**
     * Obtiene la ruta completa al archivo JSON para un nivel específico de un jugador.
     */
    @Nullable
    private static Path getLevelDataPath(Player player, int levelNumber) {
        Path playerDir = getPlayerStatsDirectory(player);
        if (playerDir == null) {
            return null;
        }
        String fileName = String.format(FILENAME_FORMAT, levelNumber);
        return playerDir.resolve(fileName);
    }

    /**
     * Guarda los datos relevantes de un nivel específico para un jugador en un archivo JSON.
     * Obtiene los atributos base del jugador en el momento de la llamada.
     */
    public static void saveLevelData(ServerPlayer player, int levelNumber, UUID levelUUID, AttributeCalculator.CalculatedBonuses bonuses) {
        Path filePath = getLevelDataPath(player, levelNumber);
        if (filePath == null) {
            LOGGER.error("No se pudo obtener la ruta del archivo para guardar datos del nivel {} para {}", levelNumber, player.getName().getString());
            return;
        }

        JsonObject root = new JsonObject();
        JsonObject levelData = new JsonObject();
        root.add(String.format(KEY_LEVEL_DATA_ROOT, levelNumber), levelData); // Usa constante formateada

        levelData.addProperty(KEY_UUID, levelUUID.toString());
        levelData.addProperty(KEY_LEVEL_NUMBER, levelNumber);

        // Obtener y guardar Atributos Base del Momento
        JsonObject attributePoints = new JsonObject();
        saveBaseAttributeValue(player, AttributeRegistry.STRENGTH.get(), attributePoints);
        saveBaseAttributeValue(player, AttributeRegistry.DEXTERITY.get(), attributePoints);
        saveBaseAttributeValue(player, AttributeRegistry.VITALITY.get(), attributePoints);
        saveBaseAttributeValue(player, AttributeRegistry.CONSTITUTION.get(), attributePoints);
        saveBaseAttributeValue(player, AttributeRegistry.INTELLIGENCE.get(), attributePoints);
        levelData.add(KEY_BASE_ATTRIBUTES, attributePoints);

        // Guardar Bonificaciones Calculadas (Totales)
        JsonObject totalBonuses = new JsonObject();
        totalBonuses.addProperty("AttackDamage", bonuses.attackDamageAddition);
        totalBonuses.addProperty("MovementSpeedMultiplier", bonuses.movementSpeedMultiplier);
        totalBonuses.addProperty("MaxHealth", bonuses.totalMaxHealthAddition);
        totalBonuses.addProperty("Armor", bonuses.totalArmorAddition);
        totalBonuses.addProperty("AttackSpeed", bonuses.totalAttackSpeedAddition);
        totalBonuses.addProperty("ArmorToughness", bonuses.armorToughnessAddition);
        totalBonuses.addProperty("AttackKnockback", bonuses.totalAttackKnockbackAddition);
        totalBonuses.addProperty("KnockbackResistance", bonuses.knockbackResistanceAddition);
        totalBonuses.addProperty("SwimSpeed", bonuses.totalSwimSpeedAddition);
        totalBonuses.addProperty("Impact", bonuses.totalImpactAddition);
        totalBonuses.addProperty("ArmorNegation", bonuses.totalArmorNegationAddition);
        totalBonuses.addProperty("StunArmor", bonuses.stunArmorAddition);
        totalBonuses.addProperty("Stamina", bonuses.totalStaminaAddition);
        totalBonuses.addProperty("StaminaRegen", bonuses.totalStaminaRegenAddition);
        totalBonuses.addProperty("WeightReductionMultiplier", bonuses.totalWeightReduction);
        levelData.add(KEY_CALCULATED_BONUSES, totalBonuses);

        // Escribir el Archivo JSON
        try {
            Files.createDirectories(filePath.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
                GSON.toJson(root, writer);
                LOGGER.debug("Datos del nivel {} para {} guardados en {}", levelNumber, player.getName().getString(), filePath);
            }
        } catch (IOException | JsonIOException e) {
            LOGGER.error("Error al guardar datos del nivel {} para {} en {}: {}", levelNumber, player.getName().getString(), filePath, e.getMessage());
        }
    }

    // Helper para guardar un atributo base en el JsonObject
    private static void saveBaseAttributeValue(Player player, Attribute attribute, JsonObject jsonObject) {
        ResourceLocation key = ForgeRegistries.ATTRIBUTES.getKey(attribute);
        String attributeKey = (key != null) ? key.getPath() : attribute.getDescriptionId(); // Usa path o fallback
        double value = player.getAttributeBaseValue(attribute);
        jsonObject.addProperty(attributeKey, value);
    }


    /**
     * Obtiene el UUID asociado a un número de nivel específico para un jugador.
     */
    @Nullable
    public static UUID getUUIDForLevel(Player player, int levelNumber) {
        if (levelNumber <= 0) return null;

        Path filePath = getLevelDataPath(player, levelNumber);
        if (filePath == null || !Files.exists(filePath)) {
            LOGGER.trace("Archivo de datos para nivel {} de {} no encontrado.", levelNumber, player.getName().getString());
            return null;
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject levelData = root.getAsJsonObject(String.format(KEY_LEVEL_DATA_ROOT, levelNumber)); // Usa constante formateada
            if (levelData != null && levelData.has(KEY_UUID)) {
                return UUID.fromString(levelData.get(KEY_UUID).getAsString());
            } else {
                LOGGER.warn("El archivo {} no contiene '{}' o la clave '{}'.", filePath, String.format(KEY_LEVEL_DATA_ROOT, levelNumber), KEY_UUID);
            }
        } catch (NoSuchFileException e) {
            LOGGER.trace("Archivo de datos para nivel {} de {} no encontrado (NoSuchFileException).", levelNumber, player.getName().getString());
        } catch (IOException | JsonParseException | IllegalStateException | IllegalArgumentException |
                 NullPointerException e) {
            LOGGER.error("Error al leer UUID del nivel {} para {} desde {}: {}", levelNumber, player.getName().getString(), filePath, e.getMessage());
        }
        return null;
    }


    /**
     * Carga los datos guardados (UUID y atributos base) para un nivel específico.
     */
    @Nullable
    public static LevelSaveData loadLevelSaveData(Player player, int levelNumber) {
        if (levelNumber <= 0) return null;
        Path filePath = getLevelDataPath(player, levelNumber);
        if (filePath == null || !Files.exists(filePath)) return null;

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject levelData = root.getAsJsonObject(String.format(KEY_LEVEL_DATA_ROOT, levelNumber)); // Usa constante formateada
            if (levelData == null) return null;

            UUID uuid = null;
            if (levelData.has(KEY_UUID)) {
                uuid = UUID.fromString(levelData.get(KEY_UUID).getAsString());
            }

            Map<String, Double> baseAttributes = new HashMap<>();
            if (levelData.has(KEY_BASE_ATTRIBUTES) && levelData.get(KEY_BASE_ATTRIBUTES).isJsonObject()) {
                JsonObject attributesJson = levelData.getAsJsonObject(KEY_BASE_ATTRIBUTES);
                for (Map.Entry<String, JsonElement> entry : attributesJson.entrySet()) {
                    if (entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isNumber()) {
                        baseAttributes.put(entry.getKey(), entry.getValue().getAsDouble());
                    }
                }
            }

            if (uuid != null && !baseAttributes.isEmpty()) {
                return new LevelSaveData(uuid, baseAttributes);
            }

        } catch (Exception e) {
            LOGGER.error("Error al cargar LevelSaveData del nivel {} para {} desde {}: {}", levelNumber, player.getName().getString(), filePath, e.getMessage());
        }
        return null;
    }

    /**
     * Elimina los archivos de datos de nivel para un jugador por encima de un nivel objetivo.
     */
    public static void deleteLevelDataAbove(Player player, int targetLevel) {
        Path playerDir = getPlayerStatsDirectory(player);
        if (playerDir == null || !Files.isDirectory(playerDir)) {
            return;
        }

        LOGGER.warn("Eliminando datos de nivel > {} para jugador UUID {}", targetLevel, player.getUUID());
        try (Stream<Path> stream = Files.list(playerDir)) {
            stream.filter(path -> path.getFileName().toString().matches("LV\\d+_Stats\\.json"))
                    .forEach(path -> {
                        try {
                            String fileName = path.getFileName().toString();
                            // Extraer número de nivel del nombre de archivo
                            int levelNum = Integer.parseInt(fileName.substring(2, fileName.indexOf("_")));
                            if (levelNum > targetLevel) {
                                if (Files.deleteIfExists(path)) {
                                    LOGGER.debug("Archivo eliminado: {}", path);
                                }
                            }
                        } catch (NumberFormatException | IOException | StringIndexOutOfBoundsException e) {
                            LOGGER.error("Error procesando o eliminando archivo {}: {}", path, e.getMessage());
                        }
                    });
        } catch (IOException e) {
            LOGGER.error("Error al listar directorio {} para eliminar archivos de nivel: {}", playerDir, e.getMessage());
        }
    }

    /**
     * Record simple para contener los datos cargados de un archivo de nivel.
     * Usado principalmente para la funcionalidad de bajar de nivel.
     *
     * @param levelUUID      El UUID asociado con este nivel.
     * @param baseAttributes Un mapa donde la clave es el nombre del atributo (ej: "strength")
     *                       y el valor es el valor base que tenía en ese nivel.
     */
    public record LevelSaveData(UUID levelUUID, Map<String, Double> baseAttributes) {
    }

}