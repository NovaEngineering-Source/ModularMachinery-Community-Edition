package hellfirepvp.modularmachinery.common.machine;

import crafttweaker.annotations.ZenRegister;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenGetter;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.HashMap;
import java.util.Map;

@ZenRegister
@ZenClass("mods.modularmachinery.RecipeFailureActions")
public enum RecipeFailureActions {
    RESET("reset"),
    STILL("still"),
    DECREASE("decrease");

    public static final  RecipeFailureActions[]                      VALUES                   = RecipeFailureActions.values();
    public static final  HashMap<String, RecipeFailureActions>       NAME_MAP;
    private static final Map<ResourceLocation, RecipeFailureActions> REGISTRY_FAILURE_ACTIONS = new HashMap<>();
    private static       RecipeFailureActions                        defaultAction            = RecipeFailureActions.STILL;

    static {
        NAME_MAP = new HashMap<>(VALUES.length);
        for (RecipeFailureActions value : VALUES) {
            NAME_MAP.put(value.name, value);
        }
    }

    private final String name;

    RecipeFailureActions(String name) {
        this.name = name;
    }

    public static void loadFromConfig(Configuration cfg) {
        defaultAction = RecipeFailureActions.NAME_MAP.get(
            cfg.getString(
                "default-failure-actions", "general", "still",
                "Define what action is used when a recipe failed to run. [actions: reset, still, decrease]"));
        if (defaultAction == null) {
            defaultAction = RecipeFailureActions.NAME_MAP.get("still");
        }
    }

    @ZenMethod
    public static RecipeFailureActions getFailureAction(String key) {
        RecipeFailureActions actions = NAME_MAP.get(key);
        if (actions != null) {
            return actions;
        }

        return defaultAction;
    }

    @ZenMethod
    public static RecipeFailureActions getDefaultAction() {
        return defaultAction;
    }

    @ZenGetter("name")
    public String getName() {
        return name;
    }
}
