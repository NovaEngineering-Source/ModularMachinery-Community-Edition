package kport.modularmagic.common.item;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.item.ItemDynamicColor;
import net.minecraft.item.Item;

import java.util.ArrayList;

public class ModularMagicItems {

    public static final ArrayList<Item>             ITEMS       = new ArrayList<>();
    public static final ArrayList<ItemDynamicColor> COLOR_ITEMS = new ArrayList<>();

    public static void initItems() {

    }

    public static void registerItem(String id, Item item) {
        item.setRegistryName(ModularMachinery.MODID, id);
        item.setTranslationKey(id);
        ITEMS.add(item);
    }
}
