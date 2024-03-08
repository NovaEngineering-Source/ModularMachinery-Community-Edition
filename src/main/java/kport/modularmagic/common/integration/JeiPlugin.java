package kport.modularmagic.common.integration;

import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.common.base.Mods;
import kport.modularmagic.common.integration.jei.helper.*;
import kport.modularmagic.common.integration.jei.ingredient.*;
import kport.modularmagic.common.integration.jei.render.*;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import thaumcraft.api.aspects.AspectList;

import javax.annotation.Nonnull;

@JEIPlugin
public class JeiPlugin implements IModPlugin {

    public static IGuiHelper GUI_HELPER;

    @Override
    public void register(IModRegistry registry) {
        GUI_HELPER = registry.getJeiHelpers().getGuiHelper();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void registerIngredients(@Nonnull IModIngredientRegistration registry) {
        if (Mods.BM2.isPresent()) {
            registry.register(DemonWill.class, Lists.newArrayList(), new DemonWillHelper<>(), new DemonWillRenderer());
            registry.register(LifeEssence.class, Lists.newArrayList(), new LifeEssenceHelper<>(), new LifeEssenceRenderer());
        }
        if (Mods.EXU2.isPresent()) {
            registry.register(Grid.class, Lists.newArrayList(), new GridHelper<>(), new GridRenderer());
            registry.register(Rainbow.class, Lists.newArrayList(), new RainbowHelper<>(), new RainbowRenderer());
        }
        if (Mods.NATURESAURA.isPresent()) {
            registry.register(Aura.class, Lists.newArrayList(), new AuraHelper<>(), new AuraRenderer());
        }
        if (Mods.TA.isPresent()) {
            registry.register(Impetus.class, Lists.newArrayList(), new ImpetusHelper<>(), new ImpetusRender());
        }
    }
}
