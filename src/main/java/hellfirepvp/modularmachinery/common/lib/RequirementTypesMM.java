/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.lib;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.*;
import net.minecraft.util.ResourceLocation;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RequirementTypesMM
 * Created by HellFirePvP
 * Date: 13.07.2019 / 09:02
 */
public class RequirementTypesMM {

    public static final ResourceLocation KEY_REQUIREMENT_ITEM = new ResourceLocation(ModularMachinery.MODID, "item");
    public static final ResourceLocation KEY_REQUIREMENT_ITEM_DURABILITY = new ResourceLocation(ModularMachinery.MODID, "item_durability");
    public static final ResourceLocation KEY_REQUIREMENT_INGREDIENT_ARRAY = new ResourceLocation(ModularMachinery.MODID, "ingredient_array_input");
    public static final ResourceLocation KEY_REQUIREMENT_FLUID = new ResourceLocation(ModularMachinery.MODID, "fluid");
    public static final ResourceLocation KEY_REQUIREMENT_FLUID_PERTICK = new ResourceLocation(ModularMachinery.MODID, "fluid_pertick");
    public static final ResourceLocation KEY_REQUIREMENT_GAS = new ResourceLocation(ModularMachinery.MODID, "gas");
    public static final ResourceLocation KEY_REQUIREMENT_GAS_PERTICK = new ResourceLocation(ModularMachinery.MODID, "gas_pertick");
    public static final ResourceLocation KEY_REQUIREMENT_ENERGY = new ResourceLocation(ModularMachinery.MODID, "energy");
    //Helper type as target for duration-type recipe modifiers
    public static final ResourceLocation KEY_REQUIREMENT_DURATION = new ResourceLocation(ModularMachinery.MODID, "duration");
    public static final ResourceLocation KEY_INTERFACE_NUMBER_INPUT = new ResourceLocation(ModularMachinery.MODID, "interface_number_input");
    public static RequirementTypeItem REQUIREMENT_ITEM;
    public static RequirementTypeItemDurability REQUIREMENT_ITEM_DURABILITY;
    public static RequirementTypeIngredientArray REQUIREMENT_INGREDIENT_ARRAY;
    public static RequirementTypeFluid REQUIREMENT_FLUID;
    public static RequirementTypeFluidPerTick REQUIREMENT_FLUID_PERTICK;
    public static RequirementTypeGasPerTick REQUIREMENT_GAS_PERTICK;
    public static RequirementTypeEnergy REQUIREMENT_ENERGY;
    public static RequirementTypeGas REQUIREMENT_GAS;
    public static RequirementDuration REQUIREMENT_DURATION;
    public static RequirementTypeInterfaceNumInput REQUIREMENT_INTERFACE_NUMBER_INPUT;
    private RequirementTypesMM() {
    }

}
