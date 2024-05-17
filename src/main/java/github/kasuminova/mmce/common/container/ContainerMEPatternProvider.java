package github.kasuminova.mmce.common.container;

import appeng.api.config.SecurityPermissions;
import appeng.api.storage.data.IAEFluidStack;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotNormal;
import appeng.container.slot.SlotRestrictedInput;
import appeng.fluids.container.IFluidSyncContainer;
import appeng.fluids.helper.FluidSyncHelper;
import appeng.helpers.InventoryAction;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.util.Platform;
import github.kasuminova.mmce.common.tile.MEPatternProvider;
import github.kasuminova.mmce.common.util.AEFluidInventoryUpgradeable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

public class ContainerMEPatternProvider extends AEBaseContainer implements IFluidSyncContainer {
    protected final FluidSyncHelper tankSync;

    private final MEPatternProvider owner;

    public ContainerMEPatternProvider(final MEPatternProvider owner, final EntityPlayer player) {
        super(player.inventory, owner);
        this.owner = owner;
        this.tankSync = new FluidSyncHelper(owner.getSubFluidHandler(), 0);

        this.bindPlayerInventory(getInventoryPlayer(), 0, 114);

        AppEngInternalInventory patterns = owner.getPatterns();
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlotToContainer(new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.ENCODED_PATTERN, patterns, (row * 9) + col, 8 + (col * 18), 28 + (row * 18), getInventoryPlayer()));
            }
        }

        AppEngInternalInventory subItemHandler = owner.getSubItemHandler();
        for (int i = 0; i < 2; i++) {
            this.addSlotToContainer(new SlotNormal(subItemHandler, i, 181 + (i * 18), 172));
        }
    }

    public void doAction(EntityPlayerMP player, InventoryAction action, int slot, long id) {
        if (action != InventoryAction.FILL_ITEM && action != InventoryAction.EMPTY_ITEM) {
            super.doAction(player, action, slot, id);
            return;
        }

        final ItemStack held = player.inventory.getItemStack();
        ItemStack heldCopy = held.copy();
        heldCopy.setCount(1);
        IFluidHandlerItem fh = FluidUtil.getFluidHandler(heldCopy);
        if (fh == null) {
            // only fluid handlers items
            return;
        }

        AEFluidInventoryUpgradeable fluidHandler = this.owner.getSubFluidHandler();
        IAEFluidStack fluidInSlot = fluidHandler.getFluidInSlot(0);
        if (action == InventoryAction.FILL_ITEM && fluidInSlot != null) {
            final IAEFluidStack stack = fluidInSlot.copy();

            // Check how much we can store in the item
            stack.setStackSize(Integer.MAX_VALUE);
            int amountAllowed = fh.fill(stack.getFluidStack(), false);
            int heldAmount = held.getCount();
            for (int i = 0; i < heldAmount; i++) {
                ItemStack copiedFluidContainer = held.copy();
                copiedFluidContainer.setCount(1);
                fh = FluidUtil.getFluidHandler(copiedFluidContainer);
                if (fh == null) {
                    // only fluid handlers items
                    continue;
                }

                FluidStack extractableFluid = fluidHandler.drain(stack.setStackSize(amountAllowed).getFluidStack(), false);
                if (extractableFluid == null || extractableFluid.amount == 0) {
                    break;
                }

                int maxCanFill = fh.fill(extractableFluid, false);
                if (maxCanFill > 0) {
                    FluidStack extractedFluid = fluidHandler.drain(extractableFluid, true);
                    fh.fill(extractedFluid, true);
                }

                if (held.getCount() == 1) {
                    player.inventory.setItemStack(fh.getContainer());
                } else {
                    player.inventory.getItemStack().shrink(1);
                    if (!player.inventory.addItemStackToInventory(fh.getContainer())) {
                        player.dropItem(fh.getContainer(), false);
                    }
                }
            }
        } else if (action == InventoryAction.EMPTY_ITEM) {
            int heldAmount = held.getCount();
            for (int i = 0; i < heldAmount; i++) {
                ItemStack copiedFluidContainer = held.copy();
                copiedFluidContainer.setCount(1);
                fh = FluidUtil.getFluidHandler(copiedFluidContainer);
                if (fh == null) {
                    // only fluid handlers items
                    continue;
                }

                FluidStack maxCanDrain = fh.drain(fluidHandler.getTankProperties()[slot].getCapacity(), false);
                if (maxCanDrain != null) {
                    fh.drain(maxCanDrain, true);
                    fluidHandler.fill(maxCanDrain, true);
                }

                if (held.getCount() == 1) {
                    player.inventory.setItemStack(fh.getContainer());
                } else {
                    player.inventory.getItemStack().shrink(1);
                    if (!player.inventory.addItemStackToInventory(fh.getContainer())) {
                        player.dropItem(fh.getContainer(), false);
                    }
                }
            }
        }
        this.updateHeld(player);
    }

    @Override
    public void receiveFluidSlots(Map<Integer, IAEFluidStack> fluids) {
        this.tankSync.readPacket(fluids);
    }

    @Override
    public void addListener(@Nonnull IContainerListener listener) {
        super.addListener(listener);
        this.tankSync.sendFull(Collections.singleton(listener));
    }

    @Override
    public void detectAndSendChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (Platform.isServer()) {
            this.tankSync.sendDiff(this.listeners);
        }

        super.detectAndSendChanges();
    }

    public MEPatternProvider getOwner() {
        return owner;
    }

}
