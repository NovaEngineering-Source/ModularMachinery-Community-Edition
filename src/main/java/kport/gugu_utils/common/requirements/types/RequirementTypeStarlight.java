package kport.gugu_utils.common.requirements.types;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.machine.IOType;
import kport.gugu_utils.common.requirements.RequirementStarlight;
import kport.gugu_utils.common.requirements.basic.ComponentRequirementAdapter;
import static kport.gugu_utils.common.requirements.basic.RequirementUtils.tryGet;

public class RequirementTypeStarlight extends RequirementTypeAdapter<Integer> implements RequirementTypeAdapter.PerTick<Integer>{
    @Override
    public ComponentRequirementAdapter.PerTick<Integer> gererateRequirementPerTick(IOType type, JsonObject obj) {
        JsonPrimitive constellationStr = tryGet(obj, "constellation", false);
        IConstellation constellation = null;
        if(constellationStr != null && constellationStr.isString()){
            constellation = ConstellationRegistry.getConstellationByName(constellationStr.getAsString());
            if(constellation == null){
                ModularMachinery.log.warn("Couldn't find constellation " + constellationStr.getAsString());
            }
        }
        return new RequirementStarlight(tryGet(obj, "starlight", true).getAsInt(), constellation, type);
    }

    @Override
    public ComponentRequirementAdapter<Integer> gererateRequirement(IOType ioType, JsonObject jsonObject) {
        throw new UnsupportedOperationException("Pertick opeartion not support this!");
    }
}
