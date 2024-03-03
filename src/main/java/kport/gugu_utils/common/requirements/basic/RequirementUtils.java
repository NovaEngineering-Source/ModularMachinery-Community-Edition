package kport.gugu_utils.common.requirements.basic;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import hellfirepvp.modularmachinery.common.crafting.helper.ProcessingComponent;
import hellfirepvp.modularmachinery.common.crafting.helper.RecipeCraftingContext;
import hellfirepvp.modularmachinery.common.machine.IOType;

import javax.annotation.Nullable;

public final class RequirementUtils {
    public static boolean isValidComponent(ProcessingComponent<?> component, RecipeCraftingContext ctx, IOType type) {
        Object handler = component.getComponent().getContainerProvider();
        if (!(handler instanceof ICraftingResourceHolder)) {
            return false;
        }
        if (type == IOType.INPUT && ((ICraftingResourceHolder<?>) handler).canConsume()) {
            return true;
        }
        return type == IOType.OUTPUT && ((ICraftingResourceHolder<?>) handler).canGenerate();
    }

    public static JsonPrimitive tryGet(JsonObject requirement, String key, boolean required) {
        if (checkRequirement(requirement, key, required)) return null;
        if (!requirement.get(key).isJsonPrimitive()) {
            String msg = String.format("The requirement %s 's format is incorrect!", key);
            throw new JsonParseException(msg);
        }
        return requirement.getAsJsonPrimitive(key);
    }


    public static int tryGetInt(JsonObject requirement, String key, @Nullable Integer fallback) {
        JsonPrimitive jsonPrimitive = tryGet(requirement, key, fallback == null);
        if(jsonPrimitive == null || !jsonPrimitive.isNumber()) {
            if(fallback == null) {
                String msg = String.format("The requirement %s should be an Integer", key);
            }
            return fallback;
        }
        return jsonPrimitive.getAsInt();
    }

    public static float tryGetFloat(JsonObject requirement, String key, @Nullable Float fallback) {
        JsonPrimitive jsonPrimitive = tryGet(requirement, key, fallback == null);
        if(jsonPrimitive == null || !jsonPrimitive.isNumber()) {
            if(fallback == null) {
                String msg = String.format("The requirement %s should be an Integer", key);
            }
            return fallback;
        }
        return jsonPrimitive.getAsFloat();
    }

    private static boolean checkRequirement(JsonObject requirement, String key, boolean required) {
        if (!requirement.has(key)) {
            if (required) {
                String val = "UNKNOWN";
                StackTraceElement[] elements = new Throwable().getStackTrace();
                if (elements.length > 1) {
                    val = elements[1].getClassName().substring("RequirementType".length());
                }
                String msg = String.format("The ComponentType '%s' expects an '%s' entry!", val, key);
                throw new JsonParseException(msg);
            } else {
                return true;
            }
        }
        return false;
    }


    public static JsonArray tryGetArr(JsonObject requirement, String key, boolean required) {
        if (checkRequirement(requirement, key, required)) return null;
        if (!requirement.get(key).isJsonArray()) {
            String msg = String.format("The requirement %s 's format is incorrect!", key);
            throw new JsonParseException(msg);
        }
        return requirement.getAsJsonArray(key);
    }

}
