package kport.gugu_utils.common.requirements.basic;

import hellfirepvp.modularmachinery.common.crafting.helper.ComponentRequirement;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementType;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import kport.gugu_utils.common.requirements.types.RequirementTypeAdapter;

import java.util.List;

public abstract class ComponentRequirementAdapter<T> extends ComponentRequirement<T, RequirementType<T, ? extends ComponentRequirementAdapter<T>>> implements IComponentRequirement<T, RequirementType<T, ? extends ComponentRequirementAdapter<T>>> {


    @SuppressWarnings("unchecked")
    public ComponentRequirementAdapter(RequirementTypeAdapter<T> requirementType, IOType actionType) {
        super((RequirementType) requirementType, actionType);
    }

    public abstract ComponentRequirementAdapter<T> deepClone();

    public abstract ComponentRequirementAdapter<T> deepCloneModified(List<RecipeModifier> modifiers);

    @Override
    public final ComponentRequirement<T, RequirementType<T, ? extends ComponentRequirementAdapter<T>>> deepCopy() {
        return deepClone();
    }

    @Override
    public final ComponentRequirement<T, RequirementType<T, ? extends ComponentRequirementAdapter<T>>> deepCopyModified(List<RecipeModifier> list) {
        return deepCloneModified(list);
    }

    public abstract static class PerTick<T> extends ComponentRequirement.PerTick<T, RequirementType<T, ? extends PerTick<T>>> {

        @SuppressWarnings("unchecked")
        public PerTick(RequirementTypeAdapter<T> requirementType, IOType actionType) {
            super((RequirementType) requirementType, actionType);
        }

        @Override
        public final ComponentRequirement<T, RequirementType<T, ? extends ComponentRequirementAdapter.PerTick<T>>> deepCopy() {
            return deepClone();
        }

        @Override
        public final ComponentRequirement<T, RequirementType<T, ? extends ComponentRequirementAdapter.PerTick<T>>> deepCopyModified(List<RecipeModifier> list) {
            return deepCloneModified(list);
        }

        public abstract ComponentRequirementAdapter.PerTick<T> deepClone();

        public abstract ComponentRequirementAdapter.PerTick<T> deepCloneModified(List<RecipeModifier> list);

    }


}
