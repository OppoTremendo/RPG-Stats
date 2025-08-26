package net.iaxsro.rpgstats.capabilities; // ¡Paquete correcto!

import net.iaxsro.rpgstats.RpgStatsMod;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerStatsProvider implements ICapabilitySerializable<CompoundTag> { // ¡Clase pública!

    // Identificador único para la capability
    public static final ResourceLocation IDENTIFIER = new ResourceLocation(RpgStatsMod.MOD_ID, "player_stats");

    // Instancia concreta de la capacidad
    private final IPlayerStats stats = new PlayerStats(); // O crea una instancia por defecto si es necesario
    // Wrapper LazyOptional para la instancia
    private final LazyOptional<IPlayerStats> optional = LazyOptional.of(() -> stats);

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        // Devuelve la capacidad si es la que se pide
        return cap == PlayerStats.PLAYER_STATS_CAPABILITY ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        // Llama al método de guardado de tu implementación
        return stats.saveNBTData();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        // Llama al método de carga de tu implementación
        stats.loadNBTData(nbt);
    }

    // (Opcional) invalidar el LazyOptional
    // public void invalidate() { optional.invalidate(); }
}