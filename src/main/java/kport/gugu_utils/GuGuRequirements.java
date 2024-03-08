package kport.gugu_utils;

import hellfirepvp.modularmachinery.common.base.Mods;
import static kport.gugu_utils.common.Constants.*;
import kport.gugu_utils.common.requirements.types.*;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class GuGuRequirements {

    public static Object REQUIREMENT_TYPE_ENVIRONMENT;
    public static Object REQUIREMENT_TYPE_MANA;
    public static Object REQUIREMENT_TYPE_MANA_PER_TICK;
    public static Object REQUIREMENT_TYPE_STARLIGHT;
    public static Object REQUIREMENT_TYPE_EMBER;
    public static Object REQUIREMENT_TYPE_COMPRESSED_AIR;
    public static Object REQUIREMENT_TYPE_COMPRESSED_AIR_PER_TICK;
    public static Object REQUIREMENT_TYPE_HOT_AIR;

    public static void preInit() {
        REQUIREMENT_TYPE_ENVIRONMENT = new RequirementTypeEnvironment().setRegistryName(RESOURCE_ENVIRONMENT);
        if (Mods.BOTANIA.isPresent()) {
            REQUIREMENT_TYPE_MANA = new RequirementTypeMana().setRegistryName(RESOURCE_MANA);
            REQUIREMENT_TYPE_MANA_PER_TICK = new RequirementTypeManaPerTick().setRegistryName(RESOURCE_MANA_PERTICK);
        }
        if (Mods.ASTRAL_SORCERY.isPresent())
            REQUIREMENT_TYPE_STARLIGHT = new RequirementTypeStarlight().setRegistryName(RESOURCE_STARLIGHT);
        if (Mods.EMBERS.isPresent())
            REQUIREMENT_TYPE_EMBER = new RequirementTypeEmber().setRegistryName(RESOURCE_EMBER);
        if (Mods.PNEUMATICCRAFT.isPresent()) {
            REQUIREMENT_TYPE_COMPRESSED_AIR = new RequirementTypeCompressedAir().setRegistryName(RESOURCE_COMPRESSED_AIR);
            REQUIREMENT_TYPE_COMPRESSED_AIR_PER_TICK = new RequirementTypeCompressedAirPerTick().setRegistryName(RESOURCE_COMPRESSED_AIR_PER_TICK);
        }
        if (Mods.PRODIGYTECH.isPresent())
            REQUIREMENT_TYPE_HOT_AIR = new RequirementTypeHotAir().setRegistryName(RESOURCE_HOT_AIR);


    }


    @SuppressWarnings("unchecked")
    public static void initRequirements(IForgeRegistry registry) {
        registry.register((IForgeRegistryEntry) REQUIREMENT_TYPE_ENVIRONMENT);
        if (Mods.BOTANIA.isPresent()) {
            registry.register((IForgeRegistryEntry) REQUIREMENT_TYPE_MANA);
            registry.register((IForgeRegistryEntry) REQUIREMENT_TYPE_MANA_PER_TICK);
        }
        if (Mods.ASTRAL_SORCERY.isPresent())
            registry.register((IForgeRegistryEntry) REQUIREMENT_TYPE_STARLIGHT);
        if (Mods.EMBERS.isPresent())
            registry.register((IForgeRegistryEntry) REQUIREMENT_TYPE_EMBER);

        if (Mods.PNEUMATICCRAFT.isPresent()) {
            registry.register((IForgeRegistryEntry) REQUIREMENT_TYPE_COMPRESSED_AIR);
            registry.register((IForgeRegistryEntry) REQUIREMENT_TYPE_COMPRESSED_AIR_PER_TICK);
        }
        if (Mods.PRODIGYTECH.isPresent())
            registry.register((IForgeRegistryEntry) REQUIREMENT_TYPE_HOT_AIR);


    }
}
