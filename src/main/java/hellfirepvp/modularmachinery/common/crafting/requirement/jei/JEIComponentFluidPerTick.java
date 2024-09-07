package hellfirepvp.modularmachinery.common.crafting.requirement.jei;

import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementFluidPerTick;
import hellfirepvp.modularmachinery.common.integration.recipe.RecipeLayoutPart;
import hellfirepvp.modularmachinery.common.machine.IOType;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fluids.FluidStack;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class JEIComponentFluidPerTick extends ComponentRequirement.JEIComponent<FluidStack> {
    private final RequirementFluidPerTick requirement;

    public JEIComponentFluidPerTick(RequirementFluidPerTick requirement) {
        this.requirement = requirement;
    }

    @Override
    public Class<FluidStack> getJEIRequirementClass() {
        return FluidStack.class;
    }

    @Override
    public List<FluidStack> getJEIIORequirements() {
        return Collections.singletonList(requirement.required.copy());
    }

    @Override
    public RecipeLayoutPart<FluidStack> getLayoutPart(Point offset) {
        return new RecipeLayoutPart.FluidTank(offset);
    }

    @Override
    public void onJEIHoverTooltip(int slotIndex, boolean input, FluidStack ingredient, List<String> tooltip) {
        IOType ioType = requirement.getActionType();
        if (ioType == IOType.INPUT) {
            tooltip.add(I18n.format("tooltip.fluid_pertick.in", ingredient.amount));
        } else if (ioType == IOType.OUTPUT) {
            tooltip.add(I18n.format("tooltip.fluid_pertick.out", ingredient.amount));
        }
    }
}
