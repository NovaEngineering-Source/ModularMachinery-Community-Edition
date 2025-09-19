package hellfirepvp.modularmachinery.common.crafting.requirement;

import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.helper.CraftCheck;
import hellfirepvp.modularmachinery.common.crafting.helper.ProcessingComponent;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementTypeItemDurability;
import hellfirepvp.modularmachinery.common.lib.ComponentTypesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.Asyncable;
import hellfirepvp.modularmachinery.common.util.ResultChance;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class RequirementItemDurability extends ComponentRequirement.MultiCompParallelizable<ItemStack, RequirementTypeItemDurability>
    implements ComponentRequirement.ChancedRequirement, ComponentRequirement.Parallelizable, Asyncable {

    public RequirementItemDurability(final RequirementTypeItemDurability requirementType, final IOType actionType) {
        super(requirementType, actionType);
    }

    @Override
    public boolean isValidComponent(ProcessingComponent<?> component, RecipeCraftingContext ctx) {
        MachineComponent<?> cmp = component.component();
        ComponentType cmpType = cmp.getComponentType();
        return (cmpType.equals(ComponentTypesMM.COMPONENT_ITEM) || cmpType.equals(ComponentTypesMM.COMPONENT_ITEM_FLUID_GAS))
            && cmp.ioType == actionType;
    }

    @Override
    public ComponentRequirement<ItemStack, RequirementTypeItemDurability> deepCopy() {
        return null;
    }

    @Override
    public ComponentRequirement<ItemStack, RequirementTypeItemDurability> deepCopyModified(final List<RecipeModifier> modifiers) {
        return null;
    }

    @Nonnull
    @Override
    public String getMissingComponentErrorMessage(final IOType ioType) {
        return "";
    }

    @Override
    public JEIComponent<ItemStack> provideJEIComponent() {
        return null;
    }

    @Override
    public void setChance(final float chance) {

    }

    @Override
    public int getMaxParallelism(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context, final int maxParallelism) {
        return 0;
    }

    @Nonnull
    @Override
    public List<ProcessingComponent<?>> copyComponents(final List<ProcessingComponent<?>> components) {
        return Collections.emptyList();
    }

    @Nonnull
    @Override
    public CraftCheck canStartCrafting(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context) {
        return null;
    }

    @Override
    public void startCrafting(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context, final ResultChance chance) {
        super.startCrafting(components, context, chance);
    }

    @Override
    public void finishCrafting(final List<ProcessingComponent<?>> components, final RecipeCraftingContext context, final ResultChance chance) {
        super.finishCrafting(components, context, chance);
    }
}
