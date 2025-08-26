package net.iaxsro.rpgstats.attributesystem;

import net.iaxsro.rpgstats.RpgStatsMod;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = RpgStatsMod.MOD_ID)
public class InitiateSystem {
    private static final UUID BASIC_ATTACK_UUID = UUID.fromString("bb18a7b4-52c5-4c85-987c-7b8ac2e1cf52");
    private static final UUID DEAL_ATTACK_UUID = UUID.fromString("6db05cb7-c2c3-4e87-8ac2-fb4c10d764dc");
    private static final UUID DEAL_HURT_UUID = UUID.fromString("469ccd20-858e-4f97-85ef-9e2e43fc39cc");
    private static final UUID DEAL_DAMAGE_UUID = UUID.fromString("b98e9696-f1e4-4f80-9c3e-5492f2e157ad");
    private static final UUID TAKE_DAMAGE_HURT_UUID = UUID.fromString("b047913d-2b2d-4367-b931-84056b4b90df");
    private static final UUID DODGE_UUID = UUID.fromString("ac7390b6-837d-459e-bb13-c9743e0d53dd");

    private static final Logger LOGGER = LoggerFactory.getLogger(InitiateSystem.class);

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayerPatch playerPatch = EpicFightCapabilities.getEntityPatch(event.getEntity(), ServerPlayerPatch.class);
        if (playerPatch == null) {
            LOGGER.error("Could not get ServerPlayerPatch for {} on login!", event.getEntity().getName().getString());
            return;
        }

        playerPatch.getEventListener().addEventListener(EventType.BASIC_ATTACK_EVENT, BASIC_ATTACK_UUID, (basicAttackEvent) -> {
            playerPatch.getOriginal().getServer().getPlayerList().broadcastSystemMessage(Component.literal("Attack handled successfully!"), false);
        });
        playerPatch.getEventListener().addEventListener(EventType.DEAL_DAMAGE_EVENT_ATTACK, DEAL_ATTACK_UUID, (dealAttackEvent) -> {
            float attack_damage = dealAttackEvent.getAttackDamage();
            dealAttackEvent.getPlayerPatch().getOriginal().getServer().getPlayerList().broadcastSystemMessage(Component.literal(" Attacked with " + attack_damage + " raw damage."), false);
        });
        playerPatch.getEventListener().addEventListener(EventType.DEAL_DAMAGE_EVENT_HURT, DEAL_HURT_UUID, (dealHurtEvent) -> {
            float hurt_damage = dealHurtEvent.getAttackDamage();
            dealHurtEvent.getPlayerPatch().getOriginal().getServer().getPlayerList().broadcastSystemMessage(Component.literal(" Hurt with " + hurt_damage + " raw damage."), false);
        });
        playerPatch.getEventListener().addEventListener(EventType.DEAL_DAMAGE_EVENT_DAMAGE, DEAL_DAMAGE_UUID, (dealDamageEvent) -> {
            float final_damage = dealDamageEvent.getAttackDamage();
            dealDamageEvent.getPlayerPatch().getOriginal().getServer().getPlayerList().broadcastSystemMessage(Component.literal(" Damaged with " + final_damage + " raw damage."), false);
        });
        playerPatch.getEventListener().addEventListener(EventType.TAKE_DAMAGE_EVENT_DAMAGE, TAKE_DAMAGE_HURT_UUID, (hurtEvent) -> {
            float taken_damage = hurtEvent.getDamage();
            hurtEvent.getPlayerPatch().getOriginal().getServer().getPlayerList().broadcastSystemMessage(Component.literal("Took " + taken_damage + " damage!"), false);
        });
        playerPatch.getEventListener().addEventListener(EventType.DODGE_SUCCESS_EVENT, DODGE_UUID, (dodgeSuccessEvent) -> {
            LivingEntity damageSourceEntity = (LivingEntity) dodgeSuccessEvent.getDamageSource().getEntity();
            Entity directSourceEntity = dodgeSuccessEvent.getDamageSource().getDirectEntity();
            ServerPlayer player = dodgeSuccessEvent.getPlayerPatch().getOriginal();
            EpicFightDamageSource EFDS = (EpicFightDamageSource) dodgeSuccessEvent.getDamageSource();

            float attackerAttackAttributeValue = (float) damageSourceEntity.getAttribute(Attributes.ATTACK_DAMAGE).getValue();

            EFDS.calculateDamageAgainst(damageSourceEntity, player, attackerAttackAttributeValue);
            player.getServer().getPlayerList().broadcastSystemMessage(Component.literal("Dodged: " + attackerAttackAttributeValue + "!"), false);
        });
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        // It's possible for the capability to be detached before this event is handled.
        // We get the patch and check if it's null before trying to use it.
        ServerPlayerPatch playerPatch = EpicFightCapabilities.getEntityPatch(event.getEntity(), ServerPlayerPatch.class);
        if (playerPatch != null) {
            // If the patch exists, we can safely remove the listeners.
            playerPatch.getEventListener().removeListener(EventType.BASIC_ATTACK_EVENT, BASIC_ATTACK_UUID);
            playerPatch.getEventListener().removeListener(EventType.DEAL_DAMAGE_EVENT_ATTACK, DEAL_ATTACK_UUID);
            playerPatch.getEventListener().removeListener(EventType.DEAL_DAMAGE_EVENT_HURT, DEAL_HURT_UUID);
            playerPatch.getEventListener().removeListener(EventType.DEAL_DAMAGE_EVENT_DAMAGE, DEAL_DAMAGE_UUID);
            playerPatch.getEventListener().removeListener(EventType.TAKE_DAMAGE_EVENT_DAMAGE, TAKE_DAMAGE_HURT_UUID);
            playerPatch.getEventListener().removeListener(EventType.DODGE_SUCCESS_EVENT, DODGE_UUID);
        } else {
            // Optional: Log a warning. This is not an error, but can be useful for debugging.
            // It simply means the capability was already cleaned up by its owner mod.
            LOGGER.warn("ServerPlayerPatch not found for {} on logout. This is often expected and not an error.", event.getEntity().getName().getString());
        }
    }
}
