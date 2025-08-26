package net.iaxsro.rpgstats.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

// Nota: Esta clase ahora solo define las configuraciones. No las registra.
public class ModConfig {

    // Separa la definición de la especificación (SPEC) y los valores (COMMON)
    public static final Common COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;

    // Bloque estático para inicializar la configuración
    static {
        // Crea un par (Pair) que contiene la implementación de Common y la especificación de ForgeConfigSpec
        final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
        // Asigna la especificación y la implementación a las constantes finales
        COMMON_SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    // Clase interna que contiene todas las opciones de configuración
    public static class Common {

        // --- The Formula ---
        public final ForgeConfigSpec.DoubleValue pointsMultiplier;
        public final ForgeConfigSpec.DoubleValue counterVariation;
        public final ForgeConfigSpec.DoubleValue dividerExponent;

        // --- Strength's Attribute Dividers ---
        public final ForgeConfigSpec.DoubleValue strengthAttackDamageDivider; // Renombrado para claridad (Divider)
        public final ForgeConfigSpec.DoubleValue strengthImpactDivider;
        public final ForgeConfigSpec.DoubleValue strengthArmorNegationDivider;
        public final ForgeConfigSpec.DoubleValue strengthSwimSpeedDivider;
        public final ForgeConfigSpec.DoubleValue strengthAttackSpeedDivider;
        public final ForgeConfigSpec.DoubleValue strengthAttackKnockbackDivider;

        // --- Dexterity's Attribute Dividers ---
        public final ForgeConfigSpec.DoubleValue dexterityMovementSpeedDivider;
        public final ForgeConfigSpec.DoubleValue dexterityArmorNegationDivider;
        public final ForgeConfigSpec.DoubleValue dexterityStaminaDivider;
        public final ForgeConfigSpec.DoubleValue dexterityStaminaRegenDivider;
        public final ForgeConfigSpec.DoubleValue dexteritySwimSpeedDivider;
        public final ForgeConfigSpec.DoubleValue dexterityAttackSpeedDivider;

        // --- Vitality's Attribute Dividers ---
        public final ForgeConfigSpec.DoubleValue vitalityMaxHealthDivider;
        public final ForgeConfigSpec.DoubleValue vitalityStaminaRegenDivider;
        public final ForgeConfigSpec.DoubleValue vitalityArmorDivider;
        public final ForgeConfigSpec.DoubleValue vitalityStaminaDivider;
        public final ForgeConfigSpec.DoubleValue vitalityWeightReductionDivider;

        // --- Constitution's Attribute Dividers ---
        public final ForgeConfigSpec.DoubleValue constitutionMaxHealthDivider;
        public final ForgeConfigSpec.DoubleValue constitutionArmorDivider;
        public final ForgeConfigSpec.DoubleValue constitutionStunArmorDivider;
        public final ForgeConfigSpec.DoubleValue constitutionImpactDivider;
        public final ForgeConfigSpec.DoubleValue constitutionKnockbackResistanceDivider;
        public final ForgeConfigSpec.DoubleValue constitutionWeightReductionDivider;
        public final ForgeConfigSpec.DoubleValue constitutionAttackKnockbackDivider;
        public final ForgeConfigSpec.DoubleValue constitutionArmorToughnessDivider;


        // Constructor de la clase Common donde se definen las opciones
        Common(ForgeConfigSpec.Builder builder) {
            builder.comment("Configuration for RPG Stats Mod").push("RPG Stats"); // Categoría principal

            // --- The Formula ---
            builder.comment("Settings for the point gain formula").push("Formula");
            pointsMultiplier = builder
                    .comment("Multiplier for points gained through actions (e.g., combat, dodging). Default: 0.3")
                    .defineInRange("pointsMultiplier", 0.3, 0.0, 100.0); // Define con rango (min=0, max=100)
            counterVariation = builder
                    .comment("Ascending variation of the counter that divides initial points per action. Default: 0.005")
                    .defineInRange("counterVariation", 0.005, 0.0, 1.0);
            dividerExponent = builder
                    .comment("Exponent applied to the point divider calculation. Default: 1.5")
                    .defineInRange("dividerExponent", 1.5, 0.1, 10.0);
            builder.pop(); // Fin de Formula

            // --- Strength ---
            builder.comment("Dividers for stats derived from Strength. Stat = Base + (Strength / Divider)").push("Strength");
            strengthAttackDamageDivider = builder.defineInRange("attackDamageDivider", 30.0, 1.0, Double.MAX_VALUE);
            strengthImpactDivider = builder.defineInRange("impactDivider", 120.0, 1.0, Double.MAX_VALUE);
            strengthArmorNegationDivider = builder.defineInRange("armorNegationDivider", 8.0, 1.0, Double.MAX_VALUE);
            strengthSwimSpeedDivider = builder.defineInRange("swimSpeedDivider", 2000.0, 1.0, Double.MAX_VALUE);
            strengthAttackSpeedDivider = builder.defineInRange("attackSpeedDivider", 500.0, 1.0, Double.MAX_VALUE);
            strengthAttackKnockbackDivider = builder.defineInRange("attackKnockbackDivider", 600.0, 1.0, Double.MAX_VALUE);
            builder.pop(); // Fin de Strength

            // --- Dexterity ---
            builder.comment("Dividers for stats derived from Dexterity. Stat = Base + (Dexterity / Divider), except Speed = Base * (1 + Dexterity/Divider)").push("Dexterity");
            dexterityMovementSpeedDivider = builder.defineInRange("movementSpeedDivider", 8000.0, 1.0, Double.MAX_VALUE); // Para multiplicación
            dexterityArmorNegationDivider = builder.defineInRange("armorNegationDivider", 8.0, 1.0, Double.MAX_VALUE);
            dexterityStaminaDivider = builder.defineInRange("staminaDivider", 7.0, 1.0, Double.MAX_VALUE);
            dexterityStaminaRegenDivider = builder.defineInRange("staminaRegenDivider", 500.0, 1.0, Double.MAX_VALUE);
            dexteritySwimSpeedDivider = builder.defineInRange("swimSpeedDivider", 600.0, 1.0, Double.MAX_VALUE);
            dexterityAttackSpeedDivider = builder.defineInRange("attackSpeedDivider", 1000.0, 1.0, Double.MAX_VALUE);
            builder.pop(); // Fin de Dexterity

            // --- Vitality ---
            builder.comment("Dividers for stats derived from Vitality. Stat = Base + (Vitality / Divider)").push("Vitality");
            vitalityMaxHealthDivider = builder.defineInRange("maxHealthDivider", 8.0, 1.0, Double.MAX_VALUE);
            vitalityStaminaRegenDivider = builder.defineInRange("staminaRegenDivider", 500.0, 1.0, Double.MAX_VALUE);
            vitalityArmorDivider = builder.defineInRange("armorDivider", 35.0, 1.0, Double.MAX_VALUE);
            vitalityStaminaDivider = builder.defineInRange("staminaDivider", 15.0, 1.0, Double.MAX_VALUE);
            vitalityWeightReductionDivider = builder.defineInRange("weightReductionDivider", 800.0, 1.0, Double.MAX_VALUE);
            builder.pop(); // Fin de Vitality

            // --- Constitution ---
            builder.comment("Dividers for stats derived from Constitution. Stat = Base + (Constitution / Divider)").push("Constitution");
            constitutionMaxHealthDivider = builder.defineInRange("maxHealthDivider", 40.0, 1.0, Double.MAX_VALUE);
            constitutionArmorDivider = builder.defineInRange("armorDivider", 10.0, 1.0, Double.MAX_VALUE);
            constitutionStunArmorDivider = builder.defineInRange("stunArmorDivider", 25.0, 1.0, Double.MAX_VALUE);
            constitutionImpactDivider = builder.defineInRange("impactDivider", 120.0, 1.0, Double.MAX_VALUE);
            constitutionKnockbackResistanceDivider = builder.defineInRange("knockbackResistanceDivider", 100.0, 1.0, Double.MAX_VALUE);
            constitutionWeightReductionDivider = builder.defineInRange("weightReductionDivider", 800.0, 1.0, Double.MAX_VALUE);
            constitutionAttackKnockbackDivider = builder.defineInRange("attackKnockbackDivider", 300.0, 1.0, Double.MAX_VALUE);
            constitutionArmorToughnessDivider = builder.defineInRange("armorToughnessDivider", 75.0, 1.0, Double.MAX_VALUE);
            builder.pop(); // Fin de Constitution

            builder.pop(); // Fin de RPG Stats
        }
    }
}