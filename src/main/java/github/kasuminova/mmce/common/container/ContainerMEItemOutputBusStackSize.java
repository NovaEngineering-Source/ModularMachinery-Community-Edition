package github.kasuminova.mmce.common.container;

import appeng.api.config.SecurityPermissions;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import github.kasuminova.mmce.common.tile.MEItemOutputBus;
import net.minecraft.entity.player.InventoryPlayer;

/**
 * Container for the ME Item Output Bus Stack Size configuration GUI.
 * Handles synchronization of the stack size value between client and server.
 *
 * NOTE: This container does NOT apply changes to the tile entity in real-time.
 * Changes are only applied when the GUI is closed via onContainerClosed().
 */
public class ContainerMEItemOutputBusStackSize extends AEBaseContainer {

    private final MEItemOutputBus outputBus;

    /**
     * The stack size value displayed and edited in the GUI.
     * Synced from server to client automatically via @GuiSync.
     * This is a TEMPORARY value that gets applied when the GUI closes.
     */
    @GuiSync(0)
    public int stackSize = Integer.MAX_VALUE;

    public ContainerMEItemOutputBusStackSize(final InventoryPlayer inventoryPlayer, final MEItemOutputBus outputBus) {
        super(inventoryPlayer, outputBus);
        this.outputBus = outputBus;

        // Initialize with the current value from the tile entity
        // This must be done in the constructor, not in detectAndSendChanges,
        // to ensure the value is set before the GUI tries to read it
        this.stackSize = outputBus.getConfiguredStackSize();
    }

    /**
     * Returns the current stack size value being displayed/edited in the GUI.
     * This is NOT necessarily the actual stack size in the tile entity.
     */
    public int getStackSize() {
        return this.stackSize;
    }

    /**
     * Returns the Output Bus tile entity.
     */
    public MEItemOutputBus getOwner() {
        return this.outputBus;
    }

    /**
     * Called periodically on the server to detect changes and sync to client.
     * We don't read from the tile entity here to avoid overwriting user edits.
     */
    @Override
    public void detectAndSendChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        // Let AE2's @GuiSync system handle synchronization
        // Don't read from tile entity - we want to preserve user edits
        super.detectAndSendChanges();
    }

    /**
     * Called when the container is closed (player closes the GUI).
     * This is where we apply the configured stack size to the tile entity.
     */
    @Override
    public void onContainerClosed(net.minecraft.entity.player.EntityPlayer playerIn) {
        super.onContainerClosed(playerIn);

        // Only apply on server side
        if (!playerIn.world.isRemote) {
            // Apply the configured stack size to the tile entity
            this.outputBus.setConfiguredStackSize(this.stackSize);
        }
    }
}