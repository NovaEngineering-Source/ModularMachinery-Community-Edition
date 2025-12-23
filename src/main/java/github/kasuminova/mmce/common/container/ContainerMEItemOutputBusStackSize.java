package github.kasuminova.mmce.common.container;

import appeng.api.config.SecurityPermissions;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import github.kasuminova.mmce.common.tile.MEItemOutputBus;
import net.minecraft.entity.player.InventoryPlayer;
import org.jetbrains.annotations.NotNull;

public class ContainerMEItemOutputBusStackSize extends AEBaseContainer {

    private final MEItemOutputBus outputBus;

    @GuiSync(0)
    public int stackSize;

    public ContainerMEItemOutputBusStackSize(final InventoryPlayer inventoryPlayer, final MEItemOutputBus outputBus) {
        super(inventoryPlayer, outputBus);
        this.outputBus = outputBus;

        this.stackSize = outputBus.getConfiguredStackSize();
    }

    public int getStackSize() {
        return this.stackSize;
    }

    public MEItemOutputBus getOwner() {
        return this.outputBus;
    }

    @Override
    public void detectAndSendChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        super.detectAndSendChanges();
    }

    @Override
    public void onContainerClosed(@NotNull net.minecraft.entity.player.EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);

        if (!playerIn.world.isRemote) {
            this.outputBus.setConfiguredStackSize(this.stackSize);
        }
    }
}