package net.iaxsro.rpgstats.client.command;

import com.mojang.brigadier.CommandDispatcher;
import net.iaxsro.rpgstats.RpgStatsMod;
import net.iaxsro.rpgstats.client.event.ClientAttributeEvents;
import net.iaxsro.rpgstats.client.gui.AttributePointsOverlay;
import net.iaxsro.rpgstats.client.gui.AttributeValuesOverlay;
import net.iaxsro.rpgstats.client.gui.ModifiedAttributesOverlay;
import net.iaxsro.rpgstats.client.gui.SimpleTestOverlay;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Comandos del cliente para controlar las GUIs de los overlays.
 * Incluye comandos para puntos de atributos, valores de atributos, atributos modificados y overlay de test.
 */
@Mod.EventBusSubscriber(modid = RpgStatsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientCommands {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientCommands.class);

    @SubscribeEvent
    public static void registerClientCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("rpgstats-client")
                        // Comandos para overlay de puntos
                        .then(Commands.literal("points")
                                .then(Commands.literal("toggle")
                                        .executes(context -> {
                                            AttributePointsOverlay.toggleVisibility();
                                            boolean isVisible = AttributePointsOverlay.isVisible();
                                            context.getSource().sendSuccess(
                                                    () -> Component.translatable("command.rpgstats-client.points.toggle",
                                                            Component.translatable(isVisible ? "command.rpgstats-client.status.activated" : "command.rpgstats-client.status.deactivated")),
                                                    false
                                            );
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("show")
                                        .executes(context -> {
                                            AttributePointsOverlay.setVisible(true);
                                            context.getSource().sendSuccess(
                                                    () -> Component.translatable("command.rpgstats-client.points.show"),
                                                    false
                                            );
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("hide")
                                        .executes(context -> {
                                            AttributePointsOverlay.setVisible(false);
                                            context.getSource().sendSuccess(
                                                    () -> Component.translatable("command.rpgstats-client.points.hide"),
                                                    false
                                            );
                                            return 1;
                                        })
                                )
                        )
                        // Comandos para overlay de valores
                        .then(Commands.literal("values")
                                .then(Commands.literal("toggle")
                                        .executes(context -> {
                                            AttributeValuesOverlay.toggleVisibility();
                                            boolean isVisible = AttributeValuesOverlay.isVisible();
                                            context.getSource().sendSuccess(
                                                    () -> Component.translatable("command.rpgstats-client.values.toggle",
                                                            Component.translatable(isVisible ? "command.rpgstats-client.status.activated" : "command.rpgstats-client.status.deactivated")),
                                                    false
                                            );
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("show")
                                        .executes(context -> {
                                            AttributeValuesOverlay.setVisible(true);
                                            context.getSource().sendSuccess(
                                                    () -> Component.translatable("command.rpgstats-client.values.show"),
                                                    false
                                            );
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("hide")
                                        .executes(context -> {
                                            AttributeValuesOverlay.setVisible(false);
                                            context.getSource().sendSuccess(
                                                    () -> Component.translatable("command.rpgstats-client.values.hide"),
                                                    false
                                            );
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("refresh")
                                        .executes(context -> {
                                            AttributeValuesOverlay.forceUpdate();
                                            ClientAttributeEvents.resetCache();
                                            context.getSource().sendSuccess(
                                                    () -> Component.translatable("command.rpgstats-client.values.refresh"),
                                                    false
                                            );
                                            return 1;
                                        })
                                )
                        )
                        // Comandos para overlay de atributos modificados
                        .then(Commands.literal("modified")
                                .then(Commands.literal("toggle")
                                        .executes(context -> {
                                            boolean currentVisible = ModifiedAttributesOverlay.isVisible();
                                            ModifiedAttributesOverlay.setVisible(!currentVisible);
                                            boolean isVisible = ModifiedAttributesOverlay.isVisible();
                                            context.getSource().sendSuccess(
                                                    () -> Component.translatable("command.rpgstats-client.modified.toggle",
                                                            Component.translatable(isVisible ? "command.rpgstats-client.status.activated" : "command.rpgstats-client.status.deactivated")),
                                                    false
                                            );
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("show")
                                        .executes(context -> {
                                            ModifiedAttributesOverlay.setVisible(true);
                                            context.getSource().sendSuccess(
                                                    () -> Component.translatable("command.rpgstats-client.modified.show"),
                                                    false
                                            );
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("hide")
                                        .executes(context -> {
                                            ModifiedAttributesOverlay.setVisible(false);
                                            context.getSource().sendSuccess(
                                                    () -> Component.translatable("command.rpgstats-client.modified.hide"),
                                                    false
                                            );
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("refresh")
                                        .executes(context -> {
                                            ModifiedAttributesOverlay.forceUpdate();
                                            context.getSource().sendSuccess(
                                                    () -> Component.translatable("command.rpgstats-client.modified.refresh"),
                                                    false
                                            );
                                            return 1;
                                        })
                                )
                        )
                        // Comandos para overlay de test
                        .then(Commands.literal("test")
                                .then(Commands.literal("toggle")
                                        .executes(context -> {
                                            SimpleTestOverlay.toggleVisibility();
                                            boolean isVisible = SimpleTestOverlay.isVisible();
                                            context.getSource().sendSuccess(
                                                    () -> Component.translatable("command.rpgstats-client.test.toggle",
                                                            Component.translatable(isVisible ? "command.rpgstats-client.status.activated" : "command.rpgstats-client.status.deactivated")),
                                                    false
                                            );
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("show")
                                        .executes(context -> {
                                            SimpleTestOverlay.setVisible(true);
                                            context.getSource().sendSuccess(
                                                    () -> Component.translatable("command.rpgstats-client.test.show"),
                                                    false
                                            );
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("hide")
                                        .executes(context -> {
                                            SimpleTestOverlay.setVisible(false);
                                            context.getSource().sendSuccess(
                                                    () -> Component.translatable("command.rpgstats-client.test.hide"),
                                                    false
                                            );
                                            return 1;
                                        })
                                )
                        )
                        // Comandos para todos los overlays
                        .then(Commands.literal("all")
                                .then(Commands.literal("toggle")
                                        .executes(context -> {
                                            AttributePointsOverlay.toggleVisibility();
                                            AttributeValuesOverlay.toggleVisibility();
                                            boolean currentModifiedVisible = ModifiedAttributesOverlay.isVisible();
                                            ModifiedAttributesOverlay.setVisible(!currentModifiedVisible);

                                            boolean pointsVisible = AttributePointsOverlay.isVisible();
                                            boolean valuesVisible = AttributeValuesOverlay.isVisible();
                                            boolean modifiedVisible = ModifiedAttributesOverlay.isVisible();

                                            String statusKey = (pointsVisible && valuesVisible && modifiedVisible) ? "command.rpgstats-client.all.toggle.activated" :
                                                    (!pointsVisible && !valuesVisible && !modifiedVisible) ? "command.rpgstats-client.all.toggle.deactivated" :
                                                            "command.rpgstats-client.all.toggle.mixed";

                                            context.getSource().sendSuccess(
                                                    () -> Component.translatable(statusKey),
                                                    false
                                            );
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("show")
                                        .executes(context -> {
                                            AttributePointsOverlay.setVisible(true);
                                            AttributeValuesOverlay.setVisible(true);
                                            ModifiedAttributesOverlay.setVisible(true);
                                            context.getSource().sendSuccess(
                                                    () -> Component.translatable("command.rpgstats-client.all.show"),
                                                    false
                                            );
                                            return 1;
                                        })
                                )
                                .then(Commands.literal("hide")
                                        .executes(context -> {
                                            AttributePointsOverlay.setVisible(false);
                                            AttributeValuesOverlay.setVisible(false);
                                            ModifiedAttributesOverlay.setVisible(false);
                                            context.getSource().sendSuccess(
                                                    () -> Component.translatable("command.rpgstats-client.all.hide"),
                                                    false
                                            );
                                            return 1;
                                        })
                                )
                        )
                        // Comando de estado
                        .then(Commands.literal("status")
                                .executes(context -> {
                                    boolean pointsVisible = AttributePointsOverlay.isVisible();
                                    boolean valuesVisible = AttributeValuesOverlay.isVisible();
                                    boolean modifiedVisible = ModifiedAttributesOverlay.isVisible();
                                    boolean testVisible = SimpleTestOverlay.isVisible();

                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.status.header"), false
                                    );
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.status.points",
                                                    Component.translatable(pointsVisible ? "command.rpgstats-client.status.activated" : "command.rpgstats-client.status.deactivated")), false
                                    );
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.status.values",
                                                    Component.translatable(valuesVisible ? "command.rpgstats-client.status.activated" : "command.rpgstats-client.status.deactivated")), false
                                    );
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.status.modified",
                                                    Component.translatable(modifiedVisible ? "command.rpgstats-client.status.activated" : "command.rpgstats-client.status.deactivated")), false
                                    );
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.status.test",
                                                    Component.translatable(testVisible ? "command.rpgstats-client.status.activated" : "command.rpgstats-client.status.deactivated")), false
                                    );

                                    return 1;
                                })
                        )
                        // Comando de debug
                        .then(Commands.literal("debug")
                                .executes(context -> {
                                    boolean pointsVisible = AttributePointsOverlay.isVisible();
                                    boolean valuesVisible = AttributeValuesOverlay.isVisible();
                                    boolean modifiedVisible = ModifiedAttributesOverlay.isVisible();
                                    boolean testVisible = SimpleTestOverlay.isVisible();

                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.debug.header"), false
                                    );
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.debug.state"), false
                                    );
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.debug.points",
                                                    Component.translatable(pointsVisible ? "command.rpgstats-client.status.activated" : "command.rpgstats-client.status.deactivated")), false
                                    );
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.debug.values",
                                                    Component.translatable(valuesVisible ? "command.rpgstats-client.status.activated" : "command.rpgstats-client.status.deactivated")), false
                                    );
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.debug.modified",
                                                    Component.translatable(modifiedVisible ? "command.rpgstats-client.status.activated" : "command.rpgstats-client.status.deactivated")), false
                                    );
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.debug.test",
                                                    Component.translatable(testVisible ? "command.rpgstats-client.status.activated" : "command.rpgstats-client.status.deactivated")), false
                                    );

                                    // Forzar actualización y mostrar overlays principales
                                    AttributePointsOverlay.setVisible(true);
                                    AttributeValuesOverlay.setVisible(true);
                                    ModifiedAttributesOverlay.setVisible(true);
                                    AttributeValuesOverlay.forceUpdate();
                                    ModifiedAttributesOverlay.forceUpdate();
                                    ClientAttributeEvents.resetCache();

                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.debug.visibility_forced"), false
                                    );
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.debug.test_hide"), false
                                    );

                                    return 1;
                                })
                        )
                        // Comando de ayuda
                        .then(Commands.literal("help")
                                .executes(context -> {
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.help.header"), false
                                    );
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.help.points"), false
                                    );
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.help.points.toggle"), false
                                    );
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.help.points.show"), false
                                    );
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.help.points.hide"), false
                                    );
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.help.values"), false
                                    );
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.help.values.toggle"), false
                                    );
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.help.values.show"), false
                                    );
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.help.values.hide"), false
                                    );
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.help.values.refresh"), false
                                    );
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.help.modified"), false
                                    );
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.help.modified.toggle"), false
                                    );
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.help.modified.show"), false
                                    );
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.help.modified.hide"), false
                                    );
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.help.modified.refresh"), false
                                    );
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.help.test"), false
                                    );
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.help.test.toggle"), false
                                    );
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.help.test.show"), false
                                    );
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.help.test.hide"), false
                                    );
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.help.all"), false
                                    );
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.help.all.toggle"), false
                                    );
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.help.all.show"), false
                                    );
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.help.all.hide"), false
                                    );
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.help.status"), false
                                    );
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.help.debug"), false
                                    );
                                    context.getSource().sendSuccess(
                                            () -> Component.translatable("command.rpgstats-client.help.help"), false
                                    );

                                    return 1;
                                })
                        )
        );

        LOGGER.info("Comandos del cliente registrados exitosamente");
    }
}