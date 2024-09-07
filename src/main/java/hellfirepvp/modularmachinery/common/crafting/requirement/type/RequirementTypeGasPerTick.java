package hellfirepvp.modularmachinery.common.crafting.requirement.type;

import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.common.crafting.requirement.RequirementGasPerTick;
import hellfirepvp.modularmachinery.common.machine.IOType;
import mekanism.api.gas.GasStack;

public class RequirementTypeGasPerTick extends RequirementType<GasStack, RequirementGasPerTick> {
    @Override
    public RequirementGasPerTick createRequirement(IOType type, JsonObject jsonObject) {
        return null;
    }
}