/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.block.prop;

import gregtech.api.GTValues;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.util.MiscUtils;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Optional;

import javax.annotation.Nonnull;

import static hellfirepvp.modularmachinery.common.base.Mods.GREGTECH;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: EnergyHatchSize
 * Created by HellFirePvP
 * Date: 08.07.2017 / 10:25
 * TODO: Transfer to normal class.
 */
public enum EnergyHatchData implements IStringSerializable {

    TINY(2048, 1, 128, 1, 2),
    SMALL(4096, 2, 512, 2, 2),
    NORMAL(8192, 2, 512, 2, 2),
    REINFORCED(16384, 3, 2048, 3, 2),
    BIG(32768, 4, 8192, 4, 2),
    HUGE(131072, 5, 32768, 5, 2),
    LUDICROUS(524288, 6, 131072, 6, 2),
    ULTIMATE(2097152, 6, 131072, 6, 2);

    public static boolean enableDEIntegration      = true;
    public static boolean delayedEnergyCoreSearch  = true;
    public static int     energyCoreSearchDelay    = 100;
    public static int     maxEnergyCoreSearchDelay = 300;
    public static int     searchRange              = 16;
    public static boolean enableGTExplodes         = true;
    private final int     defaultConfigurationEnergy;
    private final int     defaultConfigurationTransferLimit;
    private final int     defaultIC2EnergyTier;
    private final int     defaultGTEnergyTier;
    private final int     defaultGTAmperage;
    public        long    maxEnergy;
    public        long    transferLimit;
    public        int     ic2EnergyTier;
    public        int     gtEnergyTier;
    public        int     gtAmperage;

    EnergyHatchData(int maxEnergy, int ic2EnergyTier, int transferLimit, int gtEnergyTier, int gtAmperage) {
        this.defaultConfigurationEnergy = maxEnergy;
        this.defaultIC2EnergyTier = ic2EnergyTier;
        this.defaultConfigurationTransferLimit = transferLimit;
        this.defaultGTEnergyTier = gtEnergyTier;
        this.defaultGTAmperage = gtAmperage;
    }

    public static void loadFromConfig(Configuration cfg) {
        for (EnergyHatchData size : values()) {
            size.maxEnergy = cfg.get("energyhatch.size", size.name().toUpperCase(), String.valueOf(size.defaultConfigurationEnergy), "Energy storage size of the energy hatch. [range: 0 ~ 9223372036854775807, default: " + size.defaultConfigurationEnergy + "]").getLong();
            size.maxEnergy = MiscUtils.clamp(size.maxEnergy, 1, Long.MAX_VALUE);
            size.transferLimit = cfg.get("energyhatch.limit", size.name().toUpperCase(), String.valueOf(size.defaultConfigurationTransferLimit), "Defines the transfer limit for RF/FE things. IC2's transfer limit is defined by the voltage tier. [range: 1 ~ 9223372036854775806, default: " + size.defaultConfigurationEnergy + "]").getLong();
            size.transferLimit = MiscUtils.clamp(size.transferLimit, 1, Long.MAX_VALUE - 1);

            size.ic2EnergyTier = cfg.get("energyhatch.tier", size.name().toUpperCase(), size.defaultIC2EnergyTier, "Defines the IC2 output-voltage tier. Only affects the power the output hatches will output power as. 0 = 'ULV' = 8 EU/t, 1 = 'LV' = 32 EU/t, 2 = 'MV' = 128 EU/t, ... [range: 0 ~ 12, default: " + size.defaultIC2EnergyTier + "]").getInt();

            if (GREGTECH.isPresent()) {
                int gtEnergyTierlength = GTValues.VN.length - 1;

                size.gtEnergyTier = cfg.get("energyhatch.gtvoltage", size.name().toUpperCase(), size.defaultGTEnergyTier, "Defines the GT voltage tier. Affects both input and output hatches of this tier. [range: 0 ~ " + gtEnergyTierlength + ", default: " + size.defaultGTEnergyTier + "]").getInt();
                size.gtEnergyTier = MathHelper.clamp(size.gtEnergyTier, 0, gtEnergyTierlength);

                size.gtAmperage = cfg.get("energyhatch.gtamperage", size.name().toUpperCase(), size.defaultGTAmperage, "Defines the GT amperage. Affects both output amperage as well as maximum input amperage. [range: 1 ~ " + Integer.MAX_VALUE + ", default: " + size.defaultGTAmperage + "]").getInt();
                size.gtAmperage = MathHelper.clamp(size.gtAmperage, 1, Integer.MAX_VALUE);
            }
        }

        enableGTExplodes = cfg.getBoolean("enable-GT-Explodes", "energyhatch", true,
            "When enabled, the energy chamber will use GT's explosive mechanism, which is only valid when GT is installed");
        enableDEIntegration = cfg.getBoolean("enable-de-integration", "energyhatch", true,
            "When enabled, EnergyHatch can be used as an energy tower for the Draconic Evolution energy core and can automatically output energy at a rate that depends on the maximum rate in the configuration. Available only when Draconic Evolution is installed.");
        searchRange = cfg.getInt("energy-core-search-range", "energyhatch", 16, 1, 64,
            "How many energy cores within a radius does EnergyHatch look for?");
        delayedEnergyCoreSearch = cfg.getBoolean("delayed-energy-core-search", "energyhatch", true,
            "When enabled, the search interval grows gradually when EnergyHatch fails to find the energy core.");
        energyCoreSearchDelay = cfg.getInt("energy-core-search-delay", "energyhatch", 100, 1, 1200,
            "The minimum energy core search interval. (TimeUnit: Tick)");
        maxEnergyCoreSearchDelay = cfg.getInt("max-energy-core-search-delay", "energyhatch", 300, 2, 1200,
            "The maximum energy core search interval. (TimeUnit: Tick)");

        if (energyCoreSearchDelay >= maxEnergyCoreSearchDelay) {
            ModularMachinery.log.warn("energy-core-search-delay is bigger than or equal max-energy-core-search-delay!, use default value...");
            energyCoreSearchDelay = 100;
            maxEnergyCoreSearchDelay = 300;
        }
    }

    @Override
    public String getName() {
        return name().toLowerCase();
    }

    @Nonnull
    public String getUnlocalizedEnergyDescriptor() {
        return "tooltip.ic2.powertier." + ic2EnergyTier + ".name";
    }

    // MM only supports GTCE tiers from ULV to UV
    public int getGTEnergyTier() {
        return MathHelper.clamp(this.gtEnergyTier, 0, GTValues.VN.length - 1);
    }

    public int getGtAmperage() {
        return this.gtAmperage;
    }

    @Optional.Method(modid = "gregtech")
    public String getUnlocalizedGTEnergyTier() {
        return GTValues.VN[getGTEnergyTier()];
    }

    public long getGTEnergyTransferVoltage() {
        if (getGTEnergyTier() < 0) {
            return -1;
        }
        return (int) Math.pow(2, ((getGTEnergyTier() + 1) * 2) + 1);
    }

    public int getIC2EnergyTransmission() {
        if (ic2EnergyTier < 0) {
            return -1;
        }
        return (int) Math.pow(2, ((ic2EnergyTier + 1) * 2) + 1);
    }

}
