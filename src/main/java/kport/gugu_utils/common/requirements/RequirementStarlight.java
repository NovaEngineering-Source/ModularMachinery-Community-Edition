package kport.gugu_utils.common.requirements;

import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementType;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import static hellfirepvp.modularmachinery.common.modifier.RecipeModifier.applyModifiers;
import kport.gugu_utils.common.Constants;
import kport.gugu_utils.common.requirements.types.RequirementTypeAdapter;
import kport.gugu_utils.compat.jei.components.JEIComponentStarlight;
import kport.gugu_utils.GuGuCompoments;
import kport.gugu_utils.GuGuRequirements;
import kport.gugu_utils.common.requirements.basic.ComponentRequirementAdapter;
import kport.gugu_utils.common.requirements.basic.IResourceToken;
import kport.gugu_utils.common.requirements.basic.RequirementConsumePerTick;

import java.util.Collection;
import java.util.List;

public class RequirementStarlight extends RequirementConsumePerTick<Integer, RequirementStarlight.RT> {
    private int starlight;
    private IConstellation constellation;

    public RequirementStarlight(int starlight, IConstellation constellation, IOType actionType) {
        super((RequirementTypeAdapter<Integer>) GuGuRequirements.REQUIREMENT_TYPE_STARLIGHT, actionType);
        this.starlight = starlight;
        this.constellation = constellation;
    }

    public int getStarlight() {
        return starlight;
    }

    public void setStarlight(int starlight) {
        this.starlight = starlight;
    }

    public IConstellation getConstellation() {
        return constellation;
    }

    public void setConstellation(IConstellation constellation) {
        this.constellation = constellation;
    }

    @Override
    public ComponentRequirementAdapter.PerTick<Integer> deepClone() {
        return new RequirementStarlight(getStarlight(), getConstellation(), getActionType());
    }

    @Override
    public ComponentRequirementAdapter.PerTick<Integer> deepCloneModified(List<RecipeModifier> list) {
        return new RequirementStarlight((int) applyModifiers(list, this, getStarlight(), false), getConstellation(), getActionType());
    }

    @Override
    protected RT emitConsumptionToken(RecipeCraftingContext context) {
        return new RT(this.starlight, this.constellation);
    }

    @Override
    protected boolean isCorrectHatch(MachineComponent component) {
        return component.getComponentType().equals(GuGuCompoments.COMPONENT_STARLIGHT);
    }

    @Override
    public JEIComponent provideJEIComponent() {
        return new JEIComponentStarlight(this.starlight, this.constellation);
    }

    public static class RT implements IResourceToken {
        private int starlight;
        private IConstellation constellation;
        private String error;

        public RT(int starlight, IConstellation constellation) {
            this.starlight = starlight;
            this.constellation = constellation;
        }

        @Override
        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public int getStarlight() {
            return starlight;
        }

        public void setStarlight(int starlight) {
            this.starlight = starlight;
        }

        public IConstellation getConstellation() {
            return constellation;
        }

        public void setConstellation(IConstellation constellation) {
            this.constellation = constellation;
        }

        @Override
        public void applyModifiers(Collection<RecipeModifier> modifiers, RequirementType type, IOType ioType, float durationMultiplier) {
            starlight = (int) RecipeModifier.applyModifiers(modifiers, type, ioType, starlight, false);
        }

        @Override
        public String getKey() {
            return Constants.STRING_RESOURCE_STARLIGHT;
        }

        @Override
        public boolean isEmpty() {
            return starlight <= 0;
        }


    }
}
