package github.kasuminova.mmce.common.container;

import appeng.api.config.SecurityPermissions;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.IConfigManager;
import appeng.container.slot.SlotRestrictedInput;
import appeng.fluids.container.ContainerFluidConfigurable;
import appeng.fluids.helper.FluidSyncHelper;
import appeng.fluids.util.IAEFluidTank;
import appeng.util.Platform;
import github.kasuminova.mmce.common.tile.MEFluidInputBus;
import github.kasuminova.mmce.common.tile.base.MEFluidBus;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

public class ContainerMEFluidInputBus extends ContainerFluidConfigurable {
    private final MEFluidInputBus owner;
    private final FluidSyncHelper tankSync;

    public ContainerMEFluidInputBus(final MEFluidInputBus owner, final EntityPlayer player) {
        super(player.inventory, owner);
        this.owner = owner;
        this.tankSync = new FluidSyncHelper(owner.getTanks(), MEFluidBus.TANK_SLOT_AMOUNT);
    }

    public MEFluidInputBus getOwner() {
        return owner;
    }

    @Override
    protected int getHeight() {
        return 231;
    }

    @Override
    public IAEFluidTank getFluidConfigInventory() {
        return owner.getConfig();
    }

    @Override
    public void detectAndSendChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (Platform.isServer()) {
            this.tankSync.sendDiff(this.listeners);
        }

        super.detectAndSendChanges();
    }

    @Override
    protected void setupConfig() {
        setupUpgrades();
    }

    @Override
    protected void loadSettingsFromHost(final IConfigManager cm) {
    }

    @Override
    protected void setupUpgrades() {
        final IItemHandler upgrades = this.getUpgradeable().getInventoryByName("upgrades");
        for (int i = 0; i < availableUpgrades(); i++) {
            this.addSlotToContainer(
                (new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, i, 187, 8 + (18 * i), this.getInventoryPlayer()))
                    .setNotDraggable());
        }
    }

    @Override
    public void addListener(@Nonnull IContainerListener listener) {
        super.addListener(listener);
        this.tankSync.sendFull(Collections.singleton(listener));
    }

    @Override
    public void receiveFluidSlots(final Map<Integer, IAEFluidStack> fluids) {
        super.receiveFluidSlots(fluids);
        this.tankSync.readPacket(fluids);
    }

    @Override
    protected boolean supportCapacity() {
        return false;
    }

    @Override
    public int availableUpgrades() {
        return 5;
    }

    @Override
    public boolean hasToolbox() {
        return false;
    }
}
