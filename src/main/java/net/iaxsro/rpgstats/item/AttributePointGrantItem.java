package net.iaxsro.rpgstats.item;

import net.iaxsro.rpgstats.capabilities.PlayerStats;
import net.iaxsro.rpgstats.client.gui.AttributeValuesOverlay;
import net.iaxsro.rpgstats.client.gui.ModifiedAttributesOverlay;
import net.iaxsro.rpgstats.network.ClientboundSyncPlayerStatsPacket;
import net.iaxsro.rpgstats.network.PacketHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;

public class AttributePointGrantItem extends Item {

    private static final Logger LOGGER = LoggerFactory.getLogger(AttributePointGrantItem.class);

    // Puntos a otorgar por cada uso
    private static final double POINTS_TO_GRANT = 1.0;

    public AttributePointGrantItem(Properties properties) {
        // Llama al constructor de la clase Item con las propiedades definidas en ItemRegistry
        super(properties);
        LOGGER.info("AttributePointGrantItem creado con {} puntos por uso", POINTS_TO_GRANT);
    }

    /**
     * Método estático para forzar actualización de overlays del lado del cliente
     * (llamado cuando se recibe el paquete de sincronización)
     */
    @OnlyIn(Dist.CLIENT)
    public static void updateOverlays() {
        try {
            // AttributePointsOverlay se actualiza automáticamente cada segundo, no necesita forceUpdate
            AttributeValuesOverlay.forceUpdate();
            ModifiedAttributesOverlay.forceUpdate();
            LOGGER.debug("Overlays updated after attribute point grant");
        } catch (Exception e) {
            LOGGER.error("Error updating overlays after attribute point grant: {}", e.getMessage(), e);
        }
    }

    /**
     * Hace que el ítem brille como si estuviera encantado.
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isFoil(ItemStack pStack) {
        return true; // Siempre brilla
    }

    /**
     * Se ejecuta cuando el jugador hace clic derecho con el ítem.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemStack = pPlayer.getItemInHand(pUsedHand);

        // Ejecutar lógica solo en el lado del servidor
        if (!pLevel.isClientSide()) {
            // Obtener la capacidad del jugador
            pPlayer.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY).ifPresent(stats -> {
                // Añadir puntos a los atributos temporales
                stats.addStrengthPoints(POINTS_TO_GRANT);
                stats.addDexterityPoints(POINTS_TO_GRANT);
                stats.addVitalityPoints(POINTS_TO_GRANT);
                stats.addConstitutionPoints(POINTS_TO_GRANT);
                stats.addIntelligencePoints(POINTS_TO_GRANT);

                LOGGER.info("Player {} used Attribute Point Orb, gained {} points in each attribute",
                        pPlayer.getName().getString(), POINTS_TO_GRANT);

                // Reproducir sonido de feedback
                pLevel.playSound(
                        null, // null para sonido global en el servidor
                        pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP, // Sonido de experiencia
                        SoundSource.PLAYERS, // Categoría del sonido
                        0.5F, // Volumen
                        1.2F + pLevel.random.nextFloat() * 0.3F // Pitch variable más alegre
                );

                // Sincronizar inmediatamente al cliente para actualizar overlays
                if (pPlayer instanceof ServerPlayer serverPlayer) {
                    PacketHandler.sendToPlayer(serverPlayer, new ClientboundSyncPlayerStatsPacket(stats.writeNBT()));
                    LOGGER.debug("Synchronized attribute data to client for overlay update");
                }

                // Consumir el ítem después de usarlo
                itemStack.shrink(1);

            }); // Fin de ifPresent

            // Indicar que la acción fue exitosa y el ítem se consumió
            return InteractionResultHolder.consume(itemStack);

        } else {
            // En el lado del cliente, realizar actualizaciones locales de overlays si es necesario
            // Esto es principalmente para feedback inmediato
            LOGGER.debug("Client side item use - overlays will update when server sync arrives");
            return InteractionResultHolder.success(itemStack);
        }
    }

    /**
     * Añade tooltip informativo al ítem
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        // Descripción principal
        pTooltipComponents.add(Component.translatable("tooltip.rpgstats.attribute_point_grant.description")
                .withStyle(ChatFormatting.GRAY));

        // Información detallada
        pTooltipComponents.add(Component.translatable("item.rpgstats.attribute_point_grant.grants", POINTS_TO_GRANT)
                .withStyle(ChatFormatting.GREEN));

        // Advertencia de consumo
        pTooltipComponents.add(Component.translatable("item.rpgstats.attribute_point_grant.single_use")
                .withStyle(ChatFormatting.YELLOW));

        // Info avanzada en modo debug (F3+H)
        if (pIsAdvanced.isAdvanced()) {
            pTooltipComponents.add(Component.translatable("item.rpgstats.attribute_point_grant.immediate_update")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }

        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }
}