/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.data;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.client.util.EnergyDisplayUtil;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.block.prop.EnergyHatchData;
import hellfirepvp.modularmachinery.common.block.prop.FluidHatchSize;
import hellfirepvp.modularmachinery.common.block.prop.ParallelControllerData;
import hellfirepvp.modularmachinery.common.block.prop.UpgradeBusData;
import hellfirepvp.modularmachinery.common.integration.ModIntegrationTOP;
import hellfirepvp.modularmachinery.common.machine.RecipeFailureActions;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import ink.ikx.mmce.core.AssemblyConfig;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: Config
 * Created by HellFirePvP
 * Date: 26.06.2017 / 22:34
 */
public class Config {
    public static boolean mocCompatibleMode = false;
    public static boolean onlyOneMachineController = false;
    public static boolean disableMocDeprecatedTip = false;
    public static boolean machineParallelizeEnabledByDefault = true;
    public static boolean recipeParallelizeEnabledByDefault = true;
    public static boolean enableFluxNetworksIntegration = true;
    public static boolean enableFactoryControllerByDefault = false;
    public static boolean controllerOutputComparatorSignal = true;
    public static boolean enableStructurePreviewDisplayList = false;
    public static boolean enableDurationMultiplier = true;
    public static int machineColor;
    public static int maxMachineParallelism = 2048;
    public static int defaultFactoryMaxThread = 20;

    private static File lastReadFile;
    private static Configuration lastReadConfig;

    @net.minecraftforge.common.config.Config.Comment("Max aspect in aspect output hatch")
    @net.minecraftforge.common.config.Config.RangeInt(min = 1)
    public static int ASPECT_OUTPUT_HATCH_MAX_STORAGE = 500;

    @net.minecraftforge.common.config.Config.Comment("Actions when a aspect output hatch are full, can be 'spill_random', 'spill_all' and 'halt'.")
    public static String ASPECT_OUTPUT_HATCH_FULL_ACTION = "spill_random";


    public static void loadFrom(File file) {
        lastReadFile = file;
        lastReadConfig = new Configuration(file);

        load();

        if (lastReadConfig.hasChanged()) {
            lastReadConfig.save();
        }
    }

    private static void load() {
        FluidHatchSize.loadFromConfig(lastReadConfig);
        EnergyHatchData.loadFromConfig(lastReadConfig);
        ParallelControllerData.loadFromConfig(lastReadConfig);
        UpgradeBusData.loadFromConfig(lastReadConfig);
        EnergyDisplayUtil.loadFromConfig(lastReadConfig);
        RecipeFailureActions.loadFromConfig(lastReadConfig);
        TileMultiblockMachineController.loadFromConfig(lastReadConfig);
        AssemblyConfig.loadFormConfig(lastReadConfig);
        if (Mods.TOP.isPresent()) {
            ModIntegrationTOP.loadFromConfig(lastReadConfig);
        }

        String strColor = lastReadConfig.getString("general-casing-color", "general", "FFFFFF", "Defines the _default_ color for machine casings as items or blocks. (Hex color without alpha) Has to be defined both server and clientside!");
        int col = 0xff921e; //TODO uh
        try {
            col = Integer.parseInt(strColor, 16);
        } catch (Exception exc) {
            ModularMachinery.log.error("Machine-Casing color defined in the config is not a hex color: " + strColor);
            ModularMachinery.log.error("Using default color instead...");
        }

        // General
        machineColor = col;
        onlyOneMachineController = lastReadConfig.getBoolean("only-one-machine-controller", "general", false,
                "When enabled, Modules no longer register a separate controller for each machine, and the modular-controller-compatible-mode option is turned off.");
        enableDurationMultiplier = lastReadConfig.getBoolean("enable-duration-multiplier", "general", false,
                "If enabled, and the RecipeModifier modifies the recipe duration, certain requirements (e.g., energy) will change over time.");

        // Client
        enableStructurePreviewDisplayList = lastReadConfig.getBoolean("enable-structure-preview-display-list", "client", false,
                "(Experimental) When enabled, Machinery Preview attempts to use optimized rendering to dramatically improve smoothness in the case of large machinery, note that this can cause potential issues such as rendering misalignment.");

        // Modular Controller Merge Support
        mocCompatibleMode = lastReadConfig.getBoolean(
                "modular-controller-compatible-mode", "general", false,
                "When enabled, the mod registers a controller block under the name modularcontroller to prevent incompatibility with older saves.");
        disableMocDeprecatedTip = lastReadConfig.getBoolean(
                "disable-moc-deprecated-tip", "general", false,
                "Disable the ModularController is deprecated tooltip.");

        // FluxNetworks Integration
        enableFluxNetworksIntegration = lastReadConfig.getBoolean("enable-fluxnetworks-integration", "general", true,
                "When enabled, allows you to use the flux network to transfer larger amounts of energy than 2147483647.");

        // Parallelize Feature
        machineParallelizeEnabledByDefault = lastReadConfig.getBoolean("machine-parallelize-enabled-bydefault",
                "parallel-controller", true, "Whether the machine parallel recipe processing is enabled by default.");
        recipeParallelizeEnabledByDefault = lastReadConfig.getBoolean("recipe-parallelize-enabled-bydefault",
                "parallel-controller", true, "Whether parallel recipe processing is enabled by default.");
        maxMachineParallelism = lastReadConfig.getInt("max-machine-parallelism", "parallel-controller",
                2048, 1, Integer.MAX_VALUE, "The default max number of parallelism for a machine.");

        // Factory System
        enableFactoryControllerByDefault = lastReadConfig.getBoolean("enable-factory-controller-bydefault",
                "factory-system", false, "If enabled, the mod will register the factory system controller for all machines by default.");
        defaultFactoryMaxThread = lastReadConfig.getInt("default-factory-max-thread", "factory-system",
                10, 1, 100,
                "Sets the maximum number of threads in the factory system by default.");
    }
}
