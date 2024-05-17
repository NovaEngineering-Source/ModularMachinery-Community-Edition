package ink.ikx.mmce.core;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import github.kasuminova.mmce.common.network.PktAutoAssemblyRequest;
import github.kasuminova.mmce.common.util.DynamicPattern;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.client.ClientProxy;
import hellfirepvp.modularmachinery.client.util.DynamicMachineRenderContext;
import hellfirepvp.modularmachinery.common.block.BlockController;
import hellfirepvp.modularmachinery.common.block.BlockFactoryController;
import hellfirepvp.modularmachinery.common.lib.ItemsMM;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import hellfirepvp.modularmachinery.common.util.BlockArray;
import hellfirepvp.modularmachinery.common.util.BlockArrayCache;
import ink.ikx.mmce.common.assembly.MachineAssembly;
import ink.ikx.mmce.common.assembly.MachineAssemblyManager;
import ink.ikx.mmce.common.utils.StructureIngredient;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("MethodMayBeStatic")
public class AssemblyEventHandler {
    public static final AssemblyEventHandler INSTANCE = new AssemblyEventHandler();

    private static final Cache<EntityPlayer, Boolean> ASSEMBLY_ACCESS_TOKEN = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.SECONDS)
            .build();

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        EntityPlayer player = event.getEntityPlayer();
        ItemStack stack = event.getHand() == EnumHand.MAIN_HAND ? event.getItemStack() : player.getHeldItem(EnumHand.MAIN_HAND);

        if (player.isSneaking()) {
            return;
        }
        if (!(world.getTileEntity(pos) instanceof TileMultiblockMachineController ctrl)) {
            return;
        }

        // Blueprints
        if (stack.getItem().equals(ItemsMM.blueprint)) {
            if (world.isRemote || event.getHand() != EnumHand.MAIN_HAND) {
                return;
            }
            if (getBlueprint(ctrl).isEmpty()) {
                ItemStack copy = stack.copy();
                copy.setCount(1);
                if (!player.isCreative()) stack.setCount(stack.getCount() - 1);
                ctrl.getInventory().setStackInSlot(TileMultiblockMachineController.BLUEPRINT_SLOT, copy);
            }
            event.setCanceled(true);
            event.setCancellationResult(EnumActionResult.SUCCESS);
            return;
        }

        // Machine assembly
        Item item = Optional.ofNullable(Item.getByNameOrId(AssemblyConfig.itemName)).orElse(Items.STICK);
        if (stack.isItemEqual(new ItemStack(item, 1, AssemblyConfig.itemMeta))) {
            if (world.isRemote && event.getHand() == EnumHand.MAIN_HAND) {
                sendAssemblyRequestToServer(pos);
            }
            if (!world.isRemote && event.getHand() == EnumHand.MAIN_HAND) {
                if (ASSEMBLY_ACCESS_TOKEN.getIfPresent(player) == null) {
                    ASSEMBLY_ACCESS_TOKEN.put(player, true);
                }
            }
            event.setCanceled(true);
            event.setCancellationResult(EnumActionResult.SUCCESS);
        }
    }

    @SideOnly(Side.CLIENT)
    private void sendAssemblyRequestToServer(final BlockPos pos) {
        // Ensure proper server-side judgment.
        ClientProxy.clientScheduler.addRunnable(() -> {
            int dynamicPatternSize = 0;
            DynamicMachineRenderContext context = ClientProxy.renderHelper.getContext();
            if (context != null) {
                dynamicPatternSize = context.getDynamicPatternSize();
            }
            ModularMachinery.NET_CHANNEL.sendToServer(new PktAutoAssemblyRequest(pos, (short) dynamicPatternSize));
        }, 1);
    }

    public void processAutoAssembly(final EntityPlayer player,
                                    final ItemStack handStack,
                                    final BlockPos pos,
                                    final short dynamicPatternSize)
    {
        Boolean access = ASSEMBLY_ACCESS_TOKEN.getIfPresent(player);
        if (access == null) {
            return;
        }
        if (!access) {
            player.sendMessage(new TextComponentTranslation("message.assembly.tip.too_quickly"));
            return;
        }
        ASSEMBLY_ACCESS_TOKEN.put(player, false);

        World world = player.world;
        TileEntity te = world.getTileEntity(pos);
        Block block = world.getBlockState(pos).getBlock();

        Item item = Item.getByNameOrId(AssemblyConfig.itemName);
        if (item == null) item = Items.STICK;

        if (player.isSneaking()) {
            return;
        }

        if (!(te instanceof final TileMultiblockMachineController ctrl)) {
            return;
        }

        if (handStack.isItemEqual(new ItemStack(item, 1, AssemblyConfig.itemMeta))) {
            DynamicMachine machine = ctrl.getBlueprintMachine();
            if (machine == null) {
                if (block instanceof BlockController) {
                    machine = ((BlockController) block).getParentMachine();
                }
                if (block instanceof BlockFactoryController) {
                    machine = ((BlockFactoryController) block).getParentMachine();
                }
            }

            assemblyBefore(machine, player, pos, dynamicPatternSize);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        World world = player.world;
        if (event.phase == TickEvent.Phase.START || world.isRemote || world.getTotalWorldTime() % AssemblyConfig.tickBlock != 0) {
            return;
        }

        Collection<MachineAssembly> assemblies = MachineAssemblyManager.getMachineAssemblyListFromPlayer(player);

        if (assemblies == null || assemblies.isEmpty()) {
            return;
        }

        ModularMachinery.EXECUTE_MANAGER.addSyncTask(() -> {
            for (final MachineAssembly assembly : assemblies) {
                if (assembly.isControllerInvalid()) {
                    MachineAssemblyManager.removeMachineAssembly(assembly.getCtrlPos());
                    player.sendMessage(new TextComponentTranslation("message.assembly.tip.cancelled"));
                    return;
                }
                assembly.assembly(true);
                if (assembly.isCompleted()) {
                    MachineAssemblyManager.removeMachineAssembly(assembly.getCtrlPos());
                    player.sendMessage(new TextComponentTranslation("message.assembly.tip.success"));
                }
            }
        });
    }

    @SubscribeEvent
    public void onPlayerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        MachineAssemblyManager.removeMachineAssembly(event.player);
    }

    private static void assemblyBefore(DynamicMachine machine, EntityPlayer player, BlockPos pos, int dynamicPatternSize) {
        if (machine == null) {
            player.sendMessage(new TextComponentTranslation("message.assembly.tip.no_machine"));
            return;
        }

        if (MachineAssemblyManager.checkMachineExist(pos)) {
            player.sendMessage(new TextComponentTranslation("message.assembly.tip.already_assembly"));
            return;
        }

        EnumFacing controllerFacing = player.world.getBlockState(pos).getValue(BlockController.FACING);
        BlockArray machinePattern = new BlockArray(BlockArrayCache.getBlockArrayCache(machine.getPattern(), controllerFacing));

        Map<String, DynamicPattern> dynamicPatterns = machine.getDynamicPatterns();
        for (final DynamicPattern pattern : dynamicPatterns.values()) {
            dynamicPatternSize = Math.max(dynamicPatternSize, pattern.getMinSize());
        }

        for (final DynamicPattern pattern : dynamicPatterns.values()) {
            pattern.addPatternToBlockArray(
                    machinePattern,
                    Math.min(Math.max(pattern.getMinSize(), dynamicPatternSize), pattern.getMaxSize()),
                    pattern.getFaces().iterator().next(),
                    controllerFacing);
        }

        MachineAssembly assembly = new MachineAssembly(
                player.world, pos, player,
                StructureIngredient.of(player.world, pos, machinePattern)
        );

        if (player.isCreative()) {
            assembly.assemblyCreative();
            return;
        }

        if (MachineAssembly.checkAllItems(player, assembly.getIngredient().copy())) {
            assembly.buildIngredients(false);
            MachineAssemblyManager.addMachineAssembly(assembly);
            return;
        }

        if (!AssemblyConfig.needAllBlocks) {
            player.sendMessage(new TextComponentTranslation("message.assembly.tip.partial_assembly"));
            assembly.buildIngredients(false);
            MachineAssemblyManager.addMachineAssembly(assembly);
        }
    }

    private static ItemStack getBlueprint(TileMultiblockMachineController controller) {
        return controller.getInventory().getStackInSlot(TileMultiblockMachineController.BLUEPRINT_SLOT);
    }
}
