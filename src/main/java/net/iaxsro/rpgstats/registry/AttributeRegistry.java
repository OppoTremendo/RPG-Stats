package net.iaxsro.rpgstats.registry;

import net.iaxsro.rpgstats.RpgStatsMod;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class AttributeRegistry {

    // 1. Crea el DeferredRegister para Atributos
    // Apunta al registro de atributos de Forge y usa el MOD_ID de tu mod.
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(ForgeRegistries.ATTRIBUTES, RpgStatsMod.MOD_ID);

    // 2. Define tus Atributos Personalizados usando RegistryObject
    // RegistryObject<Attribute> NOMBRE_ATRIBUTO = ATTRIBUTES.register("nombre_registro", () -> new RangedAttribute(...));
    // Corregimos los nombres y usamos los valores originales de min/max/default.

    public static final RegistryObject<Attribute> STRENGTH = ATTRIBUTES.register("strength", // Corregido: strength
            () -> new RangedAttribute("attribute." + RpgStatsMod.MOD_ID + ".strength", 0.0D, 0.0D, 1000.0D) // min=0, max=1000
                    .setSyncable(true)); // Importante para que funcione en multijugador

    public static final RegistryObject<Attribute> VITALITY = ATTRIBUTES.register("vitality",
            () -> new RangedAttribute("attribute." + RpgStatsMod.MOD_ID + ".vitality", 0.0D, 0.0D, 1000.0D)
                    .setSyncable(true));

    public static final RegistryObject<Attribute> DEXTERITY = ATTRIBUTES.register("dexterity",
            () -> new RangedAttribute("attribute." + RpgStatsMod.MOD_ID + ".dexterity", 0.0D, 0.0D, 1000.0D)
                    .setSyncable(true));

    public static final RegistryObject<Attribute> CONSTITUTION = ATTRIBUTES.register("constitution",
            () -> new RangedAttribute("attribute." + RpgStatsMod.MOD_ID + ".constitution", 0.0D, 0.0D, 1000.0D)
                    .setSyncable(true));

    public static final RegistryObject<Attribute> INTELLIGENCE = ATTRIBUTES.register("intelligence", // Corregido: intelligence
            () -> new RangedAttribute("attribute." + RpgStatsMod.MOD_ID + ".intelligence", 0.0D, 0.0D, 1000.0D) // Ajustado max a 1000 para consistencia, si 100 era intencional, puedes cambiarlo
                    .setSyncable(true));

    // 3. Método para registrar el DeferredRegister en el bus de eventos del Mod
    // Este método es llamado desde la clase principal del mod (RpgStatsMod).
    public static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
        RpgStatsMod.LOGGER.debug("Attribute Registry para {} registrado.", RpgStatsMod.MOD_ID);
    }

    // --- Lógica Movida ---
    // La lógica para *añadir* estos atributos a las entidades (EntityAttributeModificationEvent)
    // se moverá a la clase event/ModBusEvents.java.
    // Esta clase solo se centra en la *definición* y *registro* de los atributos.
}