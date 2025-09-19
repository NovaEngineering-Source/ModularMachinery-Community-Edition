package kport.modularmagic.common.crafting.requirement;

import WayofTime.bloodmagic.soul.EnumDemonWillType;
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
import hellfirepvp.modularmachinery.common.util.ResultChance;
import kport.modularmagic.common.crafting.component.ComponentWill;
import kport.modularmagic.common.crafting.requirement.types.ModularMagicRequirements;
import kport.modularmagic.common.crafting.requirement.types.RequirementTypeWill;
import kport.modularmagic.common.integration.jei.component.JEIComponentWill;
import kport.modularmagic.common.integration.jei.ingredient.DemonWill;
import kport.modularmagic.common.tile.TileWillProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class RequirementWill extends ComponentRequirement<DemonWill, RequirementTypeWill> implements Asyncable {

    public double            willAmount;
    public EnumDemonWillType willType;
    public double            min;
    public double            max;

    public RequirementWill(IOType actionType, double willRequired, EnumDemonWillType willType, double min, double max) {
        super((RequirementTypeWill) RegistriesMM.REQUIREMENT_TYPE_REGISTRY.getValue(ModularMagicRequirements.KEY_REQUIREMENT_WILL), actionType);
        this.willAmount = willRequired;
        this.willType = willType;
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean isValidComponent(ProcessingComponent<?> component, RecipeCraftingContext ctx) {
        MachineComponent<?> cpn = component.getComponent();
        return cpn.getContainerProvider() instanceof TileWillProvider &&
            cpn.getComponentType() instanceof ComponentWill &&
            cpn.ioType == getActionType();
    }

    @Override
    public boolean startCrafting(ProcessingComponent<?> component, RecipeCraftingContext context, ResultChance chance) {
        if (!canStartCrafting(component, context, Collections.emptyList()).isSuccess()) {
            return false;
        }

        if (getActionType() == IOType.INPUT) {
            TileWillProvider willProvider = (TileWillProvider) component.getComponent().getContainerProvider();
            ModularMachinery.EXECUTE_MANAGER.addSyncTask(() -> willProvider.removeWill(willAmount, willType));
        }
        return true;
    }

    @Nonnull
    @Override
    public CraftCheck finishCrafting(ProcessingComponent<?> component, RecipeCraftingContext context, ResultChance chance) {
        if (getActionType() == IOType.OUTPUT) {
            TileWillProvider willProvider = (TileWillProvider) component.getComponent().getContainerProvider();
            ModularMachinery.EXECUTE_MANAGER.addSyncTask(() -> willProvider.addWill(willAmount, willType));
        }
        return CraftCheck.success();
    }

    @Nonnull
    @Override
    public CraftCheck canStartCrafting(ProcessingComponent<?> component, RecipeCraftingContext context, List<ComponentOutputRestrictor> restrictions) {
        TileWillProvider willProvider = (TileWillProvider) component.getComponent().getContainerProvider();
        switch (getActionType()) {
            case INPUT -> {
                if (willProvider.getWill(this.willType) - this.willAmount < this.min) {
                    return CraftCheck.failure("error.modularmachinery.requirement.will.less");
                }
            }
            case OUTPUT -> {
                if (willProvider.getWill(this.willType) - this.willAmount > this.max) {
                    return CraftCheck.failure("error.modularmachinery.requirement.will.more");
                }
            }
        }
        return CraftCheck.success();
    }

    @Nonnull
    @Override
    public String getMissingComponentErrorMessage(IOType ioType) {
        return "error.modularmachinery.component.invalid";
    }

    @Override
    public RequirementWill deepCopy() {
        return this;
    }

    @Override
    public RequirementWill deepCopyModified(List<RecipeModifier> list) {
        return this;
    }

    @Override
    public JEIComponentWill provideJEIComponent() {
        return new JEIComponentWill(this);
    }
}
