/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.lib;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.crafting.tooltip.RequirementTip;
import net.minecraft.util.ResourceLocation;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: RequirementTipsMM
 * Created by HellFirePvP
 * Date: 14.07.2019 / 19:13
 */
public class RequirementTipsMM {

    public static final ResourceLocation TIP_ENERGY_INPUT_NAME                 = new ResourceLocation(ModularMachinery.MODID, "energyin");
    public static final ResourceLocation TIP_ENERGY_OUTPUT_NAME                = new ResourceLocation(ModularMachinery.MODID, "energyout");
    public static final ResourceLocation TIP_FUEL_INPUT_NAME                   = new ResourceLocation(ModularMachinery.MODID, "fuelin");
    public static final ResourceLocation TIP_SMART_INTERFACE_NUMBER_INPUT_NAME = new ResourceLocation(ModularMachinery.MODID, "smart_interface_number_input");
    public static       RequirementTip   TIP_ENERGY_INPUT;
    public static       RequirementTip   TIP_ENERGY_OUTPUT;
    public static       RequirementTip   TIP_FUEL_INPUT;
    public static       RequirementTip   TIP_SMART_INTERFACE_NUMBER_INPUT;

    private RequirementTipsMM() {
    }

}
