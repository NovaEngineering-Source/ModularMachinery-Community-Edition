package kport.gugu_utils.common.requirements;

import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementType;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.modifier.RecipeModifier;
import kport.gugu_utils.common.Constants;
import kport.gugu_utils.common.requirements.types.RequirementTypeAdapter;
import kport.gugu_utils.compat.jei.components.JEIComponentHotAir;
import kport.gugu_utils.GuGuCompoments;
import kport.gugu_utils.GuGuRequirements;
import kport.gugu_utils.common.requirements.basic.ComponentRequirementAdapter;
import kport.gugu_utils.common.requirements.basic.IResourceToken;
import kport.gugu_utils.common.requirements.basic.RequirementConsumePerTick;

import java.util.Collection;
import java.util.List;


public class RequirementHotAir extends RequirementConsumePerTick<Integer, RequirementHotAir.RT> {
    //最低工作温度
    private final int minTemperature;
    //最高工作温度
    private final int maxTemperature;
    //热量消耗
    private final int heat;
    //计算公式  输出 = (输入 - 消耗) * 传输比

    public RequirementHotAir(int minTemperature, int maxTemperature, int heat, IOType actionType) {
        super((RequirementTypeAdapter<Integer>) GuGuRequirements.REQUIREMENT_TYPE_HOT_AIR, actionType);
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.heat = heat;
    }

    @Override
    public ComponentRequirementAdapter.PerTick<Integer> deepClone() {
        return new RequirementHotAir(minTemperature, maxTemperature, heat, getActionType());
    }

    @Override
    public ComponentRequirementAdapter.PerTick<Integer> deepCloneModified(List list) {
        return new RequirementHotAir(minTemperature, maxTemperature, (int) RecipeModifier.applyModifiers(list, this, heat, false), getActionType());
    }

    @Override
    public JEIComponent provideJEIComponent() {
        return new JEIComponentHotAir(minTemperature, maxTemperature, heat);
    }

    @Override
    protected boolean isCorrectHatch(MachineComponent component) {
        return component.getComponentType().equals(GuGuCompoments.COMPONENT_HOT_AIR);
    }

    @Override
    protected RT emitConsumptionToken(RecipeCraftingContext context) {
        return new RT(minTemperature, maxTemperature, heat);
    }


    public static class RT implements IResourceToken {
        //最低工作温度
        private int minTemperature;
        //最高工作温度
        private int maxTemperature;
        //热量消耗
        private int heat;
        private String error;

        public RT(int minTemperature, int maxTemperature, int heat) {
            this.minTemperature = minTemperature;
            this.maxTemperature = maxTemperature;
            this.heat = heat;
        }

        public int getMinTemperature() {
            return minTemperature;
        }

        public int getMaxTemperature() {
            return maxTemperature;
        }

        public int getHeat() {
            return heat;
        }

        public void setHeat(int heat) {
            this.heat = heat;
        }

        @Override
        public void applyModifiers(Collection<RecipeModifier> modifiers, RequirementType type, IOType ioType, float durationMultiplier) {
            heat = (int) RecipeModifier.applyModifiers(modifiers, type, ioType, heat, false);
        }

        @Override
        public String getKey() {
            return Constants.STRING_RESOURCE_HOT_AIR;
        }

        @Override
        public boolean isEmpty() {
            return heat <= 0;
        }

        @Override
        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }
}
