package hellfirepvp.modularmachinery.common.machine;

import com.google.gson.*;
import net.minecraft.util.JsonUtils;

import java.lang.reflect.Type;

public class DynamicMachinePreDeserializer implements JsonDeserializer<DynamicMachine> {
    @Override
    public DynamicMachine deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject root = json.getAsJsonObject();

        String registryName = getRegistryName(root);
        String localized = getLocalizedName(root);

        DynamicMachine machine = new DynamicMachine(registryName);
        machine.setLocalizedName(localized);

        // Prefix
        if (root.has("prefix")) {
            machine.setPrefix(getPrefix(root));
        }

        // Failure Action
        if (root.has("failure-action")) {
            machine.setFailureAction(getFailureActions(root));
        }

        // Requires Blueprint
        if (root.has("requires-blueprint")) {
            machine.setRequiresBlueprint(getRequireBlueprint(root));
        }

        // Color
        if (root.has("color")) {
            machine.setDefinedColor(getColor(root));
        }

        // Has Factory
        if (root.has("has-factory")) {
            machine.setHasFactory(getHasFactory(root));
        }

        // Factory Only
        if (root.has("factory-only")) {
            machine.setFactoryOnly(getFactoryOnly(root));
        }

        return machine;
    }

    public static String getRegistryName(JsonObject root) throws JsonParseException {
        String registryName = JsonUtils.getString(root, "registryname", "");
        if (registryName.isEmpty()) {
            registryName = JsonUtils.getString(root, "registryName", "");
            if (registryName.isEmpty()) {
                throw new JsonParseException("Invalid/Missing 'registryname' !");
            }
        }
        return registryName;
    }

    public static String getPrefix(JsonObject root) throws JsonParseException {
        String localized = JsonUtils.getString(root, "prefix", "");

        if (localized.isEmpty()) {
            throw new JsonParseException("Invalid/Missing 'prefix' !");
        }

        return localized;
    }

    public static String getLocalizedName(JsonObject root) throws JsonParseException {
        String localized = JsonUtils.getString(root, "localizedname", "");

        if (localized.isEmpty()) {
            throw new JsonParseException("Invalid/Missing 'localizedname' !");
        }

        return localized;
    }

    public static RecipeFailureActions getFailureActions(JsonObject root) throws JsonParseException {
        JsonElement failureAction = root.get("failure-action");
        if (!failureAction.isJsonPrimitive() || !failureAction.getAsJsonPrimitive().isString()) {
            throw new JsonParseException("'failure-action' has to be 'reset', 'still' or 'decrease'!");
        }
        String action = failureAction.getAsJsonPrimitive().getAsString();
        return RecipeFailureActions.getFailureAction(action);
    }

    public static boolean getRequireBlueprint(JsonObject root) throws JsonParseException {
        JsonElement elementBlueprint = root.get("requires-blueprint");
        if (!elementBlueprint.isJsonPrimitive() || !elementBlueprint.getAsJsonPrimitive().isBoolean()) {
            throw new JsonParseException("'requires-blueprint' has to be either 'true' or 'false'!");
        }
        return elementBlueprint.getAsJsonPrimitive().getAsBoolean();
    }

    public static int getColor(JsonObject root) throws JsonParseException {
        JsonElement elementColor = root.get("color");
        if (!elementColor.isJsonPrimitive()) {
            throw new JsonParseException("The Color defined in 'color' should be a hex integer number! Found " + elementColor + " instead!");
        }
        int hexColor;
        String hexStr = elementColor.getAsJsonPrimitive().getAsString();
        try {
            hexColor = Integer.parseInt(hexStr, 16);
        } catch (NumberFormatException parseExc) {
            throw new JsonParseException("The Color defined in 'color' should be a hex integer number! Found " + elementColor + " instead!", parseExc);
        }
        return hexColor;
    }

    public static boolean getHasFactory(JsonObject root) throws JsonParseException {
        JsonElement elementHasFactory = root.get("has-factory");
        if (!elementHasFactory.isJsonPrimitive() || !elementHasFactory.getAsJsonPrimitive().isBoolean()) {
            throw new JsonParseException("'has-factory' has to be either 'true' or 'false'!");
        }
        return elementHasFactory.getAsJsonPrimitive().getAsBoolean();
    }

    public static boolean getFactoryOnly(JsonObject root) throws JsonParseException {
        JsonElement elementFactoryOnly = root.get("factory-only");
        if (!elementFactoryOnly.isJsonPrimitive() || !elementFactoryOnly.getAsJsonPrimitive().isBoolean()) {
            throw new JsonParseException("'factory-only' has to be either 'true' or 'false'!");
        }
        return elementFactoryOnly.getAsJsonPrimitive().getAsBoolean();
    }
}
