package net.iaxsro.rpgstats.mixin;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.dodge.DodgeSkill;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

import java.util.UUID;

/**
 * Mixin for the BasicAttack class from the Epic Fight mod.
 * This class injects code into the BasicAttack skill to add custom functionality.
 */
@Mixin(DodgeSkill.class)
public class DodgeSkillMixin {
    // UUIDs for the different event listeners.
    private static final UUID ATTACK_UUID = UUID.fromString("bb18a7b4-52c5-4c85-987c-7b8ac2e1cf52");
    private static final UUID DODGE_UUID = UUID.fromString("ac7390b6-837d-459e-bb13-c9743e0d53dd");
    private static final UUID TAKE_DAMAGE_HURT_UUID = UUID.fromString("b047913d-2b2d-4367-b931-84056b4b90df");
    private static final UUID DEAL_DAMAGE_HURT_UUID = UUID.fromString("6db05cb7-c2c3-4e87-8ac2-fb4c10d764dc");

    /**
     * Injects code at the beginning of the onInitiate method.
     * This method is called when the skill is initiated.
     *
     * @param skillContainer The skill container.
     * @param ci             The callback info.
     */
    @Inject(
            method = {"executeOnServer"},
            at = {@At("HEAD")},
            remap = false
    )
    private void executeOnServer(SkillContainer skillContainer, FriendlyByteBuf args, CallbackInfo ci) {
//        skillContainer.getExecutor().getEventListener().addEventListener(EventType.BASIC_ATTACK_EVENT, ATTACK_UUID, (attackEvent) -> {
//            attackEvent.getPlayerPatch().getOriginal().getServer().getPlayerList().broadcastSystemMessage(Component.literal("Attack handled successfully!"), false);
//        });

        // Add an event listener for the successful dodge event.
        skillContainer.getExecutor().getEventListener().addEventListener(EventType.DODGE_SUCCESS_EVENT, DODGE_UUID, (dodgeSuccessEvent) -> {
            LivingEntity damageSourceEntity = (LivingEntity) dodgeSuccessEvent.getDamageSource().getEntity();
            Entity directSourceEntity = dodgeSuccessEvent.getDamageSource().getDirectEntity();
            ServerPlayer player = dodgeSuccessEvent.getPlayerPatch().getOriginal();
            EpicFightDamageSource EFDS = (EpicFightDamageSource) dodgeSuccessEvent.getDamageSource();

            float attackerAttackAttributeValue = (float) damageSourceEntity.getAttribute(Attributes.ATTACK_DAMAGE).getValue();

            EFDS.calculateDamageAgainst(damageSourceEntity, player, attackerAttackAttributeValue);
            player.getServer().getPlayerList().broadcastSystemMessage(Component.literal("Dodge event handled successfully! Amount dodged: " + attackerAttackAttributeValue + "!"), false);
        });

//        // Add an event listener for the take damage event.
//        skillContainer.getExecutor().getEventListener().addEventListener(EventType.TAKE_DAMAGE_EVENT_HURT, TAKE_DAMAGE_HURT_UUID, (hurtEvent) -> {
//            hurtEvent.getPlayerPatch().getOriginal().getServer().getPlayerList().broadcastSystemMessage(Component.literal("Take damage hurt event handled successfully!"), false);
//        });
//
//        // Add an event listener for the deal damage event.
//        skillContainer.getExecutor().getEventListener().addEventListener(EventType.DEAL_DAMAGE_EVENT_HURT, DEAL_DAMAGE_HURT_UUID, (dealDamageEvent) -> {
//            dealDamageEvent.getPlayerPatch().getOriginal().getServer().getPlayerList().broadcastSystemMessage(Component.literal("Deal damage hurt event handled successfully!"), false);
//        });

    }
}