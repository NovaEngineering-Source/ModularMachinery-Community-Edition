package github.kasuminova.mmce.common.container;

import appeng.api.config.SecurityPermissions;
import appeng.api.config.Upgrades;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.IConfigManager;
import appeng.container.guisync.GuiSync;
import appeng.container.implementations.ContainerUpgradeable;
import appeng.container.slot.SlotRestrictedInput;
import appeng.fluids.container.IFluidSyncContainer;
import appeng.fluids.helper.FluidSyncHelper;
import appeng.util.Platform;
import github.kasuminova.mmce.common.tile.base.MEFluidBus;
import github.kasuminova.mmce.common.util.AEFluidInventoryUpgradeable;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

public abstract class ContainerMEFluidBus extends ContainerUpgradeable implements IFluidSyncContainer {
    protected final FluidSyncHelper tankSync;
    private final   MEFluidBus      owner;
    @GuiSync(7)
    public          int             capacityUpgrades = 0;

    public ContainerMEFluidBus(final InventoryPlayer ip, final MEFluidBus te) {
        super(ip, te);
        this.owner = te;
        this.tankSync = new FluidSyncHelper(owner.getTanks(), 0);
    }

    @Override
    public void receiveFluidSlots(Map<Integer, IAEFluidStack> fluids) {
        this.tankSync.readPacket(fluids);
    }

    @Override
    protected void setupConfig() {
        setupUpgrades();
    }

    @Override
    protected void loadSettingsFromHost(final IConfigManager cm) {
    }

    @Override
    public void addListener(@Nonnull IContainerListener listener) {
        super.addListener(listener);
        this.tankSync.sendFull(Collections.singleton(listener));
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
    public void detectAndSendChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (Platform.isServer()) {
            this.tankSync.sendDiff(this.listeners);

            int installedUpgrades = getUpgradeable().getInstalledUpgrades(Upgrades.CAPACITY);
            if (capacityUpgrades != installedUpgrades) {
                capacityUpgrades = installedUpgrades;
            }
        }

        super.detectAndSendChanges();
    }

    @Override
    public void onUpdate(final String field, final Object oldValue, final Object newValue) {
        super.onUpdate(field, oldValue, newValue);
        if (Platform.isClient() && field.equals("capacityUpgrades")) {
            this.capacityUpgrades = (int) newValue;
            ((AEFluidInventoryUpgradeable) this.owner.getTanks()).setCapacity(
                (int) (Math.pow(4, this.capacityUpgrades + 1) * (MEFluidBus.TANK_DEFAULT_CAPACITY / 4)));
        }
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
