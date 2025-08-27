package net.iaxsro.rpgstats.command;

import com.mojang.brigadier.CommandDispatcher;
import net.iaxsro.rpgstats.RpgStatsMod;
import net.iaxsro.rpgstats.capabilities.IPlayerStats;
import net.iaxsro.rpgstats.capabilities.PlayerStats;
import net.iaxsro.rpgstats.network.ClientboundSyncPlayerStatsPacket;
import net.iaxsro.rpgstats.network.PacketHandler;
import net.iaxsro.rpgstats.registry.AttributeRegistry;
import net.iaxsro.rpgstats.system.AttributeCalculator;
import net.iaxsro.rpgstats.system.LevelingManager;
import net.iaxsro.rpgstats.util.ChatUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

@Mod.EventBusSubscriber(modid = RpgStatsMod.MOD_ID)
public class StatsCommand {

    private static final Logger LOGGER = RpgStatsMod.LOGGER;

    @SubscribeEvent
    public static void registerCommands(final RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("rpgstats")
                        .requires(source -> source.hasPermission(2))

                        // --- Subcomando: levelup ---
                        .then(Commands.literal("levelup")
                                .executes(context -> executeLevelUp(
                                        context.getSource(),
                                        Collections.singleton(context.getSource().getPlayerOrException())
                                ))
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(context -> executeLevelUp(
                                                context.getSource(),
                                                EntityArgument.getPlayers(context, "targets")
                                        ))
                                )
                        ) // Fin levelup

                        // --- Subcomando: show ---
                        .then(Commands.literal("show")
                                .executes(context -> executeShowStats(
                                        context.getSource(),
                                        context.getSource().getPlayerOrException()
                                ))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(context -> executeShowStats(
                                                context.getSource(),
                                                EntityArgument.getPlayer(context, "target")
                                        ))
                                )
                        ) // Fin show

                        // --- Subcomando: resetpoints ---
                        .then(Commands.literal("resetpoints")
                                .executes(context -> executeResetTempPoints(
                                        context.getSource(),
                                        Collections.singleton(context.getSource().getPlayerOrException())
                                ))
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(context -> executeResetTempPoints(
                                                context.getSource(),
                                                EntityArgument.getPlayers(context, "targets")
                                        ))
                                )
                        ) // Fin resetpoints

        ); // Fin register
        LOGGER.info("Comando /rpgstats registrado.");
    }

    // --- Métodos de Ejecución Corregidos ---

    private static int executeLevelUp(CommandSourceStack source, Collection<ServerPlayer> targets) {
        // Usar AtomicInteger para el contador dentro de lambdas
        if (targets == null || targets.isEmpty()) {
            source.sendFailure(Component.translatable("command.rpgstats.levelup.no_valid_players"));
            return 0;
        }

        // Usar AtomicInteger para el contador dentro de lambdas
        AtomicInteger successCounter = new AtomicInteger(0);
        for (ServerPlayer target : targets) {
            try {
                // Validar que el jugador no sea null
                if (target == null) {
                    LOGGER.warn("Se encontró un jugador null en la colección de targets");
                    continue;
                }

                // Llamar al LevelingManager
                LevelingManager.processLevelUp(target);
                successCounter.incrementAndGet();

            } catch (Exception e) {
                LOGGER.error("Error al intentar subir de nivel a {}: {}",
                        target != null ? target.getName().getString() : "null", e.getMessage(), e);
                source.sendFailure(Component.translatable("command.rpgstats.levelup.player_error",
                        target != null ? target.getName().getString() : "jugador desconocido"));
            }
        }

        // Leer el valor final del AtomicInteger para el mensaje
        int finalCount = successCounter.get();
        if (finalCount > 0) {
            source.sendSuccess(() -> Component.translatable("command.rpgstats.levelup.success", finalCount), true);
        } else {
            source.sendFailure(Component.translatable("command.rpgstats.levelup.failure"));
        }
        return finalCount;
    }

    private static int executeShowStats(CommandSourceStack source, ServerPlayer target) {
        // Obtener el LazyOptional de la capacidad
        if (target == null) {
            source.sendFailure(Component.translatable("command.rpgstats.levelup.player_not_found"));
            return 0;
        }

        // Obtener el LazyOptional de la capacidad
        LazyOptional<IPlayerStats> statsOptional = target.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY);

        // Comprobar si la capacidad está presente
        if (statsOptional.isPresent()) {
            // Usar .ifPresent para obtener la instancia segura de IPlayerStats
            statsOptional.ifPresent(stats -> {
                try {
                    // Calcular bonificaciones
                    AttributeCalculator.CalculatedBonuses bonuses = AttributeCalculator.calculateBonuses(target);

                    // Usar ChatUtil si está disponible (recomendado)
                    source.sendSuccess(() -> ChatUtil.formatHeader("Estadísticas RPG para " + target.getName().getString()), false);
                    source.sendSuccess(() -> ChatUtil.formatKeyValue(" Nivel", stats.getLevel(), ChatFormatting.AQUA), false);
                    source.sendSuccess(() -> ChatUtil.formatKeyValue(" Nickname", stats.getNickname() != null ? stats.getNickname() : "N/A", ChatFormatting.YELLOW), false);

                    source.sendSuccess(() -> Component.translatable("command.rpgstats.show.temp_points_header").withStyle(ChatFormatting.GRAY), false);
                    source.sendSuccess(() -> Component.literal(String.format("  STR: %.1f | DEX: %.1f | VIT: %.1f | CON: %.1f | INT: %.1f",
                                    stats.getStrengthPoints(), stats.getDexterityPoints(), stats.getVitalityPoints(),
                                    stats.getConstitutionPoints(), stats.getIntelligencePoints()))
                            .withStyle(ChatFormatting.WHITE), false);

                    source.sendSuccess(() -> Component.translatable("command.rpgstats.show.base_attributes_header").withStyle(ChatFormatting.GRAY), false);
                    source.sendSuccess(() -> Component.literal(String.format("  STR: %.1f | DEX: %.1f | VIT: %.1f | CON: %.1f | INT: %.1f",
                                    target.getAttributeBaseValue(AttributeRegistry.STRENGTH.get()),
                                    target.getAttributeBaseValue(AttributeRegistry.DEXTERITY.get()),
                                    target.getAttributeBaseValue(AttributeRegistry.VITALITY.get()),
                                    target.getAttributeBaseValue(AttributeRegistry.CONSTITUTION.get()),
                                    target.getAttributeBaseValue(AttributeRegistry.INTELLIGENCE.get())))
                            .withStyle(ChatFormatting.LIGHT_PURPLE), false);

                    source.sendSuccess(() -> Component.translatable("command.rpgstats.show.bonuses_header").withStyle(ChatFormatting.GRAY), false);
                    source.sendSuccess(() -> Component.literal(String.format("  +VidaMax: %.1f | +Armadura: %.1f | +DañoAtaque: %.1f",
                                    bonuses.totalMaxHealthAddition, bonuses.totalArmorAddition, bonuses.attackDamageAddition))
                            .withStyle(ChatFormatting.GREEN), false);
                    source.sendSuccess(() -> Component.literal(String.format("  +VelMov*: %.1f%% | +VelAtaque: %.2f | +ResisKB: %.1f",
                                    bonuses.movementSpeedMultiplier * 100, bonuses.totalAttackSpeedAddition, bonuses.knockbackResistanceAddition))
                            .withStyle(ChatFormatting.GREEN), false);
                    source.sendSuccess(() -> Component.translatable("command.rpgstats.show.sprint_note").withStyle(ChatFormatting.DARK_GREEN), false);

                } catch (Exception e) {
                    LOGGER.error("Error al mostrar estadísticas para {}: {}", target.getName().getString(), e.getMessage(), e);
                    source.sendFailure(Component.translatable("command.rpgstats.show.stats_error"));
                }
            });
        } else {
            // Manejar el caso donde la capacidad no está presente
            source.sendFailure(Component.translatable("command.rpgstats.show.no_capability", target.getName().getString()));
            return 0;
        }

        return 1; // Indica éxito
    }


    private static int executeResetTempPoints(CommandSourceStack source, Collection<ServerPlayer> targets) {
        // Usar AtomicInteger para el contador
        if (targets == null || targets.isEmpty()) {
            source.sendFailure(Component.translatable("command.rpgstats.resetpoints.no_valid_players"));
            return 0;
        }

        // Usar AtomicInteger para el contador
        AtomicInteger successCounter = new AtomicInteger(0);
        for (ServerPlayer target : targets) {
            try {
                // Obtener LazyOptional y usar ifPresent
                target.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY).ifPresent(stats -> {
                    stats.setStrengthPoints(0);
                    stats.setDexterityPoints(0);
                    stats.setVitalityPoints(0);
                    stats.setConstitutionPoints(0);
                    stats.setIntelligencePoints(0);
                    //stats.setIterations(0);

                    // Sincronizar cambios
                    try {
                        PacketHandler.sendToPlayer(target, new ClientboundSyncPlayerStatsPacket(stats.writeNBT()));
                        successCounter.incrementAndGet();
                    } catch (Exception e) {
                        LOGGER.error("Error al sincronizar datos para {}: {}", target.getName().getString(), e.getMessage(), e);
                    }
                });
            } catch (Exception e) {
                LOGGER.error("Error al resetear puntos para {}: {}", target.getName().getString(), e.getMessage(), e);
            }
        }

        int finalCount = successCounter.get();
        if (finalCount > 0) {
            source.sendSuccess(() -> Component.translatable("command.rpgstats.resetpoints.success", finalCount), true);
        } else {
            source.sendFailure(Component.translatable("command.rpgstats.resetpoints.failure"));
        }
        return finalCount;
    }

}