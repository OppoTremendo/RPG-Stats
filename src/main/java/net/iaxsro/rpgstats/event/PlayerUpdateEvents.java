package net.iaxsro.rpgstats.event;

import net.iaxsro.rpgstats.RpgStatsMod;
import net.iaxsro.rpgstats.capabilities.PlayerStats;
import net.iaxsro.rpgstats.config.ModConfig;
import net.iaxsro.rpgstats.network.ClientboundSyncPlayerStatsPacket;
import net.iaxsro.rpgstats.network.PacketHandler;
import net.iaxsro.rpgstats.registry.AttributeRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.UUID;

// Escucha eventos en el bus de FORGE (implícito)
@Mod.EventBusSubscriber(modid = RpgStatsMod.MOD_ID)
public class PlayerUpdateEvents {

    private static final Logger LOGGER = RpgStatsMod.LOGGER;

    // --- Lógica del Nickname al Entrar por Primera Vez ---
    // UUID constante para el modificador de sprint (debe ser único)
    private static final UUID SPRINT_SPEED_MODIFIER_UUID = UUID.fromString("ce5eb8b2-8e32-4de0-837c-aeb0173b3256"); // Mantenemos el UUID original

    // --- Lógica del Modificador de Velocidad al Esprintar ---

    @SubscribeEvent
    public static void onEntityJoinLevel(final EntityJoinLevelEvent event) {
        // Ejecutar solo en el servidor lógico y para jugadores reales
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof Player player && !(player instanceof FakePlayer)) {
            player.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY).ifPresent(stats -> {
                if (stats.isFirstTimeJoining()) {
                    String playerName = player.getGameProfile().getName(); // Usar GameProfile para el nombre
                    LOGGER.info("Primera entrada detectada para {}. Estableciendo nickname.", playerName);
                    stats.setNickname(playerName);
                    stats.setFirstTimeJoining(false);

                    // Sincronizar el cambio (nickname y firstTimeJoining) al cliente
                    if (player instanceof ServerPlayer serverPlayer) {
                        PacketHandler.sendToPlayer(serverPlayer, new ClientboundSyncPlayerStatsPacket(stats.writeNBT()));
                        LOGGER.debug("Nickname y estado firstJoin sincronizados para {}", playerName);
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(final TickEvent.PlayerTickEvent event) {
        // Ejecutar solo al final del tick y en el servidor lógico
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide()) {
            Player player = event.player;
            Level level = player.level();
            AttributeInstance speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttribute == null) {
                // No debería pasar para un jugador, pero es una comprobación segura
                return;
            }

            // Comprobar si el jugador está esprintando válidamente (no agachado, no en el aire)
            boolean isSprintingEffectively = player.isSprinting() && !player.isShiftKeyDown() && !level.isEmptyBlock(player.blockPosition().below());

            boolean hasModifier = speedAttribute.getModifier(SPRINT_SPEED_MODIFIER_UUID) != null;

            if (isSprintingEffectively) {
                // Aplicar modificador si está esprintando y no lo tiene ya
                if (!hasModifier) {
                    player.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY).ifPresent(stats -> {
                        // Obtener la destreza BASE del jugador. Los modificadores no deben basarse en valores ya modificados.
                        // Necesitamos obtener el atributo de DEXTERITY registrado
                        AttributeInstance dexterityAttribute = player.getAttribute(AttributeRegistry.DEXTERITY.get());
                        if (dexterityAttribute != null) {
                            double dexterityValue = dexterityAttribute.getBaseValue(); // Usar valor base
                            double divider = ModConfig.COMMON.dexterityMovementSpeedDivider.get();
                            if (divider > 0) {
                                // La fórmula según el comentario era: Base * (1 + Dex/Divider)
                                // El modificador MULTIPLY_BASE aplica (Value * (1 + ModifierAmount))
                                // Por lo tanto, ModifierAmount debe ser Dex/Divider
                                double modifierAmount = dexterityValue / divider;

                                AttributeModifier speedModifier = new AttributeModifier(
                                        SPRINT_SPEED_MODIFIER_UUID,
                                        "SprintDexterityBonus", // Nombre descriptivo
                                        modifierAmount,
                                        AttributeModifier.Operation.MULTIPLY_BASE // Operación multiplicativa
                                );
                                speedAttribute.addTransientModifier(speedModifier); // Modificador transitorio
                                LOGGER.trace("Modificador de sprint APLICADO a {}", player.getName().getString());
                            }
                        } else {
                            LOGGER.warn("Atributo Dexterity no encontrado para {}", player.getName().getString());
                        }
                    });
                }
            } else {
                // Quitar modificador si no está esprintando y sí lo tiene
                if (hasModifier) {
                    speedAttribute.removeModifier(SPRINT_SPEED_MODIFIER_UUID);
                    LOGGER.trace("Modificador de sprint REMOVIDO de {}", player.getName().getString());
                }
            }
        }

    }
}