/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.registry;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.CommonProxy;
import hellfirepvp.modularmachinery.common.item.*;

import kport.gugu_utils.common.tools.ItemRangedConstructTool;
import net.minecraft.item.Item;
import youyihj.mmce.common.item.MachineProjector;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static hellfirepvp.modularmachinery.common.lib.ItemsMM.*;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RegistryItems
 * Created by HellFirePvP
 * Date: 28.06.2017 / 18:40
 */
public class RegistryItems {

    public static final List<ItemDynamicColor> pendingDynamicColorItems = new LinkedList<>();
    protected static final List<Item> ITEM_BLOCKS = new ArrayList<>();
    protected static final List<Item> ITEM_BLOCKS_WITH_CUSTOM_NAME = new ArrayList<>();
    protected static final List<Item> ITEM_MODEL_REGISTER = new ArrayList<>();

    public static void initialize() {
        blueprint = prepareRegister(new ItemBlueprint());
        modularium = prepareRegister(new ItemModularium());
        constructTool = prepareRegister(new ItemConstructTool());
        rangedConstructTool = prepareRegister(new ItemRangedConstructTool());
        prepareRegisterWithCustomName(MachineProjector.INSTANCE);

        registerItemBlocks();
        registerItemModels();
        registerCustomNameItemBlocks();
    }

    private static <T extends Item> T prepareRegister(T item) {
        String name = item.getClass().getSimpleName().toLowerCase();
        item.setRegistryName(ModularMachinery.MODID, name).setTranslationKey(ModularMachinery.MODID + '.' + name);

        return register(item);
    }

    private static <T extends Item> T prepareRegisterWithCustomName(T item) {
        return register(item);
    }

    private static <T extends Item> T register(T item) {
        ITEM_MODEL_REGISTER.add(item);
        CommonProxy.registryPrimer.register(item);
        if (item instanceof ItemDynamicColor) {
            pendingDynamicColorItems.add((ItemDynamicColor) item);
        }
        return item;
    }

    private static void registerItemBlocks() {
        ITEM_BLOCKS.forEach(RegistryItems::register);
    }

    private static void registerItemModels() {
        ITEM_MODEL_REGISTER.stream()
                .filter(item -> !(item instanceof ItemBlockCustomName))
                .forEach(ModularMachinery.proxy::registerItemModel);
    }

    private static void registerCustomNameItemBlocks() {
        ITEM_BLOCKS_WITH_CUSTOM_NAME.forEach(item -> {
            CommonProxy.registryPrimer.register(item);
            if (item instanceof ItemDynamicColor) {
                pendingDynamicColorItems.add((ItemDynamicColor) item);
            }
            ModularMachinery.proxy.registerItemModelWithCustomName(item);
        });
    }

}
