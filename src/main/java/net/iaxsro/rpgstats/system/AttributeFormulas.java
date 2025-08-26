package net.iaxsro.rpgstats.system;

import net.iaxsro.rpgstats.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AttributeFormulas {

    private static final Logger LOGGER = LoggerFactory.getLogger(AttributeFormulas.class);

    // Constructor privado para prevenir instanciación de clase utilitaria
    private AttributeFormulas() {
    }

    /**
     * Calcula la cantidad de puntos de fuerza (temporales) a añadir basado en el daño infligido.
     * La fórmula utiliza un sistema de rendimientos decrecientes basado en 'iteraciones'.
     *
     * @param hitDamage          El daño efectivo infligido al objetivo.
     * @param currentIterations  Un contador que representa el 'progreso' o 'experiencia' acumulada
     *                           en acciones similares (afecta los rendimientos decrecientes).
     * @param targetMaxPotential Un valor que representa el 'máximo potencial' del objetivo,
     *                           usado para calcular el porcentaje de daño (ej.: vidaMaxima * (1 + armadura/100)).
     * @return La cantidad de puntos de fuerza a añadir.
     */
    public static double calculateStrengthGainOnHit(double hitDamage, double currentIterations, double targetMaxPotential) {
        // Asegurar que el potencial máximo no sea cero para evitar división por cero
        if (targetMaxPotential <= 1e-6) {
            LOGGER.warn("targetMaxPotential es cero o negativo ({}) en calculateStrengthGainOnHit. Devolviendo 0.", targetMaxPotential);
            return 0.0;
        }

        // Porcentaje del daño infligido respecto al potencial máximo del objetivo
        double damagePercentage = Math.max(0, Math.min(1, hitDamage / targetMaxPotential)); // Clamp entre 0 y 1

        // Divisor que aumenta con las iteraciones (rendimientos decrecientes)
        // Usa los valores de la configuración
        double counterVariation = ModConfig.COMMON.counterVariation.get();
        double dividerExponent = ModConfig.COMMON.dividerExponent.get();
        // Asegurar que la base del exponente no sea negativa
        double dividerBase = 1.0 + (counterVariation * Math.max(0, currentIterations));
        double pointDivider = Math.pow(dividerBase, Math.max(0.1, dividerExponent)); // Asegurar exponente > 0

        // Multiplicador base de puntos, ajustado por el potencial del objetivo
        // Usa el valor de la configuración
        double pointsMultiplierConfig = ModConfig.COMMON.pointsMultiplier.get();
        // El 0.01 original podría estar destinado a convertir un porcentaje, o ser parte del balance.
        // Si targetMaxPotential ya incluye un factor grande, este multiplicador debe ser pequeño.
        double pointBaseMultiplier = targetMaxPotential * pointsMultiplierConfig * 0.01;

        // Puntos finales = (MultiplicadorBase / Divisor) * PorcentajeDaño
        double pointsToAdd = (pointBaseMultiplier / pointDivider) * damagePercentage;

        // Asegurar que no se añadan puntos negativos
        pointsToAdd = Math.max(0, pointsToAdd);

        LOGGER.trace("Calculando StrengthGain: Damage={}, Iterations={}, MaxPotential={}, Percentage={}, Divider={}, Multiplier={}, Result={}",
                hitDamage, currentIterations, targetMaxPotential, damagePercentage, pointDivider, pointBaseMultiplier, pointsToAdd);

        return pointsToAdd;
    }

}