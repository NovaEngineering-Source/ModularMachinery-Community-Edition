package kport.modularmagic.common.crafting.requirement;

import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentOutputRestrictor;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.helper.CraftCheck;
import hellfirepvp.modularmachinery.common.crafting.helper.ProcessingComponent;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.lib.RegistriesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.Asyncable;
import hellfirepvp.modularmachinery.common.util.ResultChance;
import kport.modularmagic.common.crafting.component.ComponentGrid;
import kport.modularmagic.common.crafting.requirement.types.ModularMagicRequirements;
import kport.modularmagic.common.crafting.requirement.types.RequirementTypeGrid;
import kport.modularmagic.common.integration.jei.component.JEIComponentGrid;
import kport.modularmagic.common.integration.jei.ingredient.Grid;
import kport.modularmagic.common.tile.TileGridProvider;
import kport.modularmagic.common.tile.machinecomponent.MachineComponentGridProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class RequirementGrid extends ComponentRequirement.PerTick<Grid, RequirementTypeGrid> implements Asyncable {

    public float power;

    public RequirementGrid(IOType actionType, float power) {
        super((RequirementTypeGrid) RegistriesMM.REQUIREMENT_TYPE_REGISTRY.getValue(ModularMagicRequirements.KEY_REQUIREMENT_GRID), actionType);
        this.power = power;
    }

    @Override
    public boolean isValidComponent(ProcessingComponent<?> component, RecipeCraftingContext ctx) {
        MachineComponent<?> cmp = component.getComponent();
        return cmp.getComponentType() instanceof ComponentGrid &&
            cmp instanceof MachineComponentGridProvider &&
            cmp.ioType == getActionType();
    }

    @Nonnull
    @Override
    public CraftCheck doIOTick(ProcessingComponent<?> component, RecipeCraftingContext context) {
        TileGridProvider provider = (TileGridProvider) component.getComponent().getContainerProvider();
        switch (getActionType()) {
            case OUTPUT:
                provider.setPower(-this.power);
            case INPUT:
                provider.setPower(this.power);
        }
        return CraftCheck.success();
    }

    @Override
    public boolean startCrafting(ProcessingComponent<?> component, RecipeCraftingContext context, ResultChance chance) {
        return canStartCrafting(component, context, Lists.newArrayList()).isSuccess();
    }

    @Nonnull
    @Override
    public CraftCheck canStartCrafting(ProcessingComponent<?> component, RecipeCraftingContext context, List<ComponentOutputRestrictor> restrictions) {
        TileGridProvider provider = (TileGridProvider) component.getComponent().getContainerProvider();

        if (getActionType() == IOType.INPUT && provider.getFreq().getPowerCreated() - provider.getFreq().getPowerDrain() < this.power) {
            return CraftCheck.failure("error.modularmachinery.requirement.grid.less");
        } else {
            return CraftCheck.success();
        }
    }

    @Nonnull
    @Override
    public String getMissingComponentErrorMessage(IOType ioType) {
        return "error.modularmachinery.component.invalid";
    }

    @Override
    public RequirementGrid deepCopy() {
        return deepCopyModified(Collections.emptyList());
    }

    @Override
    public RequirementGrid deepCopyModified(List<RecipeModifier> list) {
        float power = RecipeModifier.applyModifiers(list, this, this.power, false);
        return new RequirementGrid(actionType, power);
    }

    @Override
    public JEIComponentGrid provideJEIComponent() {
        return new JEIComponentGrid(this);
    }
}
