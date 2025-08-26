package net.iaxsro.rpgstats.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.UUID;

public class PlayerStats implements IPlayerStats {

    public static final Capability<IPlayerStats> PLAYER_STATS_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerStats.class);

    // --- NBT Keys ---
    private static final String NBT_KEY_STRENGTH_POINTS = "StrengthPoints";
    private static final String NBT_KEY_DEXTERITY_POINTS = "DexterityPoints";
    private static final String NBT_KEY_VITALITY_POINTS = "VitalityPoints";
    private static final String NBT_KEY_CONSTITUTION_POINTS = "ConstitutionPoints";
    private static final String NBT_KEY_INTELLIGENCE_POINTS = "IntelligencePoints";

    private static final String NBT_KEY_STRENGTH_ITERATIONS = "StrengthIterations";
    private static final String NBT_KEY_DEXTERITY_ITERATIONS = "DexterityIterations";
    private static final String NBT_KEY_VITALITY_ITERATIONS = "VitalityIterations";
    private static final String NBT_KEY_CONSTITUTION_ITERATIONS = "ConstitutionIterations";
    private static final String NBT_KEY_INTELLIGENCE_ITERATIONS = "IntelligenceIterations";

    private static final String NBT_KEY_LEVEL = "PlayerLevel";
    private static final String NBT_KEY_NICKNAME = "Nickname";
    private static final String NBT_KEY_FIRST_JOIN = "FirstTimeJoining";
    private static final String NBT_KEY_LEVEL_UUID = "CurrentLevelUUID";

    // --- Data Fields ---
    private double strengthPoints = 0.0;
    private double dexterityPoints = 0.0;
    private double vitalityPoints = 0.0;
    private double constitutionPoints = 0.0;
    private double intelligencePoints = 0.0;

    private double strengthIterations = 0.0;
    private double dexterityIterations = 0.0;
    private double vitalityIterations = 0.0;
    private double constitutionIterations = 0.0;
    private double intelligenceIterations = 0.0;

    private int level = 0;
    private String nickname = "";
    private boolean firstTimeJoining = true;
    @Nullable
    private UUID currentLevelUUID = null;

    @Override
    public CompoundTag saveNBTData() {
        return writeNBT();
    }

    @Override
    public void loadNBTData(CompoundTag nbt) {
        readNBT(nbt);
    }

    @Override
    public CompoundTag writeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putDouble(NBT_KEY_STRENGTH_POINTS, this.strengthPoints);
        nbt.putDouble(NBT_KEY_DEXTERITY_POINTS, this.dexterityPoints);
        nbt.putDouble(NBT_KEY_VITALITY_POINTS, this.vitalityPoints);
        nbt.putDouble(NBT_KEY_CONSTITUTION_POINTS, this.constitutionPoints);
        nbt.putDouble(NBT_KEY_INTELLIGENCE_POINTS, this.intelligencePoints);

        nbt.putDouble(NBT_KEY_STRENGTH_ITERATIONS, this.strengthIterations);
        nbt.putDouble(NBT_KEY_DEXTERITY_ITERATIONS, this.dexterityIterations);
        nbt.putDouble(NBT_KEY_VITALITY_ITERATIONS, this.vitalityIterations);
        nbt.putDouble(NBT_KEY_CONSTITUTION_ITERATIONS, this.constitutionIterations);
        nbt.putDouble(NBT_KEY_INTELLIGENCE_ITERATIONS, this.intelligenceIterations);

        nbt.putInt(NBT_KEY_LEVEL, this.level);
        nbt.putString(NBT_KEY_NICKNAME, this.nickname != null ? this.nickname : "");
        nbt.putBoolean(NBT_KEY_FIRST_JOIN, this.firstTimeJoining);
        if (this.currentLevelUUID != null) {
            nbt.putUUID(NBT_KEY_LEVEL_UUID, this.currentLevelUUID);
        }
        return nbt;
    }

    @Override
    public void readNBT(CompoundTag nbt) {
        if (nbt == null) {
            LOGGER.warn("Attempting to read null NBT for PlayerStats. Ignoring.");
            return;
        }

        setStrengthPoints(nbt.getDouble(NBT_KEY_STRENGTH_POINTS));
        setDexterityPoints(nbt.getDouble(NBT_KEY_DEXTERITY_POINTS));
        setVitalityPoints(nbt.getDouble(NBT_KEY_VITALITY_POINTS));
        setConstitutionPoints(nbt.getDouble(NBT_KEY_CONSTITUTION_POINTS));
        setIntelligencePoints(nbt.getDouble(NBT_KEY_INTELLIGENCE_POINTS));

        setStrengthIterations(nbt.getDouble(NBT_KEY_STRENGTH_ITERATIONS));
        setDexterityIterations(nbt.getDouble(NBT_KEY_DEXTERITY_ITERATIONS));
        setVitalityIterations(nbt.getDouble(NBT_KEY_VITALITY_ITERATIONS));
        setConstitutionIterations(nbt.getDouble(NBT_KEY_CONSTITUTION_ITERATIONS));
        setIntelligenceIterations(nbt.getDouble(NBT_KEY_INTELLIGENCE_ITERATIONS));

        setLevel(nbt.getInt(NBT_KEY_LEVEL));
        setNickname(nbt.getString(NBT_KEY_NICKNAME));
        setFirstTimeJoining(nbt.getBoolean(NBT_KEY_FIRST_JOIN));

        if (nbt.hasUUID(NBT_KEY_LEVEL_UUID)) {
            setCurrentLevelUUID(nbt.getUUID(NBT_KEY_LEVEL_UUID));
        } else {
            setCurrentLevelUUID(null);
        }
    }

    // --- Getters ---
    @Override
    public double getStrengthPoints() {
        return strengthPoints;
    }

    // --- Setters ---
    @Override
    public void setStrengthPoints(double value) {
        this.strengthPoints = Math.max(0.0, value);
    }

    @Override
    public double getDexterityPoints() {
        return dexterityPoints;
    }

    @Override
    public void setDexterityPoints(double value) {
        this.dexterityPoints = Math.max(0.0, value);
    }

    @Override
    public double getVitalityPoints() {
        return vitalityPoints;
    }

    @Override
    public void setVitalityPoints(double value) {
        this.vitalityPoints = Math.max(0.0, value);
    }

    @Override
    public double getConstitutionPoints() {
        return constitutionPoints;
    }

    @Override
    public void setConstitutionPoints(double value) {
        this.constitutionPoints = Math.max(0.0, value);
    }

    @Override
    public double getIntelligencePoints() {
        return intelligencePoints;
    }

    @Override
    public void setIntelligencePoints(double value) {
        this.intelligencePoints = Math.max(0.0, value);
    }

    @Override
    public double getStrengthIterations() {
        return strengthIterations;
    }

    @Override
    public void setStrengthIterations(double value) {
        this.strengthIterations = Math.max(0.0, value);
    }

    @Override
    public double getDexterityIterations() {
        return dexterityIterations;
    }

    @Override
    public void setDexterityIterations(double value) {
        this.dexterityIterations = Math.max(0.0, value);
    }

    @Override
    public double getVitalityIterations() {
        return vitalityIterations;
    }

    @Override
    public void setVitalityIterations(double value) {
        this.vitalityIterations = Math.max(0.0, value);
    }

    @Override
    public double getConstitutionIterations() {
        return constitutionIterations;
    }

    @Override
    public void setConstitutionIterations(double value) {
        this.constitutionIterations = Math.max(0.0, value);
    }

    @Override
    public double getIntelligenceIterations() {
        return intelligenceIterations;
    }

    @Override
    public void setIntelligenceIterations(double value) {
        this.intelligenceIterations = Math.max(0.0, value);
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public void setLevel(int value) {
        this.level = Math.max(0, value);
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    @Override
    public void setNickname(String value) {
        this.nickname = Objects.requireNonNullElse(value, "");
    }

    @Override
    public boolean isFirstTimeJoining() {
        return firstTimeJoining;
    }

    @Override
    public void setFirstTimeJoining(boolean value) {
        this.firstTimeJoining = value;
    }

    @Override
    @Nullable
    public UUID getCurrentLevelUUID() {
        return currentLevelUUID;
    }

    @Override
    public void setCurrentLevelUUID(@Nullable UUID uuid) {
        this.currentLevelUUID = uuid;
    }

    // --- Adders ---
    @Override
    public void addStrengthPoints(double value) {
        if (value > 0) setStrengthPoints(this.strengthPoints + value);
    }

    @Override
    public void addDexterityPoints(double value) {
        if (value > 0) setDexterityPoints(this.dexterityPoints + value);
    }

    @Override
    public void addVitalityPoints(double value) {
        if (value > 0) setVitalityPoints(this.vitalityPoints + value);
    }

    @Override
    public void addConstitutionPoints(double value) {
        if (value > 0) setConstitutionPoints(this.constitutionPoints + value);
    }

    @Override
    public void addIntelligencePoints(double value) {
        if (value > 0) setIntelligencePoints(this.intelligencePoints + value);
    }

    @Override
    public void addStrengthIterations(double value) {
        if (value > 0) setStrengthIterations(this.strengthIterations + value);
    }

    @Override
    public void addDexterityIterations(double value) {
        if (value > 0) setDexterityIterations(this.dexterityIterations + value);
    }

    @Override
    public void addVitalityIterations(double value) {
        if (value > 0) setVitalityIterations(this.vitalityIterations + value);
    }

    @Override
    public void addConstitutionIterations(double value) {
        if (value > 0) setConstitutionIterations(this.constitutionIterations + value);
    }

    @Override
    public void addIntelligenceIterations(double value) {
        if (value > 0) setIntelligenceIterations(this.intelligenceIterations + value);
    }

    @Override
    public void incrementLevel() {
        setLevel(this.level + 1);
    }
}
