package github.kasuminova.mmce.common.container;

import appeng.api.config.SecurityPermissions;
import appeng.api.util.IConfigManager;
import appeng.container.slot.SlotRestrictedInput;
import appeng.util.Platform;
import com.mekeng.github.common.container.ContainerGasConfigurable;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.inventory.IGasInventory;
import com.mekeng.github.util.helpers.GasSyncHelper;
import github.kasuminova.mmce.common.tile.MEGasInputBus;
import github.kasuminova.mmce.common.tile.base.MEGasBus;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

public class ContainerMEGasInputBus extends ContainerGasConfigurable<MEGasInputBus> {
    private final GasSyncHelper tankSync;

    public ContainerMEGasInputBus(final MEGasInputBus owner, final EntityPlayer player) {
        super(player.inventory, owner);
        this.tankSync = GasSyncHelper.create(owner.getTanks(), MEGasBus.TANK_SLOT_AMOUNT);
    }

    public MEGasInputBus getOwner() {
        return getUpgradeable();
    }

    @Override
    protected int getHeight() {
        return 231;
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
    public IGasInventory getGasConfigInventory() {
        return getUpgradeable().getConfig();
    }

    @Override
    public void addListener(@Nonnull IContainerListener listener) {
        super.addListener(listener);
        this.tankSync.sendFull(Collections.singleton(listener));
    }

    @Override
    public void receiveGasSlots(final Map<Integer, IAEGasStack> gases) {
        super.receiveGasSlots(gases);
        this.tankSync.readPacket(gases);
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
