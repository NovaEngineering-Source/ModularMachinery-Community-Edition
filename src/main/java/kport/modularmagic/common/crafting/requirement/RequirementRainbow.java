package kport.modularmagic.common.crafting.requirement;

import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.helper.CraftCheck;
import hellfirepvp.modularmachinery.common.crafting.helper.ProcessingComponent;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.lib.RegistriesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.Asyncable;
import kport.modularmagic.common.crafting.component.ComponentRainbow;
import kport.modularmagic.common.crafting.requirement.types.ModularMagicRequirements;
import kport.modularmagic.common.crafting.requirement.types.RequirementTypeRainbow;
import kport.modularmagic.common.integration.jei.component.JEIComponentRainbow;
import kport.modularmagic.common.integration.jei.ingredient.Rainbow;
import kport.modularmagic.common.tile.TileRainbowProvider;

import javax.annotation.Nonnull;
import java.util.List;

public class RequirementRainbow extends ComponentRequirement.PerTickMultiComponent<Rainbow, RequirementTypeRainbow> implements Asyncable {

    public RequirementRainbow() {
        super((RequirementTypeRainbow) RegistriesMM.REQUIREMENT_TYPE_REGISTRY.getValue(ModularMagicRequirements.KEY_REQUIREMENT_RAINBOW), IOType.INPUT);
    }

    @Override
    public boolean isValidComponent(ProcessingComponent<?> component, RecipeCraftingContext ctx) {
        MachineComponent<?> cpn = component.getComponent();
        return cpn.getContainerProvider() instanceof TileRainbowProvider &&
            cpn.getComponentType() instanceof ComponentRainbow &&
            cpn.ioType == getActionType();
    }

    @Nonnull
    @Override
    public List<ProcessingComponent<?>> copyComponents(final List<ProcessingComponent<?>> components) {
        return components;
    }

    @Nonnull
    @Override
    public CraftCheck canStartCrafting(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context) {
        for (final ProcessingComponent<?> component : components) {
            TileRainbowProvider provider = (TileRainbowProvider) component.getComponent().getContainerProvider();
            if (provider.rainbow()) {
                return CraftCheck.success();
            }
        }
        return CraftCheck.failure("error.modularmachinery.requirement.rainbow.less");
    }

    @Override
    public CraftCheck doIOTick(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context, final float durationMultiplier) {
        return canStartCrafting(components, context);
    }

    @Override
    public ComponentRequirement<Rainbow, RequirementTypeRainbow> deepCopy() {
        return new RequirementRainbow();
    }

    @Override
    public ComponentRequirement<Rainbow, RequirementTypeRainbow> deepCopyModified(List<RecipeModifier> modifiers) {
        return new RequirementRainbow();
    }

    @Nonnull
    @Override
    public String getMissingComponentErrorMessage(IOType ioType) {
        return "error.modularmachinery.component.invalid";
    }

    @Override
    public JEIComponent<Rainbow> provideJEIComponent() {
        return new JEIComponentRainbow();
    }
}
