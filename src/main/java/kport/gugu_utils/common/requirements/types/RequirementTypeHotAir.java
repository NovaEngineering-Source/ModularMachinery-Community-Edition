package kport.gugu_utils.common.requirements.types;

import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.common.machine.IOType;
import kport.gugu_utils.common.requirements.RequirementHotAir;
import kport.gugu_utils.common.requirements.basic.ComponentRequirementAdapter;
import static kport.gugu_utils.common.requirements.basic.RequirementUtils.tryGetInt;

public class RequirementTypeHotAir extends RequirementTypeAdapter<Integer> implements RequirementTypeAdapter.PerTick<Integer> {

    @Override
    public ComponentRequirementAdapter.PerTick<Integer> gererateRequirementPerTick(IOType ioType, JsonObject jsonObject) {
        if (ioType == IOType.OUTPUT) {
            return new RequirementHotAir(0, tryGetInt(jsonObject, "maxTemperature", null), 0, ioType);
        }
        return new RequirementHotAir(tryGetInt(jsonObject, "minTemperature", 0), tryGetInt(jsonObject, "maxTemperature", 0), tryGetInt(jsonObject, "consume", null), ioType);
    }

    @Override
    public ComponentRequirementAdapter<Integer> gererateRequirement(IOType ioType, JsonObject jsonObject) {
        throw new UnsupportedOperationException("Pertick opeartion not support this!");
    }
}
