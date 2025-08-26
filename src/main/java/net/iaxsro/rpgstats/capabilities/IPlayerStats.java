package net.iaxsro.rpgstats.capabilities;

import net.minecraft.nbt.CompoundTag;

/**
 * Interfaz que define los datos y operaciones de la capacidad PlayerStats.
 * Esta es la interfaz que otras partes del mod deberían usar para interactuar
 * con la capacidad, promoviendo bajo acoplamiento.
 */
public interface IPlayerStats {

    // --- Serialización / Deserialización ---

    /**
     * Guarda los datos de la capacidad en un CompoundTag NBT.
     *
     * @return Un nuevo CompoundTag con los datos guardados.
     */
    CompoundTag writeNBT();

    /**
     * Carga los datos de la capacidad desde un CompoundTag NBT.
     *
     * @param nbt El CompoundTag que contiene los datos.
     */
    void readNBT(CompoundTag nbt);

    /**
     * Método de compatibilidad para guardar datos NBT.
     *
     * @return Un nuevo CompoundTag con los datos guardados.
     */
    CompoundTag saveNBTData();

    /**
     * Método de compatibilidad para cargar datos NBT.
     *
     * @param nbt El CompoundTag que contiene los datos.
     */
    void loadNBTData(CompoundTag nbt);

    // --- Puntos de Atributo ---
    double getStrengthPoints();

    void setStrengthPoints(double value);

    void addStrengthPoints(double value);

    double getDexterityPoints();

    void setDexterityPoints(double value);

    void addDexterityPoints(double value);

    double getVitalityPoints();

    void setVitalityPoints(double value);

    void addVitalityPoints(double value);

    double getConstitutionPoints();

    void setConstitutionPoints(double value);

    void addConstitutionPoints(double value);

    double getIntelligencePoints();

    void setIntelligencePoints(double value);

    void addIntelligencePoints(double value);

    // --- Iteraciones de Atributo ---
    double getStrengthIterations();

    void setStrengthIterations(double value);

    void addStrengthIterations(double value);

    double getDexterityIterations();

    void setDexterityIterations(double value);

    void addDexterityIterations(double value);

    double getVitalityIterations();

    void setVitalityIterations(double value);

    void addVitalityIterations(double value);

    double getConstitutionIterations();

    void setConstitutionIterations(double value);

    void addConstitutionIterations(double value);

    double getIntelligenceIterations();

    void setIntelligenceIterations(double value);

    void addIntelligenceIterations(double value);

    // --- Nivel del Jugador ---
    int getLevel();

    void setLevel(int value);

    void incrementLevel();

    // --- Información del Jugador ---
    String getNickname();

    void setNickname(String value);

    boolean isFirstTimeJoining();

    void setFirstTimeJoining(boolean value);
}
