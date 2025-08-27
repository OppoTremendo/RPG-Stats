package net.iaxsro.rpgstats.attributesystem;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

import java.util.UUID;

/**
 * Helpers to register and remove Epic Fight listeners.
 */
public class EventListeners {
    public static final UUID BASIC_ATTACK_UUID = UUID.fromString("bb18a7b4-52c5-4c85-987c-7b8ac2e1cf52");
    public static final UUID DEAL_ATTACK_UUID = UUID.fromString("6db05cb7-c2c3-4e87-8ac2-fb4c10d764dc");
    public static final UUID DEAL_HURT_UUID = UUID.fromString("469ccd20-858e-4f97-85ef-9e2e43fc39cc");
    public static final UUID DEAL_DAMAGE_UUID = UUID.fromString("b98e9696-f1e4-4f80-9c3e-5492f2e157ad");
    public static final UUID TAKE_DAMAGE_HURT_UUID = UUID.fromString("b047913d-2b2d-4367-b931-84056b4b90df");
    public static final UUID DODGE_UUID = UUID.fromString("ac7390b6-837d-459e-bb13-c9743e0d53dd");

    private static final Logger LOGGER = LoggerFactory.getLogger(EventListeners.class);

    public static void applyAttackEventListener(ServerPlayer player) {
        ServerPlayerPatch playerPatch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);
        if (playerPatch == null) {
            LOGGER.error("Could not get ServerPlayerPatch for {}!", player.getName().getString());
            return;
        }
        playerPatch.getEventListener().addEventListener(PlayerEventListener.EventType.BASIC_ATTACK_EVENT,
                BASIC_ATTACK_UUID, (basicAttackEvent) -> {
                    if (basicAttackEvent.isCanceled()) {
                        playerPatch.getOriginal().getServer().getPlayerList()
                                .broadcastSystemMessage(Component.literal("Attack canceled!"), false);
                        return;
                    }
                    playerPatch.getOriginal().getServer().getPlayerList()
                            .broadcastSystemMessage(Component.literal("Attack handled successfully!"), false);
                });
    }

    public static void applyDealAttackEventListener(ServerPlayer player) {
        ServerPlayerPatch playerPatch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);
        if (playerPatch == null) {
            LOGGER.error("Could not get ServerPlayerPatch for {}!", player.getName().getString());
            return;
        }
        playerPatch.getEventListener().addEventListener(PlayerEventListener.EventType.DEAL_DAMAGE_EVENT_ATTACK,
                DEAL_ATTACK_UUID, (dealAttackEvent) -> {
                    double attackDamage = dealAttackEvent.getAttackDamage();
                    dealAttackEvent.getPlayerPatch().getOriginal().getServer().getPlayerList().broadcastSystemMessage(
                            Component.literal(" Attacked with " + attackDamage + " raw damage."), false);
                });
    }

    public static void applyDealHurtEventListener(ServerPlayer player) {
        ServerPlayerPatch playerPatch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);
        if (playerPatch == null) {
            LOGGER.error("Could not get ServerPlayerPatch for {}!", player.getName().getString());
            return;
        }
        playerPatch.getEventListener().addEventListener(PlayerEventListener.EventType.DEAL_DAMAGE_EVENT_HURT,
                DEAL_HURT_UUID, (dealHurtEvent) -> {
                    double hurtDamage = dealHurtEvent.getAttackDamage();
                    dealHurtEvent.getPlayerPatch().getOriginal().getServer().getPlayerList().broadcastSystemMessage(
                            Component.literal(" Hurt with " + hurtDamage + " raw damage."), false);
                });
    }

    public static void applyDealDamageEventListener(ServerPlayer player) {
        ServerPlayerPatch playerPatch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);
        if (playerPatch == null) {
            LOGGER.error("Could not get ServerPlayerPatch for {}!", player.getName().getString());
            return;
        }
        playerPatch.getEventListener().addEventListener(PlayerEventListener.EventType.DEAL_DAMAGE_EVENT_DAMAGE,
                DEAL_DAMAGE_UUID, (dealDamageEvent) -> {
                    double finalDamage = dealDamageEvent.getAttackDamage();
                    dealDamageEvent.getPlayerPatch().getOriginal().getServer().getPlayerList().broadcastSystemMessage(
                            Component.literal(" Damaged with " + finalDamage + " raw damage."), false);
                });
    }

    public static void applyTakeDamageEventListener(ServerPlayer player) {
        ServerPlayerPatch playerPatch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);
        if (playerPatch == null) {
            LOGGER.error("Could not get ServerPlayerPatch for {}!", player.getName().getString());
            return;
        }
        playerPatch.getEventListener().addEventListener(PlayerEventListener.EventType.TAKE_DAMAGE_EVENT_DAMAGE,
                TAKE_DAMAGE_HURT_UUID, (hurtEvent) -> {
                    double takenDamage = hurtEvent.getDamage();
                    hurtEvent.getPlayerPatch().getOriginal().getServer().getPlayerList().broadcastSystemMessage(
                            Component.literal("Took " + takenDamage + " damage!"), false);
                });
    }

    public static void applyDodgeSuccessEventListener(ServerPlayer player) {
        ServerPlayerPatch playerPatch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);
        if (playerPatch == null) {
            LOGGER.error("Could not get ServerPlayerPatch for {}!", player.getName().getString());
            return;
        }
        playerPatch.getEventListener().addEventListener(PlayerEventListener.EventType.DODGE_SUCCESS_EVENT,
                DODGE_UUID, (dodgeSuccessEvent) -> {
                    LivingEntity damageSourceEntity = (LivingEntity) dodgeSuccessEvent.getDamageSource().getEntity();
                    ServerPlayer dodger = dodgeSuccessEvent.getPlayerPatch().getOriginal();
                    EpicFightDamageSource efds = (EpicFightDamageSource) dodgeSuccessEvent.getDamageSource();

                    double attackerAttackAttributeValue = damageSourceEntity.getAttribute(Attributes.ATTACK_DAMAGE).getValue();

                    efds.calculateDamageAgainst(damageSourceEntity, dodger, (float) attackerAttackAttributeValue);
                    dodger.getServer().getPlayerList().broadcastSystemMessage(
                            Component.literal("Dodged: " + attackerAttackAttributeValue + "!"), false);
                });
    }

    public static void removeEventListeners(ServerPlayer player) {
        ServerPlayerPatch playerPatch = EpicFightCapabilities.getEntityPatch(player, ServerPlayerPatch.class);
        if (playerPatch != null) {
            PlayerEventListener listener = playerPatch.getEventListener();
            listener.removeListener(PlayerEventListener.EventType.BASIC_ATTACK_EVENT, BASIC_ATTACK_UUID);
            listener.removeListener(PlayerEventListener.EventType.DEAL_DAMAGE_EVENT_ATTACK, DEAL_ATTACK_UUID);
            listener.removeListener(PlayerEventListener.EventType.DEAL_DAMAGE_EVENT_HURT, DEAL_HURT_UUID);
            listener.removeListener(PlayerEventListener.EventType.DEAL_DAMAGE_EVENT_DAMAGE, DEAL_DAMAGE_UUID);
            listener.removeListener(PlayerEventListener.EventType.TAKE_DAMAGE_EVENT_DAMAGE, TAKE_DAMAGE_HURT_UUID);
            listener.removeListener(PlayerEventListener.EventType.DODGE_SUCCESS_EVENT, DODGE_UUID);
        } else {
            LOGGER.warn("ServerPlayerPatch not found for {}", player.getName().getString());
        }
    }

    public static void applyAllEventListeners(ServerPlayer player){
        applyAttackEventListener(player);
        applyDealAttackEventListener(player);
        applyDealHurtEventListener(player);
        applyDealDamageEventListener(player);
        applyTakeDamageEventListener(player);
        applyDodgeSuccessEventListener(player);
    }
}

