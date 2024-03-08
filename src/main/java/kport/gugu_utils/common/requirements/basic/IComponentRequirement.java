package kport.gugu_utils.common.requirements.basic;

import hellfirepvp.modularmachinery.common.crafting.helper.*;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import hellfirepvp.modularmachinery.common.util.ResultChance;

import javax.annotation.Nonnull;
import java.util.List;

public interface IComponentRequirement<T, V> {
    V getRequirementType();

    IOType getActionType();

    void setTag(ComponentSelectorTag tag);

    ComponentSelectorTag getTag();

    int getSortingWeight();

    boolean isValidComponent(ProcessingComponent<?> var1, RecipeCraftingContext var2);

    boolean startCrafting(ProcessingComponent<?> var1, RecipeCraftingContext var2, ResultChance var3);

    @Nonnull
    CraftCheck finishCrafting(ProcessingComponent<?> var1, RecipeCraftingContext var2, ResultChance var3);

    @Nonnull
    CraftCheck canStartCrafting(ProcessingComponent<?> var1, RecipeCraftingContext var2, List<ComponentOutputRestrictor> var3);

    ComponentRequirement deepCopy();

    ComponentRequirement deepCopyModified(List<RecipeModifier> var1);

    void startRequirementCheck(ResultChance var1, RecipeCraftingContext var2);

    void endRequirementCheck();

    @Nonnull
    String getMissingComponentErrorMessage(IOType var1);

    ComponentRequirement.JEIComponent<T> provideJEIComponent();

}