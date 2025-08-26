package net.iaxsro.rpgstats.system;

import net.iaxsro.rpgstats.RpgStatsMod;
import net.iaxsro.rpgstats.config.ModConfig;
import net.iaxsro.rpgstats.registry.AttributeRegistry;
import net.iaxsro.rpgstats.util.AttributeUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeConfigSpec;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.UUID;
import java.util.function.Supplier;

public class AttributeCalculator {

    private static final Logger LOGGER = RpgStatsMod.LOGGER;

    // Atributos opcionales (EpicFight, Forge) - Obtenidos de forma segura
    // Es mejor obtenerlos una vez si se usan mucho, o usar Suppliers
    private static final Supplier<Attribute> EF_IMPACT = AttributeUtil.registrySupplier(new ResourceLocation("epicfight:impact"));
    private static final Supplier<Attribute> EF_ARMOR_NEGATION = AttributeUtil.registrySupplier(new ResourceLocation("epicfight:armor_negation"));
    private static final Supplier<Attribute> EF_STUN_ARMOR = AttributeUtil.registrySupplier(new ResourceLocation("epicfight:stun_armor"));
    private static final Supplier<Attribute> EF_MAX_STAMINA = AttributeUtil.registrySupplier(new ResourceLocation("epicfight:staminar")); // nombre original 'staminar'
    private static final Supplier<Attribute> EF_STAMINA_REGEN = AttributeUtil.registrySupplier(new ResourceLocation("epicfight:stamina_regen"));
    private static final Supplier<Attribute> EF_WEIGHT = AttributeUtil.registrySupplier(new ResourceLocation("epicfight:weight"));
    private static final Supplier<Attribute> FORGE_SWIM_SPEED = AttributeUtil.registrySupplier(new ResourceLocation("forge:swim_speed"));


    /**
     * Calcula las bonificaciones derivadas basadas en los atributos base actuales de la entidad.
     *
     * @param entity La entidad para la cual calcular las bonificaciones.
     * @return Un objeto CalculatedBonuses con todos los valores calculados.
     */
    public static CalculatedBonuses calculateBonuses(LivingEntity entity) {
        double strengthVal = entity.getAttributeBaseValue(AttributeRegistry.STRENGTH.get());
        double dexterityVal = entity.getAttributeBaseValue(AttributeRegistry.DEXTERITY.get());
        double vitalityVal = entity.getAttributeBaseValue(AttributeRegistry.VITALITY.get());
        double constitutionVal = entity.getAttributeBaseValue(AttributeRegistry.CONSTITUTION.get());
        // Intelligence no parece usarse en los cálculos originales, añadir si es necesario

        return new CalculatedBonuses(strengthVal, dexterityVal, vitalityVal, constitutionVal);
    }

    /**
     * Aplica las bonificaciones calculadas como modificadores de atributos permanentes a la entidad.
     * También elimina los modificadores del nivel anterior si se proporciona el UUID.
     *
     * @param entity            La entidad a la que aplicar los modificadores.
     * @param bonuses           Las bonificaciones calculadas.
     * @param levelNumber       El número del nivel actual (para el nombre del modificador).
     * @param levelUUID         El UUID único para los modificadores de este nivel.
     * @param previousLevelUUID El UUID de los modificadores del nivel anterior (puede ser null).
     */
    public static void applyAttributeModifiers(LivingEntity entity, CalculatedBonuses bonuses, int levelNumber, UUID levelUUID, @Nullable UUID previousLevelUUID) {
        LOGGER.debug("Aplicando modificadores para nivel {} (UUID: {}) a {}", levelNumber, levelUUID, entity.getName().getString());

        // 1. Eliminar modificadores del nivel anterior
        if (previousLevelUUID != null) {
            LOGGER.debug("Eliminando modificadores del nivel anterior (UUID: {})", previousLevelUUID);
            // Vanilla Attributes
            AttributeUtil.removePermanentModifier(entity, Attributes.ATTACK_DAMAGE, previousLevelUUID);
            AttributeUtil.removePermanentModifier(entity, Attributes.MOVEMENT_SPEED, previousLevelUUID);
            AttributeUtil.removePermanentModifier(entity, Attributes.MAX_HEALTH, previousLevelUUID);
            AttributeUtil.removePermanentModifier(entity, Attributes.ARMOR, previousLevelUUID);
            AttributeUtil.removePermanentModifier(entity, Attributes.ATTACK_SPEED, previousLevelUUID);
            AttributeUtil.removePermanentModifier(entity, Attributes.ARMOR_TOUGHNESS, previousLevelUUID);
            AttributeUtil.removePermanentModifier(entity, Attributes.ATTACK_KNOCKBACK, previousLevelUUID);
            AttributeUtil.removePermanentModifier(entity, Attributes.KNOCKBACK_RESISTANCE, previousLevelUUID);
            // Forge Attributes
            AttributeUtil.removePermanentModifier(entity, FORGE_SWIM_SPEED, previousLevelUUID);
            // Epic Fight Attributes (usando Suppliers)
            AttributeUtil.removePermanentModifier(entity, EF_IMPACT, previousLevelUUID);
            AttributeUtil.removePermanentModifier(entity, EF_ARMOR_NEGATION, previousLevelUUID);
            AttributeUtil.removePermanentModifier(entity, EF_STUN_ARMOR, previousLevelUUID);
            AttributeUtil.removePermanentModifier(entity, EF_MAX_STAMINA, previousLevelUUID);
            AttributeUtil.removePermanentModifier(entity, EF_STAMINA_REGEN, previousLevelUUID);
            AttributeUtil.removePermanentModifier(entity, EF_WEIGHT, previousLevelUUID);
        }

        // 2. Crear nuevos modificadores
        String modifierName = "Level " + levelNumber + " Bonus"; // Nombre más descriptivo

        // Creamos los modificadores usando los valores de 'bonuses' y el nuevo levelUUID
        AttributeModifier attackDamageModifier = new AttributeModifier(levelUUID, modifierName, bonuses.attackDamageAddition, AttributeModifier.Operation.ADDITION);
        AttributeModifier movementSpeedModifier = new AttributeModifier(levelUUID, modifierName, bonuses.movementSpeedMultiplier, AttributeModifier.Operation.MULTIPLY_BASE); // Cambiado a Multiplicador
        AttributeModifier maxHealthModifier = new AttributeModifier(levelUUID, modifierName, bonuses.totalMaxHealthAddition, AttributeModifier.Operation.ADDITION);
        AttributeModifier armorModifier = new AttributeModifier(levelUUID, modifierName, bonuses.totalArmorAddition, AttributeModifier.Operation.ADDITION);
        AttributeModifier attackSpeedModifier = new AttributeModifier(levelUUID, modifierName, bonuses.totalAttackSpeedAddition, AttributeModifier.Operation.ADDITION);
        AttributeModifier armorToughnessModifier = new AttributeModifier(levelUUID, modifierName, bonuses.armorToughnessAddition, AttributeModifier.Operation.ADDITION);
        AttributeModifier attackKnockbackModifier = new AttributeModifier(levelUUID, modifierName, bonuses.totalAttackKnockbackAddition, AttributeModifier.Operation.ADDITION);
        AttributeModifier knockbackResistanceModifier = new AttributeModifier(levelUUID, modifierName, bonuses.knockbackResistanceAddition, AttributeModifier.Operation.ADDITION);
        AttributeModifier swimSpeedModifier = new AttributeModifier(levelUUID, modifierName, bonuses.totalSwimSpeedAddition, AttributeModifier.Operation.ADDITION);
        // Epic Fight
        AttributeModifier impactModifier = new AttributeModifier(levelUUID, modifierName, bonuses.totalImpactAddition, AttributeModifier.Operation.ADDITION);
        AttributeModifier armorNegationModifier = new AttributeModifier(levelUUID, modifierName, bonuses.totalArmorNegationAddition, AttributeModifier.Operation.ADDITION);
        AttributeModifier stunArmorModifier = new AttributeModifier(levelUUID, modifierName, bonuses.stunArmorAddition, AttributeModifier.Operation.ADDITION);
        AttributeModifier staminaModifier = new AttributeModifier(levelUUID, modifierName, bonuses.totalStaminaAddition, AttributeModifier.Operation.ADDITION);
        AttributeModifier staminaRegenModifier = new AttributeModifier(levelUUID, modifierName, bonuses.totalStaminaRegenAddition, AttributeModifier.Operation.ADDITION);
        // Weight Reduction: Originalmente era MULTIPLY_TOTAL con valor negativo.
        // MULTIPLY_TOTAL aplica: Base * (1 + Mod1) * (1 + Mod2) ...
        // Si queremos reducir un 10% (0.1), el modificador debe ser -0.1
        // El bonus.totalWeightReduction es positivo, así que lo negamos.
        AttributeModifier weightReductionModifier = new AttributeModifier(levelUUID, modifierName, -bonuses.totalWeightReduction, AttributeModifier.Operation.MULTIPLY_BASE); // Cambiado a MULTIPLY_BASE para consistencia

        // 3. Aplicar nuevos modificadores
        LOGGER.debug("Aplicando nuevos modificadores...");
        // Vanilla Attributes
        AttributeUtil.addPermanentModifier(entity, Attributes.ATTACK_DAMAGE, attackDamageModifier);
        AttributeUtil.addPermanentModifier(entity, Attributes.MOVEMENT_SPEED, movementSpeedModifier);
        AttributeUtil.addPermanentModifier(entity, Attributes.MAX_HEALTH, maxHealthModifier);
        AttributeUtil.addPermanentModifier(entity, Attributes.ARMOR, armorModifier);
        AttributeUtil.addPermanentModifier(entity, Attributes.ATTACK_SPEED, attackSpeedModifier);
        AttributeUtil.addPermanentModifier(entity, Attributes.ARMOR_TOUGHNESS, armorToughnessModifier);
        AttributeUtil.addPermanentModifier(entity, Attributes.ATTACK_KNOCKBACK, attackKnockbackModifier);
        AttributeUtil.addPermanentModifier(entity, Attributes.KNOCKBACK_RESISTANCE, knockbackResistanceModifier);
        // Forge Attributes
        AttributeUtil.addPermanentModifier(entity, FORGE_SWIM_SPEED, swimSpeedModifier);
        // Epic Fight Attributes (usando Suppliers)
        AttributeUtil.addPermanentModifier(entity, EF_IMPACT, impactModifier);
        AttributeUtil.addPermanentModifier(entity, EF_ARMOR_NEGATION, armorNegationModifier);
        AttributeUtil.addPermanentModifier(entity, EF_STUN_ARMOR, stunArmorModifier);
        AttributeUtil.addPermanentModifier(entity, EF_MAX_STAMINA, staminaModifier);
        AttributeUtil.addPermanentModifier(entity, EF_STAMINA_REGEN, staminaRegenModifier);
        AttributeUtil.addPermanentModifier(entity, EF_WEIGHT, weightReductionModifier);

        LOGGER.debug("Modificadores aplicados exitosamente para nivel {}.", levelNumber);
    }


    // --- Clase Interna para Almacenar Bonificaciones Calculadas ---

    /**
     * Contiene los valores de las bonificaciones calculadas a partir de los atributos base.
     * Estos valores son los que se usarán para crear los AttributeModifiers.
     */
    public static class CalculatedBonuses {
        // Valores individuales (podrían ser útiles para logs o desglose)
        public final double attackDamageAddition;
        public final double impactAdditionStrength;
        public final double impactAdditionConstitution;
        public final double armorNegationAdditionStrength;
        public final double armorNegationAdditionDexterity;
        public final double movementSpeedMultiplier; // Cambiado a Multiplicador
        public final double staminaAdditionDexterity;
        public final double staminaAdditionVitality;
        public final double staminaRegenAdditionDexterity;
        public final double staminaRegenAdditionVitality;
        public final double maxHealthAdditionVitality;
        public final double maxHealthAdditionConstitution;
        public final double armorAdditionVitality;
        public final double armorAdditionConstitution;
        public final double stunArmorAddition;
        public final double swimSpeedAdditionStrength;
        public final double swimSpeedAdditionDexterity;
        public final double attackSpeedAdditionStrength;
        public final double attackSpeedAdditionDexterity;
        public final double weightReductionVitality;
        public final double weightReductionConstitution;
        public final double armorToughnessAddition;
        public final double knockbackResistanceAddition;
        public final double attackKnockbackAdditionStrength;
        public final double attackKnockbackAdditionConstitution;

        // Totales combinados (usados para los modificadores)
        public final double totalImpactAddition;
        public final double totalArmorNegationAddition;
        public final double totalStaminaAddition;
        public final double totalStaminaRegenAddition;
        public final double totalMaxHealthAddition;
        public final double totalArmorAddition;
        public final double totalAttackKnockbackAddition;
        public final double totalSwimSpeedAddition;
        public final double totalAttackSpeedAddition;
        public final double totalWeightReduction; // Valor como porcentaje (0.0 a 1.0)

        /**
         * Constructor que calcula todas las bonificaciones.
         *
         * @param strengthBase     Valor base actual de Fuerza.
         * @param dexterityBase    Valor base actual de Destreza.
         * @param vitalityBase     Valor base actual de Vitalidad.
         * @param constitutionBase Valor base actual de Constitución.
         */
        public CalculatedBonuses(double strengthBase, double dexterityBase, double vitalityBase, double constitutionBase) {

            // --- Cálculos basados en Configuración y Atributos Base ---
            // Fuerza
            this.attackDamageAddition = calculateBonus(strengthBase, ModConfig.COMMON.strengthAttackDamageDivider);
            this.impactAdditionStrength = calculateBonus(strengthBase, ModConfig.COMMON.strengthImpactDivider);
            this.armorNegationAdditionStrength = calculateBonus(strengthBase, ModConfig.COMMON.strengthArmorNegationDivider);
            this.swimSpeedAdditionStrength = calculateBonus(strengthBase, ModConfig.COMMON.strengthSwimSpeedDivider);
            this.attackSpeedAdditionStrength = calculateBonus(strengthBase, ModConfig.COMMON.strengthAttackSpeedDivider);
            this.attackKnockbackAdditionStrength = calculateBonus(strengthBase, ModConfig.COMMON.strengthAttackKnockbackDivider);

            // Destreza
            this.armorNegationAdditionDexterity = calculateBonus(dexterityBase, ModConfig.COMMON.dexterityArmorNegationDivider);
            // Para Velocidad de Movimiento: Multiplicador = Dex / Divider
            this.movementSpeedMultiplier = calculateBonus(dexterityBase, ModConfig.COMMON.dexterityMovementSpeedDivider);
            this.staminaAdditionDexterity = calculateBonus(dexterityBase, ModConfig.COMMON.dexterityStaminaDivider);
            this.staminaRegenAdditionDexterity = calculateBonus(dexterityBase, ModConfig.COMMON.dexterityStaminaRegenDivider);
            this.swimSpeedAdditionDexterity = calculateBonus(dexterityBase, ModConfig.COMMON.dexteritySwimSpeedDivider);
            this.attackSpeedAdditionDexterity = calculateBonus(dexterityBase, ModConfig.COMMON.dexterityAttackSpeedDivider);

            // Vitalidad
            this.maxHealthAdditionVitality = calculateBonus(vitalityBase, ModConfig.COMMON.vitalityMaxHealthDivider);
            this.staminaRegenAdditionVitality = calculateBonus(vitalityBase, ModConfig.COMMON.vitalityStaminaRegenDivider);
            this.armorAdditionVitality = calculateBonus(vitalityBase, ModConfig.COMMON.vitalityArmorDivider);
            this.staminaAdditionVitality = calculateBonus(vitalityBase, ModConfig.COMMON.vitalityStaminaDivider);
            this.weightReductionVitality = calculateBonus(vitalityBase, ModConfig.COMMON.vitalityWeightReductionDivider);

            // Constitución
            this.maxHealthAdditionConstitution = calculateBonus(constitutionBase, ModConfig.COMMON.constitutionMaxHealthDivider);
            this.armorAdditionConstitution = calculateBonus(constitutionBase, ModConfig.COMMON.constitutionArmorDivider);
            this.stunArmorAddition = calculateBonus(constitutionBase, ModConfig.COMMON.constitutionStunArmorDivider);
            this.impactAdditionConstitution = calculateBonus(constitutionBase, ModConfig.COMMON.constitutionImpactDivider);
            this.knockbackResistanceAddition = calculateBonus(constitutionBase, ModConfig.COMMON.constitutionKnockbackResistanceDivider);
            this.weightReductionConstitution = calculateBonus(constitutionBase, ModConfig.COMMON.constitutionWeightReductionDivider);
            this.attackKnockbackAdditionConstitution = calculateBonus(constitutionBase, ModConfig.COMMON.constitutionAttackKnockbackDivider);
            this.armorToughnessAddition = calculateBonus(constitutionBase, ModConfig.COMMON.constitutionArmorToughnessDivider);


            // --- Cálculo de Totales ---
            this.totalImpactAddition = this.impactAdditionStrength + this.impactAdditionConstitution;
            this.totalArmorNegationAddition = this.armorNegationAdditionStrength + this.armorNegationAdditionDexterity;
            this.totalStaminaAddition = this.staminaAdditionDexterity + this.staminaAdditionVitality;
            this.totalStaminaRegenAddition = this.staminaRegenAdditionDexterity + this.staminaRegenAdditionVitality;
            this.totalMaxHealthAddition = this.maxHealthAdditionVitality + this.maxHealthAdditionConstitution;
            this.totalArmorAddition = this.armorAdditionVitality + this.armorAdditionConstitution;
            this.totalAttackKnockbackAddition = this.attackKnockbackAdditionStrength + this.attackKnockbackAdditionConstitution;
            this.totalSwimSpeedAddition = this.swimSpeedAdditionStrength + this.swimSpeedAdditionDexterity;
            this.totalAttackSpeedAddition = this.attackSpeedAdditionDexterity + this.attackSpeedAdditionStrength;
            // Reducción de peso como porcentaje (0 a 1)
            this.totalWeightReduction = this.weightReductionVitality + this.weightReductionConstitution;

            LOGGER.trace("Bonificaciones calculadas: MaxHealth={}, Armor={}, Impact={}",
                    this.totalMaxHealthAddition, this.totalArmorAddition, this.totalImpactAddition);
        }

        /**
         * Helper para calcular bonificación asegurando que el divisor no sea cero.
         */
        private double calculateBonus(double baseValue, ForgeConfigSpec.DoubleValue dividerConfig) {
            double divider = dividerConfig.get();
            return (divider > 1e-6) ? (baseValue / divider) : 0.0; // Evita división por cero o valores muy pequeños
        }
    }
}