package kport.gugu_utils.common.requirements.types;

import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.common.machine.IOType;
import kport.gugu_utils.common.requirements.RequirementMana;
import kport.gugu_utils.common.requirements.basic.ComponentRequirementAdapter;
import kport.gugu_utils.common.requirements.basic.RequirementUtils;

public class RequirementTypeMana extends RequirementTypeAdapter<Integer> {
    @Override
    public ComponentRequirementAdapter<Integer> gererateRequirement(IOType type, JsonObject obj) {

        return new RequirementMana(RequirementUtils.tryGet(obj, "mana", true).getAsInt(), type);
    }

}
