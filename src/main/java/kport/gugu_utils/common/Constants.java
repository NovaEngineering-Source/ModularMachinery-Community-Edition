package kport.gugu_utils.common;

import hellfirepvp.modularmachinery.ModularMachinery;
import net.minecraft.util.ResourceLocation;

public class Constants {

    //模块化机械相关
    public static final String NAME_MANA = "mana";
    public static final ResourceLocation RESOURCE_MANA = new ResourceLocation(ModularMachinery.MODID, NAME_MANA);
    public static final String STRING_RESOURCE_MANA = ModularMachinery.MODID + ":" + NAME_MANA;

    public static final ResourceLocation RESOURCE_MANA_PERTICK = new ResourceLocation(ModularMachinery.MODID, "mana_pertick");
    public static final ResourceLocation RESOURCE_STARLIGHT = new ResourceLocation(ModularMachinery.MODID, "starlight");
    public static final String STRING_RESOURCE_STARLIGHT = ModularMachinery.MODID + ":" + "starlight";

    public static final ResourceLocation RESOURCE_EMBER = new ResourceLocation(ModularMachinery.MODID, "ember");
    public static final String STRING_RESOURCE_EMBER = ModularMachinery.MODID + ":" + "ember";

    public static final ResourceLocation RESOURCE_ENVIRONMENT = new ResourceLocation(ModularMachinery.MODID, "environment");
    public static final String STRING_RESOURCE_ENVIRONMENT = ModularMachinery.MODID + ":" + "environment";

    public static final ResourceLocation RESOURCE_COMPRESSED_AIR = new ResourceLocation(ModularMachinery.MODID , "compressed_air");
    public static final ResourceLocation RESOURCE_COMPRESSED_AIR_PER_TICK = new ResourceLocation(ModularMachinery.MODID , "compressed_air_pertick");
    public static final String STRING_RESOURCE_COMPRESSED_AIR = ModularMachinery.MODID + ":" + "compressed_air";

    public static final ResourceLocation RESOURCE_HOT_AIR = new ResourceLocation(ModularMachinery.MODID , "hot_air");
    public static final String STRING_RESOURCE_HOT_AIR = ModularMachinery.MODID + ":" + "hot_air";


    //方块相关
    public static final String NAME_STARLIGHTHATCH_INPUT = "starlightinputhatch";
    public static final String NAME_ENVIRONMENTHATCH = "environmenthatch";
    public static final ResourceLocation RESOURCE_ENVIRONMENTHATCH = new ResourceLocation(ModularMachinery.MODID, NAME_ENVIRONMENTHATCH);


    public static final String NAME_PRESSUREHATCH = "pressurehatch";

    public static final String NAME_HOTAIRHATCH_INPUT = "hotairinputhatch";
    public static final ResourceLocation RESOURCE_TILE_HOTAIRHATCH_INPUT = new ResourceLocation(ModularMachinery.MODID, NAME_HOTAIRHATCH_INPUT + "_output");


    public static final String NAME_RANGED_CONSTRUCTION_TOOL = "constructionranged";


}
