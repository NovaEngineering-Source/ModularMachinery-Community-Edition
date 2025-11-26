package hellfirepvp.modularmachinery.common.integration;

import github.kasuminova.mmce.common.integration.theoneprobe.MachineryHatchInfoProvider;
import hellfirepvp.modularmachinery.common.integration.theoneprobe.MMInfoProvider;
import mcjty.theoneprobe.TheOneProbe;
import mcjty.theoneprobe.apiimpl.TheOneProbeImp;
import net.minecraftforge.common.config.Configuration;

public class ModIntegrationTOP {
    public static int     recipeProgressBarFilledColor           = 0xCC54FF9F;
    public static int     failureProgressBarFilledColor          = 0xCCFF4500;
    public static int     recipeProgressBarAlternateFilledColor  = 0xCC4EEE94;
    public static int     failureProgressBarAlternateFilledColor = 0xCCFF6347;
    public static int     recipeProgressBarBorderColor           = 0xCC43CD80;
    public static int     failureProgressBarBorderColor          = 0xCCFF4500;
    public static int     recipeProgressBarBackgroundColor       = 0xFF000000;
    public static boolean showRecipeProgressBarDecimalPoints     = true;
    public static boolean showParallelControllerInfo             = true;

    public static void registerProviders() {
        TheOneProbeImp top = TheOneProbe.theOneProbeImp;

        top.registerProvider(new MMInfoProvider());
        top.registerProvider(new MachineryHatchInfoProvider());
    }

    public static void loadFromConfig(Configuration cfg) {
        recipeProgressBarFilledColor = Integer.parseUnsignedInt(
            cfg.getString("RECIPE_PROGRESSBAR_FILLED_COLOR", "display.theoneprobe",
                "CC54FF9F", "Machine progressbar filled color if TheOneProbe mod is installed."),
            16);

        recipeProgressBarAlternateFilledColor = Integer.parseUnsignedInt(
            cfg.getString("RECIPE_PROGRESSBAR_ALTERNATE_FILLED_COLOR", "display.theoneprobe",
                "CC4EEE94", "Machine progressbar filled color if TheOneProbe mod is installed."),
            16);

        recipeProgressBarBorderColor = Integer.parseUnsignedInt(
            cfg.getString("RECIPE_PROGRESSBAR_BORDER_COLOR", "display.theoneprobe",
                "CC43CD80", "Machine progressbar filled color if TheOneProbe mod is installed."),
            16);

        recipeProgressBarBackgroundColor = Integer.parseUnsignedInt(
            cfg.getString("RECIPE_PROGRESSBAR_BACKGROUND_COLOR", "display.theoneprobe",
                "FF000000", "Machine progressbar filled color if TheOneProbe mod is installed."),
            16);

        failureProgressBarFilledColor = Integer.parseUnsignedInt(
            cfg.getString("FAILURE_PROGRESSBAR_FILLED_COLOR", "display.theoneprobe",
                "CCFF6347", "Similar to RECIPE_PROGRESSBAR_FILLED_COLOR, but only displayed if it fails in the middle of a mechanical run."),
            16);

        failureProgressBarAlternateFilledColor = Integer.parseUnsignedInt(
            cfg.getString("FAILURE_PROGRESSBAR_ALTERNATE_FILLED_COLOR", "display.theoneprobe",
                "CCFF4500", "Similar to RECIPE_PROGRESSBAR_ALTERNATE_FILLED_COLOR, but only displayed if it fails in the middle of a mechanical run."),
            16);

        failureProgressBarBorderColor = Integer.parseUnsignedInt(
            cfg.getString("FAILURE_PROGRESSBAR_BORDER_COLOR", "display.theoneprobe",
                "CCFF6347", "Similar to RECIPE_PROGRESSBAR_BACKGROUND_COLOR, but only displayed if it fails in the middle of a mechanical run."),
            16);

        showRecipeProgressBarDecimalPoints = cfg.getBoolean("SHOW_RECIPE_PROGRESSBAR_DECIMAL_POINTS",
            "display.theoneprobe", true,
            "Show recipe progressbar decimal points if TheOneProbe mod is installed.");

        showParallelControllerInfo = cfg.getBoolean("SHOW_PARALLEL_CONTROLLER_INFO", "display.theoneprobe",
            true, "Show parallel controller info if TheOneProbe mod is installed.");
    }
}
