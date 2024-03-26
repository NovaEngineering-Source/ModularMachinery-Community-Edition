package github.kasuminova.mmce.common.integration.groovyscript;

import com.cleanroommc.groovyscript.api.IScriptReloadable;
import com.cleanroommc.groovyscript.registry.NamedRegistry;
import net.minecraft.util.ResourceLocation;

public class GroovyMachine extends NamedRegistry implements IScriptReloadable {

    private final ResourceLocation name;

    public GroovyMachine(ResourceLocation name) {
        this.name = name;
    }

    @Override
    public void onReload() {
    }

    @Override
    public void afterScriptLoad() {
    }

    public GroovyRecipe recipeBuilder(String name) {
        return new GroovyRecipe(this.name);
    }
}
