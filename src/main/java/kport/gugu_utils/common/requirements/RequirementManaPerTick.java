package kport.gugu_utils.common.requirements;

import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import static hellfirepvp.modularmachinery.common.modifier.RecipeModifier.applyModifiers;
import kport.gugu_utils.common.requirements.types.RequirementTypeAdapter;
import kport.gugu_utils.compat.jei.components.JEIComponentMana;
import kport.gugu_utils.GuGuCompoments;
import kport.gugu_utils.GuGuRequirements;
import kport.gugu_utils.common.requirements.basic.ComponentRequirementAdapter;
import kport.gugu_utils.common.requirements.basic.RequirementConsumePerTick;

import java.util.List;

public class RequirementManaPerTick extends RequirementConsumePerTick<Integer, RequirementMana.RT> {

    int _mana;

    public RequirementManaPerTick(int mana, int totalTick, IOType actionType) {
        super((RequirementTypeAdapter<Integer>) GuGuRequirements.REQUIREMENT_TYPE_MANA_PER_TICK, actionType);
        this.setTotalTick(totalTick);
        _mana = mana;
    }

    public int getMana() {
        return _mana;
    }

    @Override
    protected boolean isCorrectHatch(MachineComponent component) {
        return component.getComponentType().equals(GuGuCompoments.COMPONENT_MANA);
    }

    @Override
    protected RequirementMana.RT emitConsumptionToken(RecipeCraftingContext context) {
        return new RequirementMana.RT(_mana);
    }


    @Override
    public ComponentRequirementAdapter.PerTick<Integer> deepClone() {
        return new RequirementManaPerTick(_mana, getTotalTick(), getActionType());
    }

    @Override
    public ComponentRequirementAdapter.PerTick<Integer> deepCloneModified(List<RecipeModifier> list) {
        return new RequirementManaPerTick((int) applyModifiers(list, this, _mana, false), getTotalTick(), getActionType());
    }

    @Override
    public JEIComponent provideJEIComponent() {
        return new JEIComponentMana(this.getMana(), true, this.getTotalTick());
    }

}