package kport.modularmagic.common.crafting.component;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;

public class ModularMagicComponents {

    public static final ResourceLocation KEY_COMPONENT_ASPECT        = new ResourceLocation(ModularMachinery.MODID, "aspect");
    public static final ResourceLocation KEY_COMPONENT_AURA          = new ResourceLocation(ModularMachinery.MODID, "aura");
    public static final ResourceLocation KEY_COMPONENT_CONSTELLATION = new ResourceLocation(ModularMachinery.MODID, "constellation");
    public static final ResourceLocation KEY_COMPONENT_GRID          = new ResourceLocation(ModularMachinery.MODID, "grid");
    public static final ResourceLocation KEY_COMPONENT_LIFE_ESSENCE  = new ResourceLocation(ModularMachinery.MODID, "lifeessence");
    public static final ResourceLocation KEY_COMPONENT_RAINBOW       = new ResourceLocation(ModularMachinery.MODID, "rainbow");
    public static final ResourceLocation KEY_COMPONENT_STARLIGHT     = new ResourceLocation(ModularMachinery.MODID, "starlight");
    public static final ResourceLocation KEY_COMPONENT_WILL          = new ResourceLocation(ModularMachinery.MODID, "will");
    public static final ResourceLocation KEY_COMPONENT_MANA          = new ResourceLocation(ModularMachinery.MODID, "mana");
    public static final ResourceLocation KEY_COMPONENT_IMPETUS       = new ResourceLocation(ModularMachinery.MODID, "impetus");

    public static final ArrayList<ComponentType> COMPONENTS = new ArrayList<>();

    public static void initComponents() {
        if (Mods.ASTRAL_SORCERY.isPresent()) {
            registerComponent(new ComponentConstellation(), KEY_COMPONENT_CONSTELLATION);
            registerComponent(new ComponentStarlight(), KEY_COMPONENT_STARLIGHT);
        }
        if (Mods.BM2.isPresent()) {
            registerComponent(new ComponentLifeEssence(), KEY_COMPONENT_LIFE_ESSENCE);
            registerComponent(new ComponentWill(), KEY_COMPONENT_WILL);
        }
        if (Mods.EXU2.isPresent()) {
            registerComponent(new ComponentGrid(), KEY_COMPONENT_GRID);
            registerComponent(new ComponentRainbow(), KEY_COMPONENT_RAINBOW);
        }
        if (Mods.NATURESAURA.isPresent()) {
            registerComponent(new ComponentAura(), KEY_COMPONENT_AURA);
        }
        if (Mods.TC6.isPresent()) {
            registerComponent(new ComponentAspect(), KEY_COMPONENT_ASPECT);
        }
        if (Mods.TA.isPresent()) {
            registerComponent(new ComponentImpetus(), KEY_COMPONENT_IMPETUS);
        }
        if (Mods.BOTANIA.isPresent()) {
            registerComponent(new ComponentMana(), KEY_COMPONENT_MANA);
        }
    }

    public static void registerComponent(ComponentType component, ResourceLocation name) {
        component.setRegistryName(name);
        COMPONENTS.add(component);
    }
}
