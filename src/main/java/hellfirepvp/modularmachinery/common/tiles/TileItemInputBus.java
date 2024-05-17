/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.tiles;

import github.kasuminova.mmce.common.tile.MEPatternProvider;
import github.kasuminova.mmce.common.tile.base.MEItemBus;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.block.prop.ItemBusSize;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.tiles.base.MachineComponentTile;
import hellfirepvp.modularmachinery.common.tiles.base.TileInventory;
import hellfirepvp.modularmachinery.common.tiles.base.TileItemBus;
import hellfirepvp.modularmachinery.common.util.IOInventory;
import hellfirepvp.modularmachinery.common.util.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileItemInputBus
 * Created by HellFirePvP
 * Date: 07.07.2017 / 17:54
 */
public class TileItemInputBus extends TileItemBus implements MachineComponentTile {
    public static int minWorkDelay = 5;
    public static int maxWorkDelay = 60;

    public TileItemInputBus() {
    }

    public TileItemInputBus(ItemBusSize type) {
        super(type);
    }

    @Override
    public void doRestrictedTick() {
        if (getWorld().isRemote || !canWork(minWorkDelay, maxWorkDelay)) {
            return;
        }

        for (EnumFacing facing : EnumFacing.VALUES) {
            BlockPos offset = getPos().offset(facing);
            TileEntity te = getWorld().getTileEntity(offset);
            if (te == null || te instanceof TileItemBus) {
                continue;
            }

            if (Mods.AE2.isPresent() && ((te instanceof MEItemBus) || (te instanceof MEPatternProvider))) {
                continue;
            }

            EnumFacing accessingSide = facing.getOpposite();

            IItemHandler itemHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, accessingSide);
            if (itemHandler == null) {
                continue;
            }

            inputFromExternal(itemHandler);
        }
    }

    private synchronized void inputFromExternal(IItemHandler external) {
        boolean successAtLeastOnce = false;

        external:
        for (int externalSlotId = 0; externalSlotId < external.getSlots(); externalSlotId++) {
            ItemStack externalStack = external.getStackInSlot(externalSlotId);
            if (externalStack.isEmpty()) {
                continue;
            }

            for (int internalSlotId = 0; internalSlotId < inventory.getSlots(); internalSlotId++) {
                ItemStack internalStack = inventory.getStackInSlot(internalSlotId);
                int maxCanExtract = Math.min(externalStack.getCount(), externalStack.getMaxStackSize());

                if (internalStack.isEmpty()) {
                    // Extract external item and insert to internal.
                    ItemStack extracted = external.extractItem(externalSlotId, maxCanExtract, false);
                    inventory.setStackInSlot(internalSlotId, extracted);
                    successAtLeastOnce = true;
                    // If there are no more items in the current slot, check the next external slot.
                    if (external.getStackInSlot(externalSlotId).isEmpty()) {
                        continue external;
                    }
                    continue;
                }

                if (internalStack.getCount() >= internalStack.getMaxStackSize() || !ItemUtils.matchStacks(internalStack, externalStack)) {
                    continue;
                }

                int extractAmt = Math.min(
                        internalStack.getMaxStackSize() - internalStack.getCount(),
                        maxCanExtract);

                // Extract external item and insert to internal.
                ItemStack extracted = external.extractItem(externalSlotId, extractAmt, false);
                inventory.setStackInSlot(internalSlotId,
                        ItemUtils.copyStackWithSize(
                                extracted, internalStack.getCount() + extracted.getCount()));
                successAtLeastOnce = true;
                // If there are no more items in the current slot, check the next external slot.
                if (external.getStackInSlot(externalSlotId).isEmpty()) {
                    continue external;
                }
            }
        }

        if (successAtLeastOnce) {
            incrementSuccessCounter(maxWorkDelay, minWorkDelay);
            markNoUpdate();
        } else {
            decrementSuccessCounter();
        }
    }

    @Override
    public IOInventory buildInventory(TileInventory tile, int size) {
        int[] slots = new int[size];
        for (int i = 0; i < size; i++) {
            slots[i] = i;
        }
        return new IOInventory(tile, slots, new int[]{});
    }

    @Nullable
    @Override
    public MachineComponent.ItemBus provideComponent() {
        return new MachineComponent.ItemBus(IOType.INPUT) {
            @Override
            public IOInventory getContainerProvider() {
                return TileItemInputBus.this.inventory;
            }
        };
    }

}
