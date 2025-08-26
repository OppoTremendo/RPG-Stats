package net.iaxsro.rpgstats.event;

import net.iaxsro.rpgstats.RpgStatsMod;
import net.iaxsro.rpgstats.capabilities.IPlayerStats;
import net.iaxsro.rpgstats.capabilities.PlayerStats;
import net.iaxsro.rpgstats.capabilities.PlayerStatsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// Escucha eventos en ambos buses: MOD (para RegisterCapabilitiesEvent) y FORGE (para AttachCapabilitiesEvent, PlayerEvent.Clone)
@Mod.EventBusSubscriber(modid = RpgStatsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CapabilityEvents {

    // Nombre de la capacidad para vincularla
    // Se corrigió para usar la constante de PlayerStatsProvider si está disponible, o mantener la ResourceLocation
    // private static final ResourceLocation PLAYER_STATS_CAP_RL = PlayerStatsProvider.IDENTIFIER;
    // Si PlayerStatsProvider.IDENTIFIER no existe o no es público/estático, usa esto:
    private static final ResourceLocation PLAYER_STATS_CAP_RL = new ResourceLocation(RpgStatsMod.MOD_ID, "player_stats");


    // --- Registro de la Capacidad ---
    @SubscribeEvent
    public static void registerCapabilities(final RegisterCapabilitiesEvent event) {
        event.register(IPlayerStats.class); // Registra la INTERFAZ
        RpgStatsMod.LOGGER.info("Capacidad IPlayerStats registrada.");
    }

    // --- Vinculación de la Capacidad ---
    // Estos eventos se disparan en el bus FORGE
    @Mod.EventBusSubscriber(modid = RpgStatsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    // Especificar bus FORGE explícitamente
    public static class ForgeEvents {

        @SubscribeEvent
        public static void onAttachCapabilitiesPlayer(final AttachCapabilitiesEvent<Entity> event) {
            // Vincula la capacidad solo a jugadores reales
            if (event.getObject() instanceof Player player && !(player instanceof FakePlayer)) {
                // No adjuntar si ya tiene la capacidad (importante si el evento se dispara múltiples veces)
                if (!player.getCapability(PlayerStats.PLAYER_STATS_CAPABILITY).isPresent()) {
                    // Crea una instancia del proveedor y la adjunta
                    event.addCapability(PLAYER_STATS_CAP_RL, new PlayerStatsProvider());
                    // Log seguro usando UUID en lugar de getName()
                    RpgStatsMod.LOGGER.trace("Adjuntando capacidad PlayerStats a entidad jugador con UUID: {}", player.getUUID());
                }
            }
        }


        // --- Proveedor de la Capacidad ---
        // Esta clase se movió a su propio archivo: net.iaxsro.rpgstats.capabilities.PlayerStatsProvider
        // Si no es así, puedes dejarla aquí. Asegúrate que el import al principio sea correcto.
    /*
    public static class PlayerStatsProvider implements ICapabilitySerializable<CompoundTag> {

        public static final ResourceLocation IDENTIFIER = new ResourceLocation(RpgStatsMod.MOD_ID, "player_stats");

        private final IPlayerStats stats = new PlayerStats();
        private final LazyOptional<IPlayerStats> optional = LazyOptional.of(() -> stats);

        @NotNull
        @Override
        public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return cap == PlayerStats.PLAYER_STATS_CAPABILITY ? optional.cast() : LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return stats.writeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            stats.readNBT(nbt);
        }
    }
    */
    }
}