package kport.gugu_utils.common.requirements.types;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import hellfirepvp.modularmachinery.common.machine.IOType;
import kport.gugu_utils.common.requirements.RequirementManaPerTick;
import kport.gugu_utils.common.requirements.basic.ComponentRequirementAdapter;
import static kport.gugu_utils.common.requirements.basic.RequirementUtils.tryGet;

public class RequirementTypeManaPerTick extends RequirementTypeAdapter<Integer> implements RequirementTypeAdapter.PerTick<Integer> {
    @Override
    public ComponentRequirementAdapter.PerTick<Integer> gererateRequirementPerTick(IOType type, JsonObject obj) {
        JsonPrimitive time = tryGet(obj, "time", true);
        return new RequirementManaPerTick(tryGet(obj, "mana", true).getAsInt(), time == null ? 1 : time.getAsInt(), type);

    }

    @Override
    public ComponentRequirementAdapter<Integer> gererateRequirement(IOType ioType, JsonObject jsonObject) {
        throw new UnsupportedOperationException("Pertick opeartion not support this!");
    }
}
