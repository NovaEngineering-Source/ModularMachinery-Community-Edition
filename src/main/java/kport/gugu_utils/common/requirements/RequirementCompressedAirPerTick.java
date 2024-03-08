package kport.gugu_utils.common.requirements;

import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import static hellfirepvp.modularmachinery.common.modifier.RecipeModifier.applyModifiers;
import kport.gugu_utils.compat.jei.components.JEIComponentCompressedAir;
import kport.gugu_utils.GuGuCompoments;
import kport.gugu_utils.GuGuRequirements;
import kport.gugu_utils.common.requirements.basic.ComponentRequirementAdapter;
import kport.gugu_utils.common.requirements.basic.RequirementConsumePerTick;
import kport.gugu_utils.common.requirements.types.RequirementTypeAdapter;

import java.util.List;

public class RequirementCompressedAirPerTick extends RequirementConsumePerTick<Integer, RequirementCompressedAir.RT> {

    float _pressure;
    int _air;

    @SuppressWarnings("unchecked")
    public RequirementCompressedAirPerTick(float pressure, int air, int totalTick, IOType actionType) {
        super((RequirementTypeAdapter<Integer>) GuGuRequirements.REQUIREMENT_TYPE_COMPRESSED_AIR_PER_TICK, actionType);
        this.setTotalTick(totalTick);
        _pressure = pressure;
        _air = air;
    }

    @Override
    protected boolean isCorrectHatch(MachineComponent component) {
        return component.getComponentType().equals(GuGuCompoments.COMPONENT_COMPRESSED_AIR);
    }

    @Override
    protected RequirementCompressedAir.RT emitConsumptionToken(RecipeCraftingContext context) {
        return new RequirementCompressedAir.RT(_pressure, _air);
    }

    @Override
    public ComponentRequirementAdapter.PerTick<Integer> deepClone() {
        return new RequirementCompressedAirPerTick(_pressure, _air, getTotalTick(), getActionType());
    }

    @Override
    public ComponentRequirementAdapter.PerTick<Integer> deepCloneModified(List<RecipeModifier> modifiers) {
        return new RequirementCompressedAirPerTick(_pressure, (int) applyModifiers(modifiers, this, _air, false), getTotalTick(), getActionType());
    }

    @Override
    public JEIComponent provideJEIComponent() {
        return new JEIComponentCompressedAir(_air, _pressure, true, getTotalTick());
    }

}