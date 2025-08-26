package net.iaxsro.rpgstats.config;

import net.iaxsro.rpgstats.RpgStatsMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

// Registra esta clase en el bus de eventos del Mod para que los @SubscribeEvent funcionen
@Mod.EventBusSubscriber(modid = RpgStatsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ConfigRegistration {

    // Registra la configuración durante la construcción del mod
    @SubscribeEvent
    public static void registerConfig(final FMLConstructModEvent event) {
        ModLoadingContext.get().registerConfig(Type.COMMON, ModConfig.COMMON_SPEC, "rpgstats-common.toml"); // Usa la SPEC y un nuevo nombre de archivo
        RpgStatsMod.LOGGER.debug("Registrando archivo de configuración para {}", RpgStatsMod.MOD_ID);
    }

    // (Opcional pero recomendado) Escucha eventos de carga/recarga de configuración
    // Esto permite actualizar valores cacheados si el usuario edita el archivo y usa /reload
    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading event) {
        // Puedes añadir lógica aquí si necesitas recalcular algo cuando la config se carga
        RpgStatsMod.LOGGER.info("Cargando configuración para {}: {}", RpgStatsMod.MOD_ID, event.getConfig().getFileName());
    }

    @SubscribeEvent
    public static void onReload(final ModConfigEvent.Reloading event) {
        // Similar a onLoad, pero para recargas
        RpgStatsMod.LOGGER.info("Recargando configuración para {}: {}", RpgStatsMod.MOD_ID, event.getConfig().getFileName());
        // Aquí podrías invalidar cachés o actualizar valores derivados de la configuración
    }
}