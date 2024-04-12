package ink.ikx.mmce.common.assembly;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.network.PktAssemblyReport;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import hellfirepvp.modularmachinery.common.util.ItemUtils;
import ink.ikx.mmce.common.utils.FluidUtils;
import ink.ikx.mmce.common.utils.StructureIngredient;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class MachineAssembly {
    private final World world;
    private final BlockPos ctrlPos;
    private final EntityPlayer player;
    private StructureIngredient ingredient;

    public MachineAssembly(final World world, final BlockPos ctrlPos, final EntityPlayer player, final StructureIngredient ingredient) {
        this.world = world;
        this.ctrlPos = ctrlPos;
        this.player = player;
        this.ingredient = ingredient;
    }

    public World getWorld() {
        return world;
    }

    public BlockPos getCtrlPos() {
        return ctrlPos;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    public StructureIngredient getIngredient() {
        return ingredient;
    }

    public void buildIngredients(boolean consumeInventory) {
        List<ItemStack> inventory = player.inventory.mainInventory;
        if (!consumeInventory) {
            inventory = inventory.stream()
                    .map(ItemStack::copy)
                    .collect(Collectors.toList());
        }

        List<StructureIngredient.ItemIngredient> newItemIngredients = buildItemIngredients(inventory, ingredient.itemIngredient());
        List<StructureIngredient.FluidIngredient> newFluidIngredients = buildFluidIngredients(inventory, ingredient.fluidIngredient());

        ingredient = new StructureIngredient(newItemIngredients, newFluidIngredients);
    }

    public static List<StructureIngredient.FluidIngredient> buildFluidIngredients(final List<ItemStack> inventory,
                                                                                  final List<StructureIngredient.FluidIngredient> fluidIngredients) {
        List<StructureIngredient.FluidIngredient> result = new ArrayList<>();
        List<IFluidHandlerItem> fluidHandlers = getFluidHandlerItems(inventory);

        Iterator<StructureIngredient.FluidIngredient> fluidIngredientIter = fluidIngredients.iterator();
        fluidIngredient:
        while (fluidIngredientIter.hasNext()) {
            final StructureIngredient.FluidIngredient fluidIngredient = fluidIngredientIter.next();
            final BlockPos pos = fluidIngredient.pos();

            for (final Tuple<FluidStack, IBlockState> tuple : fluidIngredient.ingredientList()) {
                FluidStack required = tuple.getFirst();
                IBlockState state = tuple.getSecond();

                if (!consumeInventoryFluid(required, fluidHandlers)) {
                    continue;
                }

                result.add(new StructureIngredient.FluidIngredient(
                        pos, Collections.singletonList(new Tuple<>(required, state)))
                );
                fluidIngredientIter.remove();
                continue fluidIngredient;
            }
        }

        return result;
    }

    private static List<IFluidHandlerItem> getFluidHandlerItems(final List<ItemStack> inventory) {
        List<IFluidHandlerItem> fluidHandlers = new ArrayList<>();
        for (final ItemStack invStack : inventory) {
            Item item = invStack.getItem();
            // TODO Bucket are not supported at this time.
            if (item instanceof UniversalBucket || item == Items.LAVA_BUCKET || item == Items.WATER_BUCKET) {
                continue;
            }
            if (!FluidUtils.isFluidHandler(invStack)) {
                continue;
            }
            IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(invStack);
            if (fluidHandler != null) {
                fluidHandlers.add(fluidHandler);
            }
        }
        return fluidHandlers;
    }

    public static List<StructureIngredient.ItemIngredient> buildItemIngredients(final List<ItemStack> inventory,
                                                                                final List<StructureIngredient.ItemIngredient> itemIngredients) {
        List<StructureIngredient.ItemIngredient> result = new ArrayList<>();

        Iterator<StructureIngredient.ItemIngredient> iterator = itemIngredients.iterator();
        itemIngredient:
        while (iterator.hasNext()) {
            StructureIngredient.ItemIngredient ingredient = iterator.next();

            for (final Tuple<ItemStack, IBlockState> tuple : ingredient.ingredientList()) {
                ItemStack required = tuple.getFirst();
                if (!consumeInventoryItem(required, inventory)) {
                    continue;
                }

                result.add(new StructureIngredient.ItemIngredient(
                        ingredient.pos(), Collections.singletonList(tuple), ingredient.nbt())
                );

                iterator.remove();
                continue itemIngredient;
            }
        }

        return result;
    }

    public static void searchAndRemoveContainItem(final List<ItemStack> inventory,
                                                  final List<StructureIngredient.ItemIngredient> itemIngredients) {
        Iterator<StructureIngredient.ItemIngredient> itemIngredientIter = itemIngredients.iterator();
        itemIngredient:
        while (itemIngredientIter.hasNext()) {
            final StructureIngredient.ItemIngredient ingredient = itemIngredientIter.next();

            for (final Tuple<ItemStack, IBlockState> tuple : ingredient.ingredientList()) {
                ItemStack required = tuple.getFirst();
                if (required.isEmpty() || consumeInventoryItem(required, inventory)) {
                    itemIngredientIter.remove();
                    continue itemIngredient;
                }
            }
        }
    }

    public static void searchAndRemoveContainFluid(final List<ItemStack> inventory,
                                                   final List<StructureIngredient.FluidIngredient> fluidIngredients) {
        List<IFluidHandlerItem> fluidHandlers = getFluidHandlerItems(inventory);

        Iterator<StructureIngredient.FluidIngredient> fluidIngredientIter = fluidIngredients.iterator();
        fluidIngredient:
        while (fluidIngredientIter.hasNext()) {
            final StructureIngredient.FluidIngredient fluidIngredient = fluidIngredientIter.next();

            for (final Tuple<FluidStack, IBlockState> tuple : fluidIngredient.ingredientList()) {
                FluidStack required = tuple.getFirst();

                if (consumeInventoryFluid(required, fluidHandlers)) {
                    fluidIngredientIter.remove();
                    continue fluidIngredient;
                }
            }
        }
    }

    public static boolean checkAllItems(EntityPlayer player, StructureIngredient ingredient) {
        List<ItemStack> inventory = player.inventory.mainInventory.stream()
                .map(ItemStack::copy)
                .collect(Collectors.toList());

        List<StructureIngredient.ItemIngredient> itemIngredientList = ingredient.itemIngredient();
        List<StructureIngredient.FluidIngredient> fluidIngredientList = ingredient.fluidIngredient();
        searchAndRemoveContainItem(inventory, itemIngredientList);
        searchAndRemoveContainFluid(inventory, fluidIngredientList);

        if (itemIngredientList.isEmpty() && fluidIngredientList.isEmpty()) {
            return true;
        }
        List<List<ItemStack>> itemStackIngList = getItemStackIngList(itemIngredientList);
        List<List<FluidStack>> fluidStackIngList = getFluidStackIngList(fluidIngredientList);

        PktAssemblyReport pkt = new PktAssemblyReport(
                itemStackIngList,
                fluidStackIngList);

        if (player instanceof EntityPlayerMP) {
            ModularMachinery.NET_CHANNEL.sendTo(pkt, (EntityPlayerMP) player);
        }

        return false;
    }

    private static List<List<FluidStack>> getFluidStackIngList(final List<StructureIngredient.FluidIngredient> fluidIngredientList) {
        List<List<FluidStack>> fluidStackIngList = new ArrayList<>();

        fluidIng:
        for (final StructureIngredient.FluidIngredient ingredient : fluidIngredientList) {
            if (ingredient.ingredientList().isEmpty()) {
                continue;
            }

            List<FluidStack> stackIngList = ingredient.ingredientList()
                    .stream()
                    .map(Tuple::getFirst)
                    .collect(Collectors.toList());

            if (stackIngList.size() == 1) {
                FluidStack ing = stackIngList.get(0);

                for (final List<FluidStack> fluidStackList : fluidStackIngList) {
                    if (fluidStackList.size() != 1) {
                        continue;
                    }

                    FluidStack another = fluidStackList.get(0);
                    if (ing.isFluidEqual(another)) {
                        another.amount += 1000;
                        continue fluidIng;
                    }
                }
            }

            fluidStackIngList.add(stackIngList);
        }

        return fluidStackIngList;
    }

    private static List<List<ItemStack>> getItemStackIngList(final List<StructureIngredient.ItemIngredient> itemIngredientList) {
        List<List<ItemStack>> stackList = new ArrayList<>();

        itemIng:
        for (final StructureIngredient.ItemIngredient itemIng : itemIngredientList) {
            if (itemIng.ingredientList().isEmpty()) {
                continue;
            }

            @SuppressWarnings("SimplifyStreamApiCallChains")
            List<ItemStack> stackIngList = itemIng.ingredientList()
                    .stream()
                    .map(Tuple::getFirst)
                    .collect(Collectors.toList());
            if (stackIngList.size() == 1) {
                ItemStack ing = stackIngList.get(0);

                for (final List<ItemStack> itemStackList : stackList) {
                    if (itemStackList.size() != 1) {
                        continue;
                    }

                    ItemStack anotherInput = itemStackList.get(0);
                    if (ItemUtils.matchStacks(ing, anotherInput)) {
                        anotherInput.grow(1);
                        continue itemIng;
                    }
                }
            }

            List<ItemStack> filteredStackIngList = new ArrayList<>();

            filter:
            for (final ItemStack stack : stackIngList) {
                for (final ItemStack filtered : filteredStackIngList) {
                    if (ItemUtils.matchStacks(stack, filtered)) {
                        continue filter;
                    }
                }

                filteredStackIngList.add(stack);
            }

            stackList.add(filteredStackIngList);
        }

        return stackList;
    }

    public boolean isCompleted() {
        return ingredient.itemIngredient().isEmpty() && ingredient.fluidIngredient().isEmpty();
    }

    public boolean isControllerInvalid() {
        TileEntity te = world.getTileEntity(ctrlPos);
        return !(te instanceof TileMultiblockMachineController);
    }

    public void assembly(boolean consumeInventory) {
        List<StructureIngredient.ItemIngredient> itemIngredient = ingredient.itemIngredient();
        List<StructureIngredient.FluidIngredient> fluidIngredient = ingredient.fluidIngredient();

        if (!itemIngredient.isEmpty()) {
            assemblyItemBlocks(consumeInventory, itemIngredient);
        } else if (!fluidIngredient.isEmpty()) {
            assemblyFluidBlocks(consumeInventory, fluidIngredient);
        }
    }

    public void assemblyCreative() {
        for (final StructureIngredient.ItemIngredient itemIngredient : ingredient.itemIngredient()) {
            List<Tuple<ItemStack, IBlockState>> ingredientList = itemIngredient.ingredientList();
            if (ingredientList.isEmpty()) continue;

            IBlockState state = ingredientList.get(0).getSecond();
            world.setBlockState(ctrlPos.add(itemIngredient.pos()), state);
        }
        for (final StructureIngredient.FluidIngredient fluidIngredient : ingredient.fluidIngredient()) {
            List<Tuple<FluidStack, IBlockState>> ingredientList = fluidIngredient.ingredientList();
            if (ingredientList.isEmpty()) continue;

            IBlockState state = ingredientList.get(0).getSecond();
            world.setBlockState(ctrlPos.add(fluidIngredient.pos()), state);
        }
        world.playSound(null, ctrlPos, SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
        player.sendMessage(new TextComponentTranslation("message.assembly.tip.success"));
    }

    private void assemblyItemBlocks(final boolean consumeInventory, final List<StructureIngredient.ItemIngredient> itemIngredient) {
        Iterator<StructureIngredient.ItemIngredient> iterator = itemIngredient.iterator();
        StructureIngredient.ItemIngredient ingredient = iterator.next();
        BlockPos realPos = ctrlPos.add(ingredient.pos());

        if (!replaceCheck(realPos, world, player)) {
            iterator.remove();
            return;
        }

        Tuple<ItemStack, IBlockState> tuple = ingredient.ingredientList().get(0);
        ItemStack required = tuple.getFirst();
        IBlockState state = tuple.getSecond();

        if (consumeInventory && !consumeInventoryItem(required, player.inventory.mainInventory)) {
            String posToString = hellfirepvp.modularmachinery.common.util.MiscUtils.posToString(realPos);
            player.sendMessage(new TextComponentTranslation("message.assembly.tip.missing", posToString));

            List<List<ItemStack>> itemStackIngList = new ArrayList<>();
            itemStackIngList.add(Collections.singletonList(required));
            PktAssemblyReport pkt = new PktAssemblyReport(
                    itemStackIngList,
                    new ArrayList<>());
            if (player instanceof EntityPlayerMP) {
                ModularMachinery.NET_CHANNEL.sendTo(pkt, (EntityPlayerMP) player);
            }
            player.sendMessage(new TextComponentTranslation("message.assembly.tip.skipped", posToString));

            iterator.remove();
            return;
        }

        world.setBlockState(realPos, state);
        world.playSound(null, realPos, SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1.0F, 1.0F);
        TileEntity te = world.getTileEntity(realPos);
        if (te != null && ingredient.nbt() != null) {
            try {
                te.readFromNBT(ingredient.nbt());
            } catch (Exception e) {
                ModularMachinery.log.warn("Failed to apply NBT to TileEntity!", e);
                world.removeTileEntity(realPos);
                world.setTileEntity(realPos, state.getBlock().createTileEntity(world, state));
            }
        }
        iterator.remove();
    }

    private void assemblyFluidBlocks(final boolean consumeInventory, final List<StructureIngredient.FluidIngredient> fluidIngredient) {
        Iterator<StructureIngredient.FluidIngredient> iterator = fluidIngredient.iterator();
        StructureIngredient.FluidIngredient ingredient = iterator.next();
        BlockPos realPos = ctrlPos.add(ingredient.pos());

        if (!replaceCheck(realPos, world, player)) {
            iterator.remove();
            return;
        }

        Tuple<FluidStack, IBlockState> tuple = ingredient.ingredientList().get(0);
        FluidStack required = tuple.getFirst();
        IBlockState state = tuple.getSecond();

        if (consumeInventory && !consumeInventoryFluid(required, getFluidHandlerItems(player.inventory.mainInventory))) {
            String posToString = hellfirepvp.modularmachinery.common.util.MiscUtils.posToString(realPos);
            player.sendMessage(new TextComponentTranslation("message.assembly.tip.missing", posToString));

            List<List<FluidStack>> fluidStackIngList = new ArrayList<>();
            fluidStackIngList.add(Collections.singletonList(required));
            PktAssemblyReport pkt = new PktAssemblyReport(
                    new ArrayList<>(),
                    fluidStackIngList);
            if (player instanceof EntityPlayerMP) {
                ModularMachinery.NET_CHANNEL.sendTo(pkt, (EntityPlayerMP) player);
            }
            player.sendMessage(new TextComponentTranslation("message.assembly.tip.skipped", posToString));

            iterator.remove();
            return;
        }

        world.setBlockState(realPos, state);
        world.playSound(null, realPos, SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
        iterator.remove();
    }

    public static boolean consumeInventoryItem(final ItemStack required, final List<ItemStack> inventory) {
        for (final ItemStack invStack : inventory) {
            if (ItemUtils.matchStacks(invStack, required)) {
                invStack.shrink(required.getCount());
                return true;
            }
        }
        return false;
    }

    public static boolean consumeInventoryFluid(final FluidStack required, final List<IFluidHandlerItem> fluidHandlers) {
        for (final IFluidHandlerItem fluidHandler : fluidHandlers) {
            FluidStack drained = fluidHandler.drain(required.copy(), false);
            if (drained == null || !drained.containsFluid(required)) {
                continue;
            }

            fluidHandler.drain(required.copy(), true);
            return true;
        }
        return false;
    }

    public static boolean replaceCheck(final BlockPos realPos, final World world, final EntityPlayer player) {
        IBlockState blockState = world.getBlockState(realPos);
        Block block = blockState.getBlock();
        if (world.isAirBlock(realPos) ||
                block instanceof IPlantable ||
                block instanceof BlockLiquid ||
                block instanceof IFluidBlock)
        {
            return true;
        }

        String posToString = hellfirepvp.modularmachinery.common.util.MiscUtils.posToString(realPos);
        player.sendMessage(new TextComponentTranslation("message.assembly.tip.cannot_replace", posToString));
        player.sendMessage(new TextComponentTranslation("message.assembly.tip.skipped", posToString));

        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof final MachineAssembly another)) return false;

        return ctrlPos.equals(another.ctrlPos);
    }

    @Override
    public int hashCode() {
        return ctrlPos != null ? ctrlPos.hashCode() : 0;
    }
}
