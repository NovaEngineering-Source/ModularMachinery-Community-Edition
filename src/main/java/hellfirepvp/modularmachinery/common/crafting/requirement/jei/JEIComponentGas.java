package hellfirepvp.modularmachinery.common.crafting.requirement.jei;

import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementGas;
import hellfirepvp.modularmachinery.common.integration.recipe.RecipeLayoutPart;
import mekanism.api.gas.GasStack;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class JEIComponentGas extends ComponentRequirement.JEIComponent<GasStack> {

    private final RequirementGas requirement;

    public JEIComponentGas(RequirementGas requirement) {
        this.requirement = requirement;
    }

    @Override
    public Class<GasStack> getJEIRequirementClass() {
        return GasStack.class;
    }

    @Override
    public List<GasStack> getJEIIORequirements() {
        return Collections.singletonList(requirement.required);
    }

    @Override
    public RecipeLayoutPart<GasStack> getLayoutPart(Point offset) {
        return new RecipeLayoutPart.GasTank(offset);
    }

    @Override
    public void onJEIHoverTooltip(int slotIndex, boolean input, GasStack ingredient, List<String> tooltip) {
        JEIComponentItem.addChanceTooltip(input, tooltip, requirement.chance);
    }
}
