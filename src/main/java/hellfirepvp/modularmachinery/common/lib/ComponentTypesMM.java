/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.lib;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import net.minecraft.util.ResourceLocation;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ComponentTypesMM
 * Created by HellFirePvP
 * Date: 13.07.2019 / 09:02
 */
public class ComponentTypesMM {

    public static final ResourceLocation KEY_COMPONENT_ITEM = new ResourceLocation(ModularMachinery.MODID, "item");
    public static final ResourceLocation KEY_COMPONENT_FLUID = new ResourceLocation(ModularMachinery.MODID, "fluid");
    public static final ResourceLocation KEY_COMPONENT_ITEM_FLUID = new ResourceLocation(ModularMachinery.MODID, "item_fluid");
    public static final ResourceLocation KEY_COMPONENT_GAS = new ResourceLocation(ModularMachinery.MODID, "gas");
    public static final ResourceLocation KEY_COMPONENT_ENERGY = new ResourceLocation(ModularMachinery.MODID, "energy");
    public static final ResourceLocation KEY_COMPONENT_SMART_INTERFACE = new ResourceLocation(ModularMachinery.MODID, "interface_number");
    public static final ResourceLocation KEY_COMPONENT_PARALLEL_CONTROLLER = new ResourceLocation(ModularMachinery.MODID, "parallel_controller");
    public static final ResourceLocation KEY_COMPONENT_UPGRADE_BUS = new ResourceLocation(ModularMachinery.MODID, "upgrade");

    public static ComponentType COMPONENT_ITEM;
    public static ComponentType COMPONENT_FLUID;
    public static ComponentType COMPONENT_ITEM_FLUID;
    public static ComponentType COMPONENT_ENERGY;
    public static ComponentType COMPONENT_GAS;
    public static ComponentType COMPONENT_SMART_INTERFACE;
    public static ComponentType COMPONENT_PARALLEL_CONTROLLER;
    public static ComponentType COMPONENT_UPGRADE_BUS;

    private ComponentTypesMM() {
    }

}
