package net.iaxsro.rpgstats.util;

import com.google.common.base.Suppliers;
import net.iaxsro.rpgstats.RpgStatsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Clase de utilidad para interactuar de forma segura y conveniente con el sistema
 * de atributos de Minecraft y atributos personalizados.
 * Proporciona métodos para añadir/eliminar modificadores y obtener información de atributos.
 */
public final class AttributeUtil { // Clase final, no se puede heredar

    private static final Logger LOGGER = RpgStatsMod.LOGGER;

    // Constructor privado para prevenir instanciación
    private AttributeUtil() {
    }

    /**
     * Añade de forma segura un modificador permanente a un atributo de la entidad.
     * Realiza comprobaciones de nulos para la entidad, el atributo y la instancia del atributo.
     * Si el modificador ya existe (mismo UUID), no lo añade de nuevo.
     *
     * @param entity    La entidad a modificar (no debe ser null).
     * @param attribute El atributo a modificar (puede ser null, en cuyo caso no se hace nada).
     * @param modifier  El modificador a añadir (no debe ser null).
     */
    public static void addPermanentModifier(@NotNull LivingEntity entity, @Nullable Attribute attribute, @NotNull AttributeModifier modifier) {
        // Quitamos la comprobación de null para entity y modifier aquí, confiando en @NotNull y el uso correcto.
        // Mantenemos la comprobación para attribute ya que puede ser opcional.
        if (attribute == null) {
            LOGGER.trace("Intento de añadir modificador a un atributo nulo para {}. Ignorando.", entity.getName().getString());
            return;
        }

        AttributeInstance attributeInstance = entity.getAttribute(attribute);
        if (attributeInstance != null) {
            // addPermanentModifier ya maneja la existencia del UUID, pero podemos loguear
            if (!attributeInstance.hasModifier(modifier)) { // Chequea por UUID
                attributeInstance.addPermanentModifier(modifier);
                LOGGER.trace("Modificador '{}' ({}) añadido al atributo '{}' para {}",
                        modifier.getName(), modifier.getId(), getAttributeName(attribute), entity.getName().getString());
            } else {
                LOGGER.trace("Modificador '{}' ({}) ya existe en el atributo '{}' para {}. No se añadió de nuevo.",
                        modifier.getName(), modifier.getId(), getAttributeName(attribute), entity.getName().getString());
            }
        } else {
            LOGGER.warn("La entidad {} no tiene el atributo '{}'. No se pudo añadir el modificador '{}'.",
                    entity.getName().getString(), getAttributeName(attribute), modifier.getName());
        }
    }

    /**
     * Sobrecarga para añadir un modificador usando un Supplier para el atributo.
     * Útil para atributos opcionales (ej: de otros mods) que podrían no estar presentes.
     *
     * @param entity            La entidad a modificar (no debe ser null).
     * @param attributeSupplier Supplier que provee el atributo (puede devolver null si no existe).
     * @param modifier          El modificador a añadir (no debe ser null).
     */
    public static void addPermanentModifier(@NotNull LivingEntity entity, @NotNull Supplier<Attribute> attributeSupplier, @NotNull AttributeModifier modifier) {
        Attribute attribute = attributeSupplier.get(); // Obtiene el atributo del supplier
        if (attribute != null) { // Solo procede si el atributo existe
            addPermanentModifier(entity, attribute, modifier);
        } else {
            // Loguear a nivel TRACE ya que es esperado que atributos opcionales falten
            LOGGER.trace("Atributo opcional (proveído por Supplier) no encontrado para {}. No se añadió el modificador '{}'.", entity.getName().getString(), modifier.getName());
        }
    }

    /**
     * Elimina de forma segura un modificador permanente de un atributo de la entidad, usando su UUID.
     * Realiza comprobaciones de nulos.
     *
     * @param entity       La entidad a modificar (no debe ser null).
     * @param attribute    El atributo del cual eliminar el modificador (puede ser null, en cuyo caso no se hace nada).
     * @param modifierUUID El UUID del modificador a eliminar (no debe ser null).
     */
    public static void removePermanentModifier(@NotNull LivingEntity entity, @Nullable Attribute attribute, @NotNull UUID modifierUUID) {
        if (attribute == null) {
            LOGGER.trace("Intento de eliminar modificador de un atributo nulo para {}. Ignorando.", entity.getName().getString());
            return;
        }

        AttributeInstance attributeInstance = entity.getAttribute(attribute);
        if (attributeInstance != null) {
            boolean removed = attributeInstance.removePermanentModifier(modifierUUID);
            if (removed) {
                LOGGER.trace("Modificador con UUID {} eliminado del atributo '{}' para {}",
                        modifierUUID, getAttributeName(attribute), entity.getName().getString());
            } else {
                // Esto es común si el modificador no estaba presente (ej: al bajar de nivel)
                LOGGER.trace("No se encontró modificador con UUID {} en el atributo '{}' para {}. No se eliminó nada.",
                        modifierUUID, getAttributeName(attribute), entity.getName().getString());
            }
        } else {
            LOGGER.warn("La entidad {} no tiene el atributo '{}'. No se pudo intentar eliminar el modificador UUID {}.",
                    entity.getName().getString(), getAttributeName(attribute), modifierUUID);
        }
    }

    /**
     * Sobrecarga para eliminar un modificador usando un Supplier para el atributo.
     * Útil para atributos opcionales.
     *
     * @param entity            La entidad a modificar (no debe ser null).
     * @param attributeSupplier Supplier que provee el atributo (puede devolver null si no existe).
     * @param modifierUUID      El UUID del modificador a eliminar (no debe ser null).
     */
    public static void removePermanentModifier(@NotNull LivingEntity entity, @NotNull Supplier<Attribute> attributeSupplier, @NotNull UUID modifierUUID) {
        Attribute attribute = attributeSupplier.get();
        if (attribute != null) {
            removePermanentModifier(entity, attribute, modifierUUID);
        } else {
            LOGGER.trace("Atributo opcional (proveído por Supplier) no encontrado para {}. No se intentó eliminar el modificador UUID {}.", entity.getName().getString(), modifierUUID);
        }
    }

    /**
     * Crea un Supplier memoizado (cacheado) para obtener un atributo del registro por su ResourceLocation.
     * Eficiente para buscar atributos opcionales o de otros mods repetidamente, ya que el resultado
     * (encontrado o no encontrado) se cachea después de la primera llamada.
     *
     * @param location La ResourceLocation del atributo (no debe ser null).
     * @return Un Supplier que devuelve el Attribute o null si no se encuentra en el registro.
     */
    @NotNull
    public static Supplier<Attribute> registrySupplier(@NotNull ResourceLocation location) {
        // Suppliers.memoize asegura que getValue se llame solo una vez por Supplier instance.
        return Suppliers.memoize(() -> ForgeRegistries.ATTRIBUTES.getValue(location));
        // Alternativa si Guava no es deseado:
        // return new Supplier<>() {
        //     private Attribute cachedValue = null;
        //     private boolean initialized = false;
        //     @Override public Attribute get() {
        //         if (!initialized) {
        //             cachedValue = ForgeRegistries.ATTRIBUTES.getValue(location);
        //             initialized = true;
        //         }
        //         return cachedValue;
        //     }
        // };
    }

    /**
     * Obtiene el nombre de registro de un atributo de forma segura (para logging o display).
     * Devuelve el ResourceLocation como string si está registrado, o el ID de descripción como fallback,
     * o un placeholder si no se puede determinar.
     *
     * @param attribute El atributo (puede ser null).
     * @return Una representación en String del nombre del atributo, o un placeholder como "unknown (null)".
     */
    @NotNull
    public static String getAttributeName(@Nullable Attribute attribute) { // <--- AHORA ES PÚBLICO
        if (attribute == null) return "unknown (null)";

        // Intenta obtener la clave de registro (ResourceLocation)
        ResourceLocation key = ForgeRegistries.ATTRIBUTES.getKey(attribute);
        if (key != null) {
            return key.toString(); // Devuelve ej: "minecraft:generic.max_health" o "rpgstats:strength"
        }

        // Si no tiene clave (raro para atributos registrados), usa el ID de descripción
        String descriptionId = attribute.getDescriptionId(); // ej: "attribute.name.generic.max_health"
        return Objects.requireNonNullElse(descriptionId, "unknown (no key/descId)"); // Devuelve descriptionId o el placeholder final
    }
}