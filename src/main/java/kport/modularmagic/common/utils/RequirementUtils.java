package kport.modularmagic.common.utils;

import WayofTime.bloodmagic.soul.EnumDemonWillType;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import de.ellpeck.naturesaura.api.NaturesAuraAPI;
import de.ellpeck.naturesaura.api.aura.type.IAuraType;
import net.minecraft.util.ResourceLocation;
import thaumcraft.api.aspects.Aspect;

import javax.annotation.Nullable;

public class RequirementUtils {

    public static int getRequiredInt(JsonObject json, String key, String requirement) {
        if (json.has(key) && json.get(key).isJsonPrimitive() && json.getAsJsonPrimitive(key).isNumber()) {
            int i = json.getAsJsonPrimitive(key).getAsInt();
            return i;
        }
        throw new JsonParseException("The component '" + requirement + "' expects a '" + key + "' integer entry !");
    }

    public static double getRequiredDouble(JsonObject json, String key, String requirement) {
        if (json.has(key) && json.get(key).isJsonPrimitive() && json.getAsJsonPrimitive(key).isNumber()) {
            double d = json.getAsJsonPrimitive(key).getAsInt();
            return d;
        }
        throw new JsonParseException("The component '" + requirement + "' expects a '" + key + "' double entry !");
    }

    public static float getRequiredFloat(JsonObject json, String key, String requirement) {
        if (json.has(key) && json.get(key).isJsonPrimitive() && json.getAsJsonPrimitive(key).isNumber()) {
            float f = json.getAsJsonPrimitive(key).getAsInt();
            return f;
        }
        throw new JsonParseException("The component '" + requirement + "' expects a '" + key + "' float entry !");
    }

    public static int getRequiredPositiveInt(JsonObject json, String key, String requirement) {
        if (json.has(key) && json.get(key).isJsonPrimitive() && json.getAsJsonPrimitive(key).isNumber()) {
            int i = json.getAsJsonPrimitive(key).getAsInt();
            if (i > 0) return i;
        }
        throw new JsonParseException("The component '" + requirement + "' expects a '" + key + "' positive integer entry !");
    }

    public static double getRequiredPositiveDouble(JsonObject json, String key, String requirement) {
        if (json.has(key) && json.get(key).isJsonPrimitive() && json.getAsJsonPrimitive(key).isNumber()) {
            double d = json.getAsJsonPrimitive(key).getAsInt();
            if (d > 0) return d;
        }
        throw new JsonParseException("The component '" + requirement + "' expects a '" + key + "' positive double entry !");
    }

    public static float getRequiredPositiveFloat(JsonObject json, String key, String requirement) {
        if (json.has(key) && json.get(key).isJsonPrimitive() && json.getAsJsonPrimitive(key).isNumber()) {
            float f = json.getAsJsonPrimitive(key).getAsInt();
            if (f > 0) return f;
        }
        throw new JsonParseException("The component '" + requirement + "' expects a '" + key + "' positive float entry !");
    }

    public static int getOptionalInt(JsonObject json, String key, int defaultValue) {
        if (json.has(key) && json.get(key).isJsonPrimitive() && json.getAsJsonPrimitive(key).isNumber()) {
            int i = json.getAsJsonPrimitive(key).getAsInt();
            return i;
        }
        return defaultValue;
    }

    public static double getOptionalDouble(JsonObject json, String key, double defaultValue) {
        if (json.has(key) && json.get(key).isJsonPrimitive() && json.getAsJsonPrimitive(key).isNumber()) {
            double d = json.getAsJsonPrimitive(key).getAsDouble();
            return d;
        }
        return defaultValue;
    }

    public static float getOptionalFloat(JsonObject json, String key, float defaultValue) {
        if (json.has(key) && json.get(key).isJsonPrimitive() && json.getAsJsonPrimitive(key).isNumber()) {
            float f = json.getAsJsonPrimitive(key).getAsFloat();
            return f;
        }
        return defaultValue;
    }

    public static String getRequiredString(JsonObject json, String key, String requirement) {
        if (json.has(key) && json.get(key).isJsonPrimitive() && json.getAsJsonPrimitive(key).isString()) {
            String s = json.getAsJsonPrimitive(key).getAsString();
            return s;
        }
        throw new JsonParseException("The component '" + requirement + "' expects a '" + key + "' string entry !");
    }

    @Nullable
    public static String getOptionalString(JsonObject json, String key) {
        if (json.has(key) && json.get(key).isJsonPrimitive() && json.getAsJsonPrimitive(key).isString()) {
            String s = json.getAsJsonPrimitive(key).getAsString();
            return s;
        }
        return null;
    }

    public static boolean getRequiredBoolean(JsonObject json, String key, String requirement) {
        if (json.has(key) && json.get(key).isJsonPrimitive() && json.getAsJsonPrimitive(key).isBoolean()) {
            boolean b = json.getAsJsonPrimitive(key).getAsBoolean();
            return b;
        }
        throw new JsonParseException("The component '" + requirement + "' expects a '" + key + "' boolean entry !");
    }

    public static boolean getOptionalBoolean(JsonObject json, String key, boolean defaultValue) {
        if (json.has(key) && json.get(key).isJsonPrimitive() && json.getAsJsonPrimitive(key).isBoolean()) {
            boolean b = json.getAsJsonPrimitive(key).getAsBoolean();
            return b;
        }
        return defaultValue;
    }

    public static Aspect getAspect(JsonObject json, String key, String requirement) {
        String s = getRequiredString(json, key, requirement);
        Aspect aspect = Aspect.getAspect(s);
        if (aspect != null) return aspect;
        else throw new JsonParseException("Invalid aspect name : " + s);
    }

    public static IAuraType getAuraType(JsonObject json, String key, String requirement) {
        String s = getRequiredString(json, key, requirement);
        IAuraType aura = NaturesAuraAPI.AURA_TYPES.get(new ResourceLocation(s));
        if (aura != null) return aura;
        else throw new JsonParseException("Invalid aura type : " + s);
    }


    public static EnumDemonWillType getWillType(JsonObject json, String key, String requirement) {
        String s = getRequiredString(json, key, requirement);
        EnumDemonWillType willType = null;
        switch (s) {
            case "raw":
                willType = EnumDemonWillType.DEFAULT;
                break;
            case "corrosive":
                willType = EnumDemonWillType.CORROSIVE;
                break;
            case "destructive":
                willType = EnumDemonWillType.DESTRUCTIVE;
                break;
            case "steadfast":
                willType = EnumDemonWillType.STEADFAST;
                break;
            case "vengeful":
                willType = EnumDemonWillType.VENGEFUL;
                break;
        }
        if (willType != null) return willType;
        else throw new JsonParseException("Invalid demon will type name : " + s);
    }
}
