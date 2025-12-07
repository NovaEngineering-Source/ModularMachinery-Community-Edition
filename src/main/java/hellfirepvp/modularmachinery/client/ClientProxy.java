/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.client;

import github.kasuminova.mmce.client.gui.*;
import github.kasuminova.mmce.client.renderer.MachineControllerRenderer;
import github.kasuminova.mmce.client.resource.GeoModelExternalLoader;
import github.kasuminova.mmce.common.handler.ClientHandler;
import github.kasuminova.mmce.common.tile.MEFluidInputBus;
import github.kasuminova.mmce.common.tile.MEFluidOutputBus;
import github.kasuminova.mmce.common.tile.MEGasInputBus;
import github.kasuminova.mmce.common.tile.MEGasOutputBus;
import github.kasuminova.mmce.common.tile.MEItemInputBus;
import github.kasuminova.mmce.common.tile.MEItemOutputBus;
import github.kasuminova.mmce.common.tile.MEPatternProvider;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.client.gui.GuiContainerEnergyHatch;
import hellfirepvp.modularmachinery.client.gui.GuiContainerFluidHatch;
import hellfirepvp.modularmachinery.client.gui.GuiContainerGroupInputConfig;
import hellfirepvp.modularmachinery.client.gui.GuiContainerItemBus;
import hellfirepvp.modularmachinery.client.gui.GuiContainerParallelController;
import hellfirepvp.modularmachinery.client.gui.GuiContainerSmartInterface;
import hellfirepvp.modularmachinery.client.gui.GuiContainerUpgradeBus;
import hellfirepvp.modularmachinery.client.gui.GuiFactoryController;
import hellfirepvp.modularmachinery.client.gui.GuiMachineController;
import hellfirepvp.modularmachinery.client.gui.GuiScreenBlueprint;
import hellfirepvp.modularmachinery.client.util.BlockArrayPreviewRenderHelper;
import hellfirepvp.modularmachinery.client.util.DebugOverlayHelper;
import hellfirepvp.modularmachinery.client.util.SelectionBoxRenderHelper;
import hellfirepvp.modularmachinery.common.CommonProxy;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.block.BlockController;
import hellfirepvp.modularmachinery.common.block.BlockDynamicColor;
import hellfirepvp.modularmachinery.common.block.BlockFactoryController;
import hellfirepvp.modularmachinery.common.block.BlockVariants;
import hellfirepvp.modularmachinery.common.integration.crafttweaker.command.CommandCTReloadClient;
import hellfirepvp.modularmachinery.common.item.ItemBlueprint;
import hellfirepvp.modularmachinery.common.item.ItemDynamicColor;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.registry.RegistryBlocks;
import hellfirepvp.modularmachinery.common.registry.RegistryItems;
import hellfirepvp.modularmachinery.common.tiles.TileFactoryController;
import hellfirepvp.modularmachinery.common.tiles.TileMachineController;
import hellfirepvp.modularmachinery.common.tiles.TileParallelController;
import hellfirepvp.modularmachinery.common.tiles.TileSmartInterface;
import hellfirepvp.modularmachinery.common.tiles.TileUpgradeBus;
import hellfirepvp.modularmachinery.common.tiles.base.MachineGroupInput;
import hellfirepvp.modularmachinery.common.tiles.base.TileEnergyHatch;
import hellfirepvp.modularmachinery.common.tiles.base.TileFluidTank;
import hellfirepvp.modularmachinery.common.tiles.base.TileItemBus;
import kport.modularmagic.client.gui.GuiContainerLifeEssence;
import kport.modularmagic.client.renderer.TileAspectProviderRenderer;
import kport.modularmagic.client.renderer.TileLifeEssentiaHatchRenderer;
import kport.modularmagic.common.item.ModularMagicItems;
import kport.modularmagic.common.tile.TileAspectProvider;
import kport.modularmagic.common.tile.TileLifeEssenceProvider;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ClientProxy
 * Created by HellFirePvP
 * Date: 26.06.2017 / 21:01
 */
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = ModularMachinery.MODID)
public class ClientProxy extends CommonProxy {

    public static final ClientScheduler               clientScheduler = new ClientScheduler();
    public static final BlockArrayPreviewRenderHelper renderHelper    = new BlockArrayPreviewRenderHelper();

    private final List<Block> blockModelsToRegister          = new LinkedList<>();
    private final List<Item>  itemModelsToRegister           = new LinkedList<>();
    private final List<Item>  itemModelsCustomNameToRegister = new LinkedList<>();

    private static void registerPendingIBlockColorBlocks() {
        BlockColors colors = Minecraft.getMinecraft().getBlockColors();
        for (BlockDynamicColor dynamicColor : RegistryBlocks.pendingIBlockColorBlocks) {
            colors.registerBlockColorHandler(dynamicColor::getColorMultiplier, (Block) dynamicColor);
        }
        BlockController.MACHINE_CONTROLLERS.values().forEach(block ->
                colors.registerBlockColorHandler(block::getColorMultiplier, block)
        );
        BlockController.MOC_MACHINE_CONTROLLERS.values().forEach(block ->
                colors.registerBlockColorHandler(block::getColorMultiplier, block)
        );
        BlockFactoryController.FACTORY_CONTROLLERS.values().forEach(block ->
                colors.registerBlockColorHandler(block::getColorMultiplier, block)
        );
    }

    private static void registerPendingIItemColorItems() {
        ItemColors colors = Minecraft.getMinecraft().getItemColors();
        for (ItemDynamicColor dynamicColor : RegistryItems.pendingDynamicColorItems) {
            colors.registerItemColorHandler(dynamicColor::getColorFromItemstack, (Item) dynamicColor);
        }
        BlockController.MACHINE_CONTROLLERS.values().forEach(block ->
                colors.registerItemColorHandler(block::getColorFromItemstack, block)
        );
        BlockController.MOC_MACHINE_CONTROLLERS.values().forEach(block ->
                colors.registerItemColorHandler(block::getColorFromItemstack, block)
        );
        BlockFactoryController.FACTORY_CONTROLLERS.values().forEach(block ->
                colors.registerItemColorHandler(block::getColorFromItemstack, block)
        );
    }

    private static void registryItemModel(final Item item, final String name) {
        NonNullList<ItemStack> list = NonNullList.create();
        item.getSubItems(item.getCreativeTab(), list);
        if (!list.isEmpty()) {
            for (ItemStack i : list) {
                ModelLoader.setCustomModelResourceLocation(item, i.getItemDamage(),
                        new ModelResourceLocation(ModularMachinery.MODID + ":" + name, "inventory"));
            }
        } else {
            ModelLoader.setCustomModelResourceLocation(item, 0,
                    new ModelResourceLocation(ModularMachinery.MODID + ":" + name, "inventory"));
        }
    }

    @Override
    public void preInit() {
        MinecraftForge.EVENT_BUS.register(clientScheduler);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new DebugOverlayHelper());
        MinecraftForge.EVENT_BUS.register(new SelectionBoxRenderHelper());
        MinecraftForge.EVENT_BUS.register(new ClientHandler());

        if (Mods.TC6.isPresent()) {
            ClientRegistry.bindTileEntitySpecialRenderer(TileAspectProvider.Input.class, new TileAspectProviderRenderer());
            ClientRegistry.bindTileEntitySpecialRenderer(TileAspectProvider.Output.class, new TileAspectProviderRenderer());
        }

        if (Mods.BM2.isPresent()) {
            ClientRegistry.bindTileEntitySpecialRenderer(TileLifeEssenceProvider.Input.class, new TileLifeEssentiaHatchRenderer());
            ClientRegistry.bindTileEntitySpecialRenderer(TileLifeEssenceProvider.Output.class, new TileLifeEssentiaHatchRenderer());
        }

        if (Mods.GECKOLIB.isPresent()) {
            ClientRegistry.bindTileEntitySpecialRenderer(TileMachineController.class, MachineControllerRenderer.INSTANCE);
            ClientRegistry.bindTileEntitySpecialRenderer(TileFactoryController.class, MachineControllerRenderer.INSTANCE);
            ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(GeoModelExternalLoader.INSTANCE);
        }

        super.preInit();
    }

    @SubscribeEvent
    public void onModelRegister(ModelRegistryEvent event) {
        registerModels();
    }

    private void registerModels() {
        for (Block block : blockModelsToRegister) {
            Item i = Item.getItemFromBlock(block);
            if (block instanceof BlockVariants) {
                for (IBlockState state : ((BlockVariants) block).getValidStates()) {
                    String unlocName = block.getClass().getSimpleName().toLowerCase();
                    String name = unlocName + "_" + ((BlockVariants) block).getBlockStateName(state);
                    ModelBakery.registerItemVariants(i, new ResourceLocation(ModularMachinery.MODID, name));
                    ModelLoader.setCustomModelResourceLocation(i, block.getMetaFromState(state),
                            new ModelResourceLocation(ModularMachinery.MODID + ":" + name, "inventory"));
                }
            } else {
                ModelBakery.registerItemVariants(i, new ResourceLocation(ModularMachinery.MODID, block.getClass().getSimpleName().toLowerCase()));
                ModelLoader.setCustomModelResourceLocation(i, 0,
                        new ModelResourceLocation(ModularMachinery.MODID + ":" + block.getClass().getSimpleName().toLowerCase(), "inventory"));
            }
        }
        for (Item item : itemModelsToRegister) {
            String name = item.getClass().getSimpleName().toLowerCase();
            if (item instanceof ItemBlock) {
                name = ((ItemBlock) item).getBlock().getClass().getSimpleName().toLowerCase();
            }
            registryItemModel(item, name);
        }
        for (final Item item : itemModelsCustomNameToRegister) {
            String name = item.getRegistryName().getPath();
            registryItemModel(item, name);
        }
    }

    @Override
    public void init() {
        super.init();

        registerPendingIBlockColorBlocks();
        registerPendingIItemColorItems();


        BlockColors blockColors = Minecraft.getMinecraft().getBlockColors();
        ItemColors itemColors = Minecraft.getMinecraft().getItemColors();

        for (ItemDynamicColor item : ModularMagicItems.COLOR_ITEMS) {
            itemColors.registerItemColorHandler(item::getColorFromItemstack, (Item) item);
        }
    }

    @Override
    public void postInit() {
        super.postInit();
        if (Mods.ZEN_UTILS.isPresent()) {
            ClientCommandHandler.instance.registerCommand(new CommandCTReloadClient());
        }
        if (Mods.GECKOLIB.isPresent()) {
            GeoModelExternalLoader.INSTANCE.onReload();
        }
    }

    @Override
    public void registerBlockModel(Block block) {
        blockModelsToRegister.add(block);
    }

    @Override
    public void registerItemModel(Item item) {
        itemModelsToRegister.add(item);
    }

    public void registerItemModelWithCustomName(Item item) {
        itemModelsCustomNameToRegister.add(item);
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        GuiType type = GuiType.values()[MathHelper.clamp(ID, 0, GuiType.values().length - 1)];
        Class<? extends TileEntity> required = type.requiredTileEntity;
        TileEntity present = null;
        if (required != null) {
            TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
            if (te != null && required.isAssignableFrom(te.getClass())) {
                present = te;
            } else {
                return null;
            }
        }
        switch (type) {
            case CONTROLLER -> {
                return new GuiMachineController((TileMachineController) present, player);
            }
            case FACTORY -> {
                return new GuiFactoryController((TileFactoryController) present, player);
            }
            case BUS_INVENTORY -> {
                return new GuiContainerItemBus((TileItemBus) present, player);
            }
            case TANK_INVENTORY -> {
                return new GuiContainerFluidHatch((TileFluidTank) present, player);
            }
            case ENERGY_INVENTORY -> {
                return new GuiContainerEnergyHatch((TileEnergyHatch) present, player);
            }
            case SMART_INTERFACE -> {
                return new GuiContainerSmartInterface((TileSmartInterface) present, player);
            }
            case PARALLEL_CONTROLLER -> {
                return new GuiContainerParallelController((TileParallelController) present, player);
            }
            case UPGRADE_BUS -> {
                return new GuiContainerUpgradeBus((TileUpgradeBus) present, player);
            }
            case BLUEPRINT_PREVIEW -> {
                ItemStack stack;
                if (x == 0) {
                    stack = Minecraft.getMinecraft().player.getHeldItemMainhand();
                } else {
                    stack = Minecraft.getMinecraft().player.getHeldItemOffhand();
                }
                DynamicMachine machine = ItemBlueprint.getAssociatedMachine(stack);
                if (machine != null) {
                    return new GuiScreenBlueprint(machine);
                }
            }
            case ME_ITEM_OUTPUT_BUS -> {
                if (!Mods.AE2.isPresent()) {
                    return null;
                }
                return new GuiMEItemOutputBus((MEItemOutputBus) present, player);
            }
            case ME_ITEM_INPUT_BUS -> {
                if (!Mods.AE2.isPresent()) {
                    return null;
                }
                return new GuiMEItemInputBus((MEItemInputBus) present, player);
            }
            case ME_ITEM_OUTPUT_BUS_STACK_SIZE -> {
                return new GuiMEItemOutputBusStackSize(player.inventory, (MEItemOutputBus) present);
            }
            case ME_FLUID_OUTPUT_BUS -> {
                if (!Mods.AE2.isPresent()) {
                    return null;
                }
                return new GuiMEFluidOutputBus((MEFluidOutputBus) present, player);
            }
            case ME_FLUID_INPUT_BUS -> {
                if (!Mods.AE2.isPresent()) {
                    return null;
                }
                return new GuiMEFluidInputBus((MEFluidInputBus) present, player);
            }
            case ME_GAS_OUTPUT_BUS -> {
                if (!Mods.AE2.isPresent() || !Mods.MEKENG.isPresent()) {
                    return null;
                }
                return new GuiMEGasOutputBus((MEGasOutputBus) present, player);
            }
            case ME_GAS_INPUT_BUS -> {
                if (!Mods.AE2.isPresent() || !Mods.MEKENG.isPresent()) {
                    return null;
                }
                return new GuiMEGasInputBus((MEGasInputBus) present, player);
            }
            case ME_PATTERN_PROVIDER -> {
                if (!Mods.AE2.isPresent()) {
                    return null;
                }
                return new GuiMEPatternProvider((MEPatternProvider) present, player);
            }
            case GUI_ESSENCE_PROVIDER -> {
                if (!Mods.BM2.isPresent()) {
                    return null;
                }
                return new GuiContainerLifeEssence((TileLifeEssenceProvider) present, player);
            }
            case GUI_GROUP_INPUT_CONFIG -> {
                if (present instanceof MachineGroupInput m && m.canGroupInput()) {
                    return new GuiContainerGroupInputConfig(present, player);
                }
            }
        }
        return null;
    }
}