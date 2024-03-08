package kport.gugu_utils.common.requirements;

import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementType;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import kport.gugu_utils.common.Constants;
import kport.gugu_utils.common.envtypes.EnvironmentType;
import kport.gugu_utils.common.requirements.types.RequirementTypeAdapter;
import kport.gugu_utils.compat.jei.components.JEIComponentEnvironment;
import kport.gugu_utils.GuGuCompoments;
import kport.gugu_utils.GuGuRequirements;
import kport.gugu_utils.common.requirements.basic.ComponentRequirementAdapter;
import kport.gugu_utils.common.requirements.basic.IResourceToken;
import kport.gugu_utils.common.requirements.basic.RequirementConsumePerTick;

import java.util.Collection;
import java.util.List;

public class RequirementEnvironment extends RequirementConsumePerTick<EnvironmentType, RequirementEnvironment.RT> {

    private final EnvironmentType type;

    public RequirementEnvironment(EnvironmentType type, IOType actionType) {
        super((RequirementTypeAdapter<EnvironmentType>) GuGuRequirements.REQUIREMENT_TYPE_ENVIRONMENT, actionType);
        this.type = type;
    }

    public EnvironmentType getType() {
        return type;
    }

    @Override
    public ComponentRequirementAdapter.PerTick<EnvironmentType> deepClone() {
        return new RequirementEnvironment(type, getActionType());
    }

    @Override
    public ComponentRequirementAdapter.PerTick<EnvironmentType> deepCloneModified(List<RecipeModifier> list) {
        return deepClone();
    }

    @Override
    protected RT emitConsumptionToken(RecipeCraftingContext context) {
        return new RT(type);
    }

    @Override
    protected boolean isCorrectHatch(MachineComponent component) {
        return component.getComponentType().equals(GuGuCompoments.COMPONENT_ENVIRONMENT);
    }

    @Override
    public JEIComponent provideJEIComponent() {
        return new JEIComponentEnvironment(type);
    }

    public static class RT implements IResourceToken {

        private EnvironmentType type;

        public RT(EnvironmentType type) {
            this.type = type;
        }

        public EnvironmentType getType() {
            return type;
        }

        public void setType(EnvironmentType type) {
            this.type = type;
        }

        @Override
        public void applyModifiers(Collection<RecipeModifier> modifiers, RequirementType type, IOType ioType, float durationMultiplier) {

        }

        @Override
        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        private String error;

        @Override
        public String getKey() {
            return Constants.STRING_RESOURCE_ENVIRONMENT;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }
    }
}
