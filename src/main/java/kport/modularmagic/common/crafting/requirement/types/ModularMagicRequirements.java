package kport.modularmagic.common.crafting.requirement.types;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.crafting.requirement.type.RequirementType;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;

public class ModularMagicRequirements {

    public static final ResourceLocation                 KEY_REQUIREMENT_ASPECT        = new ResourceLocation(ModularMachinery.MODID, "aspect");
    public static final ResourceLocation                 KEY_REQUIREMENT_AURA          = new ResourceLocation(ModularMachinery.MODID, "aura");
    public static final ResourceLocation                 KEY_REQUIREMENT_CONSTELLATION = new ResourceLocation(ModularMachinery.MODID, "constellation");
    public static final ResourceLocation                 KEY_REQUIREMENT_GRID          = new ResourceLocation(ModularMachinery.MODID, "grid");
    public static final ResourceLocation                 KEY_REQUIREMENT_LIFE_ESSENCE  = new ResourceLocation(ModularMachinery.MODID, "lifeessence");
    public static final ResourceLocation                 KEY_REQUIREMENT_RAINBOW       = new ResourceLocation(ModularMachinery.MODID, "rainbow");
    public static final ResourceLocation                 KEY_REQUIREMENT_STARLIGHT     = new ResourceLocation(ModularMachinery.MODID, "starlight");
    public static final ResourceLocation                 KEY_REQUIREMENT_WILL          = new ResourceLocation(ModularMachinery.MODID, "will");
    public static final ResourceLocation                 KEY_REQUIREMENT_MANA          = new ResourceLocation(ModularMachinery.MODID, "mana");
    public static final ResourceLocation                 KEY_REQUIREMENT_IMPETUS       = new ResourceLocation(ModularMachinery.MODID, "impetus");
    public static final ArrayList<RequirementType<?, ?>> REQUIREMENTS                  = new ArrayList<>();

    public static void initRequirements() {
        if (Mods.ASTRAL_SORCERY.isPresent()) {
            registerRequirement(new RequirementTypeConstellation(), KEY_REQUIREMENT_CONSTELLATION);
            registerRequirement(new RequirementTypeStarlight(), KEY_REQUIREMENT_STARLIGHT);
        }
        if (Mods.BM2.isPresent()) {
            registerRequirement(new RequirementTypeLifeEssence(), KEY_REQUIREMENT_LIFE_ESSENCE);
            registerRequirement(new RequirementTypeWill(), KEY_REQUIREMENT_WILL);
        }
        if (Mods.EXU2.isPresent()) {
            registerRequirement(new RequirementTypeGrid(), KEY_REQUIREMENT_GRID);
            registerRequirement(new RequirementTypeRainbow(), KEY_REQUIREMENT_RAINBOW);
        }
        if (Mods.NATURESAURA.isPresent()) {
            registerRequirement(new RequirementTypeAura(), KEY_REQUIREMENT_AURA);
        }
        if (Mods.TC6.isPresent()) {
            registerRequirement(new RequirementTypeAspect(), KEY_REQUIREMENT_ASPECT);
        }
        if (Mods.TA.isPresent()) {
            registerRequirement(new RequirementTypeImpetus(), KEY_REQUIREMENT_IMPETUS);
        }
        if (Mods.BOTANIA.isPresent()) {
            registerRequirement(new RequirementTypeMana(), KEY_REQUIREMENT_MANA);
        }
    }

    public static void registerRequirement(RequirementType<?, ?> requirement, ResourceLocation name) {
        requirement.setRegistryName(name);
        REQUIREMENTS.add(requirement);
    }
}
