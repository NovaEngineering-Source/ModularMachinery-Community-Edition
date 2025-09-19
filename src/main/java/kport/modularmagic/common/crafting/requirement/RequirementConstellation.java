package kport.modularmagic.common.crafting.requirement;

import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.helper.CraftCheck;
import hellfirepvp.modularmachinery.common.crafting.helper.ProcessingComponent;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.lib.RegistriesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.Asyncable;
import kport.modularmagic.common.crafting.component.ComponentConstellation;
import kport.modularmagic.common.crafting.requirement.types.ModularMagicRequirements;
import kport.modularmagic.common.crafting.requirement.types.RequirementTypeConstellation;
import kport.modularmagic.common.integration.jei.component.JEIComponentConstellation;
import kport.modularmagic.common.integration.jei.ingredient.Constellation;
import kport.modularmagic.common.tile.TileConstellationProvider;
import kport.modularmagic.common.tile.machinecomponent.MachineComponentConstellationProvider;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class RequirementConstellation extends ComponentRequirement.MultiComponentRequirement<Constellation, RequirementTypeConstellation> implements Asyncable {

    public IConstellation constellation;

    public RequirementConstellation(IOType actionType, IConstellation constellation) {
        super((RequirementTypeConstellation) RegistriesMM.REQUIREMENT_TYPE_REGISTRY.getValue(ModularMagicRequirements.KEY_REQUIREMENT_CONSTELLATION), actionType);
        this.constellation = constellation;
    }

    @Override
    public boolean isValidComponent(ProcessingComponent<?> component, RecipeCraftingContext ctx) {
        MachineComponent<?> cmp = component.getComponent();
        return cmp.getComponentType() instanceof ComponentConstellation &&
            cmp instanceof MachineComponentConstellationProvider &&
            cmp.ioType == getActionType();
    }

    @Nonnull
    @Override
    public List<ProcessingComponent<?>> copyComponents(final List<ProcessingComponent<?>> components) {
        return components;
    }

    @Nonnull
    @Override
    public CraftCheck canStartCrafting(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context) {
        if (getActionType() == IOType.OUTPUT) {
            return CraftCheck.failure("error.modularmachinery.requirement.invalid");
        }
        for (final ProcessingComponent<?> component : components) {
            TileConstellationProvider provider = (TileConstellationProvider) component.getComponent().getContainerProvider();
            if (provider.isConstellationInSky(constellation)) {
                return CraftCheck.success();
            }
        }
        return CraftCheck.failure("error.modularmachinery.requirement.constellation.less");
    }

    @Nonnull
    @Override
    public String getMissingComponentErrorMessage(IOType ioType) {
        return "error.modularmachinery.requirement.constellation.missingprovider";
    }

    @Override
    public RequirementConstellation deepCopy() {
        return deepCopyModified(Collections.emptyList());
    }

    @Override
    public RequirementConstellation deepCopyModified(List<RecipeModifier> list) {
        return new RequirementConstellation(actionType, constellation);
    }

    @Override
    public JEIComponentConstellation provideJEIComponent() {
        return new JEIComponentConstellation(this);
    }
}