/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.registry;

import hellfirepvp.modularmachinery.common.CommonProxy;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.crafting.adapter.AdapterMinecraftFurnace;
import hellfirepvp.modularmachinery.common.crafting.adapter.RecipeAdapter;
import hellfirepvp.modularmachinery.common.crafting.adapter.dragonresearch.AdapterDragonResearchFusionCrafting;
import hellfirepvp.modularmachinery.common.crafting.adapter.ic2.AdapterIC2Compressor;
import hellfirepvp.modularmachinery.common.crafting.adapter.ic2.AdapterIC2Macerator;
import hellfirepvp.modularmachinery.common.crafting.adapter.nco.*;
import hellfirepvp.modularmachinery.common.crafting.adapter.tc6.AdapterTC6InfusionMatrix;
import hellfirepvp.modularmachinery.common.crafting.adapter.tconstruct.AdapterSmelteryAlloyRecipe;
import hellfirepvp.modularmachinery.common.crafting.adapter.tconstruct.AdapterSmelteryMeltingRecipe;
import hellfirepvp.modularmachinery.common.crafting.adapter.te5.*;

import static hellfirepvp.modularmachinery.common.lib.RecipeAdaptersMM.MINECRAFT_FURNACE;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryRecipeAdapters
 * Created by HellFirePvP
 * Date: 13.07.2019 / 09:05
 */
public class RegistryRecipeAdapters {

    private RegistryRecipeAdapters() {
    }

    public static void initialize() {
        MINECRAFT_FURNACE = registerAdapter(new AdapterMinecraftFurnace());
        if (Mods.IC2.isPresent()) {
            registerAdapter(new AdapterIC2Compressor());
            registerAdapter(new AdapterIC2Macerator());
        }
        if (Mods.NUCLEARCRAFT_OVERHAULED.isPresent()) {
            registerAdapter(new AdapterNCOAlloyFurnace());
            registerAdapter(new AdapterNCOInfuser());
            registerAdapter(new AdapterNCOChemicalReactor());
            registerAdapter(new AdapterNCOMelter());
            registerAdapter(new AdapterNCOIngotFormer());
            registerAdapter(new AdapterNCOSeparator());
            registerAdapter(new AdapterNCOCentrifuge());
            registerAdapter(new AdapterNCOElectrolyzer());
        }
        if (Mods.TCONSTRUCT.isPresent()) {
            registerAdapter(new AdapterSmelteryMeltingRecipe());
            registerAdapter(new AdapterSmelteryAlloyRecipe());
        }
        if (Mods.TC6.isPresent()){
            registerAdapter(new AdapterTC6InfusionMatrix());
        }
        if (Mods.THERMAL_EXPANSION.isPresent()) {
            registerAdapter(new InsolatorRecipeAdapter(false));
            registerAdapter(new InsolatorRecipeAdapter(true));
            registerAdapter(new CrucibleRecipeAdapter());
            registerAdapter(new PulverizerRecipeAdapter());
            registerAdapter(new CompactorGearRecipeAdapter());
            registerAdapter(new CompactorPlateRecipeAdapter());
            registerAdapter(new CentrifugeRecipeAdapter());
        }
        if (Mods.DRAGON_RESEARCH.isPresent()) {
            registerAdapter(new AdapterDragonResearchFusionCrafting());
        }
    }

    public static <T extends RecipeAdapter> T registerAdapter(T adapter) {
        CommonProxy.registryPrimer.register(adapter);
        return adapter;
    }

}
