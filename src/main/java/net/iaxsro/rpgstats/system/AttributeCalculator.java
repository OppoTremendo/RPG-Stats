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
import org.slf4j.Logger;

import java.util.UUID;
import java.util.function.Supplier;

public class AttributeCalculator {

    private static final Logger LOGGER = RpgStatsMod.LOGGER;
    /** UUID constante usada para los modificadores aplicados por nivel. */
    private static final UUID LEVEL_MODIFIER_UUID = UUID.fromString("289a8420-6947-48cd-bbdb-976f291b142c");

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
     *
     * @param entity  La entidad a la que aplicar los modificadores.
     * @param bonuses Las bonificaciones calculadas.
     */
    public static void applyAttributeModifiers(LivingEntity entity, CalculatedBonuses bonuses) {
        LOGGER.debug("Aplicando modificadores a {}", entity.getName().getString());

        // Crear nuevos modificadores con UUID constante
        String modifierName = "Level Bonus";

        AttributeModifier attackDamageModifier = new AttributeModifier(LEVEL_MODIFIER_UUID, modifierName, bonuses.attackDamageAddition, AttributeModifier.Operation.ADDITION);
        AttributeModifier movementSpeedModifier = new AttributeModifier(LEVEL_MODIFIER_UUID, modifierName, bonuses.movementSpeedMultiplier, AttributeModifier.Operation.MULTIPLY_BASE);
        AttributeModifier maxHealthModifier = new AttributeModifier(LEVEL_MODIFIER_UUID, modifierName, bonuses.totalMaxHealthAddition, AttributeModifier.Operation.ADDITION);
        AttributeModifier armorModifier = new AttributeModifier(LEVEL_MODIFIER_UUID, modifierName, bonuses.totalArmorAddition, AttributeModifier.Operation.ADDITION);
        AttributeModifier attackSpeedModifier = new AttributeModifier(LEVEL_MODIFIER_UUID, modifierName, bonuses.totalAttackSpeedAddition, AttributeModifier.Operation.ADDITION);
        AttributeModifier armorToughnessModifier = new AttributeModifier(LEVEL_MODIFIER_UUID, modifierName, bonuses.armorToughnessAddition, AttributeModifier.Operation.ADDITION);
        AttributeModifier attackKnockbackModifier = new AttributeModifier(LEVEL_MODIFIER_UUID, modifierName, bonuses.totalAttackKnockbackAddition, AttributeModifier.Operation.ADDITION);
        AttributeModifier knockbackResistanceModifier = new AttributeModifier(LEVEL_MODIFIER_UUID, modifierName, bonuses.knockbackResistanceAddition, AttributeModifier.Operation.ADDITION);
        AttributeModifier swimSpeedModifier = new AttributeModifier(LEVEL_MODIFIER_UUID, modifierName, bonuses.totalSwimSpeedAddition, AttributeModifier.Operation.ADDITION);
        // Epic Fight
        AttributeModifier impactModifier = new AttributeModifier(LEVEL_MODIFIER_UUID, modifierName, bonuses.totalImpactAddition, AttributeModifier.Operation.ADDITION);
        AttributeModifier armorNegationModifier = new AttributeModifier(LEVEL_MODIFIER_UUID, modifierName, bonuses.totalArmorNegationAddition, AttributeModifier.Operation.ADDITION);
        AttributeModifier stunArmorModifier = new AttributeModifier(LEVEL_MODIFIER_UUID, modifierName, bonuses.stunArmorAddition, AttributeModifier.Operation.ADDITION);
        AttributeModifier staminaModifier = new AttributeModifier(LEVEL_MODIFIER_UUID, modifierName, bonuses.totalStaminaAddition, AttributeModifier.Operation.ADDITION);
        AttributeModifier staminaRegenModifier = new AttributeModifier(LEVEL_MODIFIER_UUID, modifierName, bonuses.totalStaminaRegenAddition, AttributeModifier.Operation.ADDITION);
        // Weight Reduction: el bonus.totalWeightReduction es positivo, por lo que se niega.
        AttributeModifier weightReductionModifier = new AttributeModifier(LEVEL_MODIFIER_UUID, modifierName, -bonuses.totalWeightReduction, AttributeModifier.Operation.MULTIPLY_BASE);

        // Aplicar nuevos modificadores
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

        LOGGER.debug("Modificadores aplicados exitosamente.");
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