package net.iaxsro.rpgstats.attributesystem;

import net.iaxsro.rpgstats.RpgStatsMod;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.server.level.ServerPlayer;

@Mod.EventBusSubscriber(modid = RpgStatsMod.MOD_ID)
public class InitiateSystem {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        ServerPlayer player = (ServerPlayer) event.getEntity();
        EventListeners.applyAttackEventListener(player);
        EventListeners.applyDealAttackEventListener(player);
        EventListeners.applyDealHurtEventListener(player);
        EventListeners.applyDealDamageEventListener(player);
        EventListeners.applyTakeDamageEventListener(player);
        EventListeners.applyDodgeSuccessEventListener(player);
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        EventListeners.removeEventListeners((ServerPlayer) event.getEntity());
    }
}
