package kport.gugu_utils.compat.jei;

import hellfirepvp.modularmachinery.common.base.Mods;
import kport.gugu_utils.compat.jei.ingedients.*;
import kport.gugu_utils.compat.jei.renders.*;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IModIngredientRegistration;

import java.util.ArrayList;

@JEIPlugin
public class JEICompact implements IModPlugin {
    @Override
    public void registerIngredients(IModIngredientRegistration registry) {
        if (Mods.BOTANIA.isPresent())
            registry.register(() -> IngredientMana.class, new ArrayList<>(), new InfoHelper<>(), RendererMana.INSTANCE);
        if (Mods.ASTRAL_SORCERY.isPresent())
            registry.register(() -> IngredientStarlight.class, new ArrayList<>(), new InfoHelper<>(), RendererStarlight.INSTANCE);
        if (Mods.EMBERS.isPresent())
            registry.register(() -> IngredientEmber.class, new ArrayList<>(), new InfoHelper<>(), RendererEmber.INSTANCE);
        registry.register(() -> IngredientEnvironment.class, new ArrayList<>(), new InfoHelper<>(), RendererEnvironment.INSTANCE);
        if(Mods.PNEUMATICCRAFT.isPresent()){
            registry.register(() -> IngredientCompressedAir.class, new ArrayList<>(), new InfoHelper<>(), RendererCompressedAir.INSTANCE);
        }
        if(Mods.PRODIGYTECH.isPresent()){
            registry.register(() -> IngredientHotAir.class, new ArrayList<>(), new InfoHelper<>(), RendererHotAir.INSTANCE);
        }
    }

}
