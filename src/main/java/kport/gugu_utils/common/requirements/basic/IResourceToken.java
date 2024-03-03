package kport.gugu_utils.common.requirements.basic;

import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementType;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;

import java.util.Collection;

public interface IResourceToken {
    void applyModifiers(Collection<RecipeModifier> modifiers, RequirementType type, IOType ioType, float durationMultiplier);

    default void applyModifiers(Collection<RecipeModifier> modifiers, RequirementType type, IOType ioType) {
        applyModifiers(modifiers, type, ioType, 1.0f);
    }

    String getKey();

    default String getError() {
        return null;
    }


    boolean isEmpty();
}
