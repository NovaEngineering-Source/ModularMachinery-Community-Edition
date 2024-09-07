package github.kasuminova.mmce.common.container;

import github.kasuminova.mmce.common.tile.MEGasOutputBus;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerMEGasOutputBus extends ContainerMEGasBus {
    private final MEGasOutputBus owner;

    public ContainerMEGasOutputBus(final MEGasOutputBus owner, final EntityPlayer player) {
        super(player.inventory, owner);
        this.owner = owner;
    }

    @Override
    protected void bindPlayerInventory(final InventoryPlayer inventoryPlayer, final int offsetX, final int offsetY) {
        super.bindPlayerInventory(inventoryPlayer, 0, 195 - /* height of player inventory */ 83);
    }

    public MEGasOutputBus getOwner() {
        return owner;
    }

    @Override
    protected int getHeight() {
        return 192;
    }

}
