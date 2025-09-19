package ink.ikx.mmce.core;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Configuration;

public class AssemblyConfig {

    @Config.Comment("Set the Item auto-assembly, default: minecraft:stick")
    public static String itemName = "minecraft:stick";

    @Config.Comment("Set the Item's meta, e.g. 0")
    public static int itemMeta = 0;

    @Config.Comment("Set how many ticks to assemble the block once, default: 5")
    public static int tickBlock = 5;

    @Config.Comment("Set the auto-assembly need all blocks must be in the inventory, default: true")
    public static boolean needAllBlocks = true;

    @Config.Comment("Set whether to skip blocks containing NBTs, default: false")
    public static boolean skipBlockContainNBT = false;

    public static void loadFormConfig(Configuration config) {
        itemName = config.getString("itemName", "auto-assembly", "minecraft:stick",
            "Set the Item auto-assembly.");
        itemMeta = config.getInt("itemMeta", "auto-assembly", 0,
            0, 32767, "Set the Item's meta, e.g. 0");
        tickBlock = config.getInt("tickBlock", "auto-assembly", 5,
            1, 1000, "Set how many ticks to assemble the block once.");
        needAllBlocks = config.getBoolean("needAllBlocks", "auto-assembly", true,
            "Set the auto-assembly need all blocks must be in the inventory.");
        skipBlockContainNBT = config.getBoolean("skipBlockContainNBT", "auto-assembly", false,
            "Set whether to skip blocks containing NBTs.");
    }
}
