package net.iaxsro.rpgstats.registry;

import net.iaxsro.rpgstats.RpgStatsMod;
import net.iaxsro.rpgstats.item.AttributePointGrantItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemRegistry {

    // 1. Crea el DeferredRegister para Items
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, RpgStatsMod.MOD_ID);

    // 2. Define tus Items Personalizados usando RegistryObject
    // Renombramos el ítem y su nombre de registro para mayor claridad.
    // Apuntamos a la nueva clase 'AttributePointGrantItem' que crearemos más adelante.
    public static final RegistryObject<Item> ATTRIBUTE_POINT_GRANT = ITEMS.register("attribute_point_grant", // Nuevo nombre de registro
            () -> new AttributePointGrantItem(new Item.Properties().stacksTo(64).rarity(Rarity.COMMON)) // Pasamos las propiedades aquí
            // Nota: La propiedad .isFoil() se manejará dentro de la clase AttributePointGrantItem
    );

    // --- Puedes añadir más ítems aquí en el futuro ---
    /*
    public static final RegistryObject<Item> OTRO_ITEM = ITEMS.register("otro_item",
            () -> new OtroItemPersonalizado(new Item.Properties().stacksTo(1))
    );
    */

    // 3. Método para registrar el DeferredRegister en el bus de eventos del Mod
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        RpgStatsMod.LOGGER.debug("Item Registry para {} registrado.", RpgStatsMod.MOD_ID);
    }
}