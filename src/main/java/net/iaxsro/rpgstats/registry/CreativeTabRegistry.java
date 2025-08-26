package net.iaxsro.rpgstats.registry;

import net.iaxsro.rpgstats.RpgStatsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;

public class CreativeTabRegistry {

    // 1. Crea el DeferredRegister para CreativeModeTabs
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RpgStatsMod.MOD_ID);

    // 2. (Opcional) Define tus Pestañas Creativas Personalizadas aquí
    // Si no necesitas una pestaña personalizada y solo añades a las existentes, puedes omitir esto.
    /*
    public static final RegistryObject<CreativeModeTab> RPG_STATS_TAB = CREATIVE_MODE_TABS.register("rpg_stats_tab",
            () -> CreativeModeTab.builder()
                    // Icono de la pestaña (ej: usando tu ítem)
                    .icon(() -> new ItemStack(ItemRegistry.ATTRIBUTE_POINT_GRANT.get()))
                    // Título de la pestaña (usa un archivo de idioma .json para traducción)
                    .title(Component.translatable("creativetab." + RpgStatsMod.MOD_ID + ".rpg_stats_tab"))
                    // Función para definir qué ítems se muestran en esta pestaña
                    .displayItems((itemDisplayParameters, output) -> {
                        // Añade los ítems de tu mod aquí
                        output.accept(ItemRegistry.ATTRIBUTE_POINT_GRANT.get());
                        // output.accept(ItemRegistry.OTRO_ITEM.get()); // Si tuvieras más ítems
                        // También puedes añadir ítems de vanilla si quieres:
                        // output.accept(Items.DIAMOND_SWORD);
                    })
                    .build()
    );
    */


    // 3. Método para registrar el DeferredRegister en el bus de eventos del Mod
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
        RpgStatsMod.LOGGER.debug("Creative Mode Tab Registry para {} registrado.", RpgStatsMod.MOD_ID);
    }

    // --- Lógica Movida ---
    // La lógica para añadir ítems a pestañas existentes (Vanilla u otros mods)
    // usando el evento 'BuildCreativeModeTabContentsEvent' se moverá a 'event/ModBusEvents.java'.
}