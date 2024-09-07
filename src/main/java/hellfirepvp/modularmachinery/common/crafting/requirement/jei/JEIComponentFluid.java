/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.requirement.jei;

import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementFluid;
import hellfirepvp.modularmachinery.common.integration.recipe.RecipeLayoutPart;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.util.Collections;
import java.util.List;

/**
 * This class is part of the Fracture Mod
 * The complete source code for this mod can be found on github.
 * Class: JEIComponentFluid
 * Created by HellFirePvP
 * Date: 08.04.2018 / 12:49
 */
public class JEIComponentFluid extends ComponentRequirement.JEIComponent<FluidStack> {

    private final RequirementFluid requirement;

    public JEIComponentFluid(RequirementFluid requirement) {
        this.requirement = requirement;
    }

    @Override
    public Class<FluidStack> getJEIRequirementClass() {
        return FluidStack.class;
    }

    @Override
    public List<FluidStack> getJEIIORequirements() {
        return Collections.singletonList(requirement.required);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public RecipeLayoutPart<FluidStack> getLayoutPart(Point offset) {
        return new RecipeLayoutPart.FluidTank(offset);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onJEIHoverTooltip(int slotIndex, boolean input, FluidStack ingredient, List<String> tooltip) {
        JEIComponentItem.addChanceTooltip(input, tooltip, requirement.chance);
    }
}
