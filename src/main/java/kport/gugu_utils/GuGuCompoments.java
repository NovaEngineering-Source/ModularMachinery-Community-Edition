package kport.gugu_utils;

import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import kport.gugu_utils.common.Constants;
import kport.gugu_utils.common.components.*;
import net.minecraftforge.registries.IForgeRegistry;

public class GuGuCompoments {


    public static Object COMPONENT_ENVIRONMENT;
    public static Object COMPONENT_MANA;
    public static Object COMPONENT_STARLIGHT;
    public static Object COMPONENT_EMBER;
    public static Object COMPONENT_COMPRESSED_AIR;
    public static Object COMPONENT_HOT_AIR;

    public static void preInit() {
        COMPONENT_ENVIRONMENT = new ComponentEnvironment().setRegistryName(Constants.RESOURCE_ENVIRONMENT);
        if (Mods.BOTANIA.isPresent())
            COMPONENT_MANA = new ComponentMana().setRegistryName(Constants.RESOURCE_MANA);
        if (Mods.ASTRAL_SORCERY.isPresent())
            COMPONENT_STARLIGHT = new ComponentStarlight().setRegistryName(Constants.RESOURCE_STARLIGHT);
        if (Mods.EMBERS.isPresent())
            COMPONENT_EMBER = new ComponentEmber().setRegistryName(Constants.RESOURCE_EMBER);
        if (Mods.PNEUMATICCRAFT.isPresent())
            COMPONENT_COMPRESSED_AIR = new ComponentCompressedAir().setRegistryName(Constants.RESOURCE_COMPRESSED_AIR);


        if (Mods.PRODIGYTECH.isPresent())
            COMPONENT_HOT_AIR = new ComponentHotAir().setRegistryName(Constants.RESOURCE_HOT_AIR);
    }

    public static void initComponents(IForgeRegistry<ComponentType> registry) {
        registry.register((ComponentType) COMPONENT_ENVIRONMENT);
        if (Mods.BOTANIA.isPresent())
            registry.register((ComponentType) COMPONENT_MANA);
        if (Mods.ASTRAL_SORCERY.isPresent())
            registry.register((ComponentType) COMPONENT_STARLIGHT);
        if (Mods.EMBERS.isPresent())
            registry.register((ComponentType) COMPONENT_EMBER);
        if (Mods.PNEUMATICCRAFT.isPresent())
            registry.register((ComponentType) COMPONENT_COMPRESSED_AIR);
    }


}
