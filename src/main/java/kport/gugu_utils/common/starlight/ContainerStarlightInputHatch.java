package kport.gugu_utils.common.starlight;

import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.modularmachinery.ModularMachinery;
import kport.gugu_utils.network.PacketStarlight;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Objects;

public class ContainerStarlightInputHatch extends Container {
    private final TileStarlightInputHatch te;
    private int clientStarlightStored;
    private IConstellation clientFocusedConstellation;
    private int clientLastRequirement;

    public int getClientStarlightStored() {
        return clientStarlightStored;
    }

    public IConstellation getClientFocusedConstellation() {
        return clientFocusedConstellation;
    }

    public int getClientLastRequirement() {
        return clientLastRequirement;
    }

    public ContainerStarlightInputHatch(IInventory playerInventory, TileStarlightInputHatch te) {
        this.te = te;
        this.clientStarlightStored = te.getStarlightStored();
        this.clientFocusedConstellation = te.getFocusedConstellation();
        this.clientLastRequirement = te.getLastRequirement();

        // This container references items out of our own inventory (the 9 slots we hold ourselves)
        // as well as the slots from the player inventory so that the user can transfer items between
        // both inventories. The two calls below make sure that slots are defined for both inventories.
        addPlayerSlots(playerInventory);
    }

    private void addPlayerSlots(IInventory playerInventory) {
        // Slots for the main inventory
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                int x = 8 + col * 18;
                int y = row * 18 + 84;
                this.addSlotToContainer(new Slot(playerInventory, col + row * 9 + 9, x, y));
            }
        }

        // Slots for the hotbar
        for (int row = 0; row < 9; ++row) {
            int x = 8 + row * 18;
            int y = 142;
            this.addSlotToContainer(new Slot(playerInventory, row, x, y));
        }
    }

    @Override
    @Nonnull
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return te.canInteractWith(playerIn);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        if (!te.getWorld().isRemote) {
            if (te.getStarlightStored() != clientStarlightStored ||
                !Objects.equals(te.getFocusedConstellation(), clientFocusedConstellation) ||
                clientLastRequirement != te.getLastRequirement()) {
                this.clientStarlightStored = te.getStarlightStored();
                this.clientFocusedConstellation = te.getFocusedConstellation();
                this.clientLastRequirement = te.getLastRequirement();

                for (IContainerListener listener : listeners) {
                    sync(listener);
                }
            }
        }
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        this.clientStarlightStored = te.getStarlightStored();
        this.clientFocusedConstellation = te.getFocusedConstellation();
        this.clientLastRequirement = te.getLastRequirement();
        sync(listener);
    }

    public void sync(IContainerListener listener) {
        if (listener instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) listener;
            ModularMachinery.NET_CHANNEL.sendTo(new PacketStarlight(te.getStarlightStored(), te.getLastRequirement(), te.getFocusedConstellation()), player);
        }
    }


    public void sync(int starlightAmount, int starlightRequirement, IConstellation constellation) {
        this.clientStarlightStored = starlightAmount;
        this.clientFocusedConstellation = constellation;
        this.clientLastRequirement = starlightRequirement;
    }
}
