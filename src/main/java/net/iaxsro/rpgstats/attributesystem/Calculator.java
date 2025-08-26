package net.iaxsro.rpgstats.attributesystem;

import net.iaxsro.rpgstats.RpgStatsMod;
import net.iaxsro.rpgstats.capabilities.util.CapabilitiesAccessor;
import net.iaxsro.rpgstats.config.ModConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;

import java.util.concurrent.CompletableFuture;


public class Calculator {
    /**
     * Calculates the points to be granted based on several factors.
     *
     * @param pointsAmount          The base amount of points for the attribute.
     * @param iterations            The number of times points have been granted for this attribute.
     * @param healthPercentageTaken The percentage of health lost/taken in the event.
     * @return The calculated points to be granted.
     */
    public static double CalculateGrantedPoints(double pointsAmount, double iterations, double healthPercentageTaken) {
        double counterVariation = ModConfig.COMMON.counterVariation.get();
        double pointsAmountMultiplier = ModConfig.COMMON.pointsMultiplier.get();
        double dividerExponent = ModConfig.COMMON.dividerExponent.get();

        return ((pointsAmount * pointsAmountMultiplier) / Math.pow((1 + counterVariation * iterations), dividerExponent)) * healthPercentageTaken;

    }

    /**
     * Fetches player stats and calculates granted points for a specific attribute.
     * This is an example showing how to get and use the 'strengthIterations'.
     * You would adapt this for other attributes as needed.
     *
     * @param entity The entity whose stats are being calculated.
     */
    public static void calculateAndApplyStrengthPoints(LivingEntity entity, double pointsAmount, double healthPercentageTaken) {
        // 1. Get the current iteration count for the desired attribute (e.g., Strength)
        double currentIterations = CapabilitiesAccessor.strengthIterations.get(entity);
        double currentStrengthPoints = CapabilitiesAccessor.strengthPoints.get(entity);

        // 2. Call the calculation method
        double grantedPoints = CalculateGrantedPoints(pointsAmount, currentIterations, healthPercentageTaken);

        // 3. Add the new points to the entity's stats
        CapabilitiesAccessor.strengthPoints.set(entity, currentStrengthPoints + grantedPoints);

        // 4. VERY IMPORTANT: Increment the iteration counter for that attribute
        CapabilitiesAccessor.strengthIterations.set(entity, currentIterations + healthPercentageTaken);
    }

    public void CalculateGrantedPoints(LivingEntity entity, double pointsAmount, double iterations) {
        double counterVariation = ModConfig.COMMON.counterVariation.get();
        double pointsAmountMultiplier = ModConfig.COMMON.pointsMultiplier.get();
        double dividerExponent = ModConfig.COMMON.dividerExponent.get();


        CompletableFuture<Double> futureSum = CompletableFuture.supplyAsync(() -> {
            double currentIterations = CapabilitiesAccessor.strengthIterations.get(entity);
            double totalSum = 0.0;
            for (int x = (int) currentIterations;
                 x <= (currentIterations + pointsAmount);
                 x++) {
                double denominator = Math.pow(1 + (counterVariation * x), dividerExponent);
                totalSum += pointsAmountMultiplier / denominator;
            }

            return totalSum; // Retorna el resultado.
        }, RpgStatsMod.EXECUTOR);
        futureSum.thenAcceptAsync(finalSum -> {

            // Este código se ejecuta de vuelta en el hilo principal.
            // Aquí es seguro interactuar con el jugador, el mundo, etc.
            entity.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "Cálculo completado. La suma es: " + finalSum
            ));
        }, runnable -> {
            // En Forge, necesitamos asegurarnos de que el código se ejecute en el hilo correcto.
            // runnables del tipo CompletableFuture ya vienen preparados para el hilo del juego.
            // Sin embargo, para mayor seguridad, puedes usar el scheduler de Minecraft.
            MinecraftServer server = entity.getServer();
            if (server != null) {
                server.execute(runnable);
            }
        });

    }
}
