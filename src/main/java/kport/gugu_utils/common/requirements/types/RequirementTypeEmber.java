package kport.gugu_utils.common.requirements.types;

import com.google.gson.JsonObject;
import hellfirepvp.modularmachinery.common.machine.IOType;
import kport.gugu_utils.common.requirements.RequirementEmber;
import kport.gugu_utils.common.requirements.basic.ComponentRequirementAdapter;
import kport.gugu_utils.common.requirements.basic.RequirementUtils;

public class RequirementTypeEmber extends RequirementTypeAdapter<Double>{
    @Override
    public ComponentRequirementAdapter<Double> gererateRequirement(IOType ioType, JsonObject jsonObject) {
        return new RequirementEmber(RequirementUtils.tryGet(jsonObject, "ember", true).getAsDouble(), ioType);
    }
}
