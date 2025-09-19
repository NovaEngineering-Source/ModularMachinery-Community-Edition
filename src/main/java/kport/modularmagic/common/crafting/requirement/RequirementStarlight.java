package kport.modularmagic.common.crafting.requirement;

import hellfirepvp.modularmachinery.ModularMachinery;
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
import kport.modularmagic.common.crafting.component.ComponentStarlight;
import kport.modularmagic.common.crafting.requirement.types.ModularMagicRequirements;
import kport.modularmagic.common.crafting.requirement.types.RequirementTypeStarlight;
import kport.modularmagic.common.integration.jei.component.JEIComponentStarlight;
import kport.modularmagic.common.integration.jei.ingredient.Starlight;
import kport.modularmagic.common.tile.TileStarlightInput;
import kport.modularmagic.common.tile.TileStarlightOutput;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class RequirementStarlight extends ComponentRequirement.PerTick<Starlight, RequirementTypeStarlight> implements Asyncable {

    public float starlightAmount;

    public RequirementStarlight(IOType actionType, float starlightAmount) {
        super((RequirementTypeStarlight) RegistriesMM.REQUIREMENT_TYPE_REGISTRY.getValue(ModularMagicRequirements.KEY_REQUIREMENT_STARLIGHT), actionType);
        this.starlightAmount = starlightAmount;
    }

    @Override
    public boolean isValidComponent(ProcessingComponent<?> component, RecipeCraftingContext ctx) {
        MachineComponent<?> cpn = component.getComponent();
        return (cpn.getContainerProvider() instanceof TileStarlightInput || cpn.getContainerProvider() instanceof TileStarlightOutput) &&
            cpn.getComponentType() instanceof ComponentStarlight &&
            cpn.ioType == getActionType();
    }

    @Nonnull
    @Override
    public CraftCheck doIOTick(ProcessingComponent<?> component, RecipeCraftingContext context) {
        if (getActionType() == IOType.OUTPUT) {
            ModularMachinery.EXECUTE_MANAGER.addSyncTask(() ->
                ((TileStarlightOutput) component.getComponent().getContainerProvider())
                    .setStarlightProduced(this.starlightAmount / 4000));
        }
        return CraftCheck.success();
    }

    @Nonnull
    @Override
    public CraftCheck canStartCrafting(ProcessingComponent<?> component, RecipeCraftingContext context, List<ComponentOutputRestrictor> restrictions) {
        if (getActionType() == IOType.INPUT) {
            TileStarlightInput provider = (TileStarlightInput) component.getComponent().getContainerProvider();
            return provider.getStarlightStored() >= this.starlightAmount ? CraftCheck.success() : CraftCheck.failure("error.modularmachinery.requirement.starlight.less");
        }
        return CraftCheck.success();
    }

    @Nonnull
    @Override
    public String getMissingComponentErrorMessage(IOType ioType) {
        return "error.modularmachinery.component.invalid";
    }

    @Override
    public RequirementStarlight deepCopy() {
        return deepCopyModified(Collections.emptyList());
    }

    @Override
    public RequirementStarlight deepCopyModified(List<RecipeModifier> list) {
        float starlightAmount = RecipeModifier.applyModifiers(list, this, this.starlightAmount, false);
        return new RequirementStarlight(actionType, starlightAmount);
    }

    @Override
    public JEIComponentStarlight provideJEIComponent() {
        return new JEIComponentStarlight(this);
    }
}
