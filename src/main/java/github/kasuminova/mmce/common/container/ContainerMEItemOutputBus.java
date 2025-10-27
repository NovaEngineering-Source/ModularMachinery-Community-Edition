package github.kasuminova.mmce.common.container;

import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotDisabled;
import github.kasuminova.mmce.common.tile.base.MEItemBus;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.items.IItemHandlerModifiable;

public class ContainerMEItemOutputBus extends AEBaseContainer {
    private final MEItemBus owner;

    public ContainerMEItemOutputBus(final MEItemBus owner, final EntityPlayer player) {
        super(player.inventory, owner);
        this.owner = owner;

        this.bindPlayerInventory(getInventoryPlayer(), 0, 195 - /* height of player inventory */ 82);

        IItemHandlerModifiable guiAccess = this.owner.getInternalInventory().asGUIAccess();
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 9; x++) {
                this.addSlotToContainer(new SlotDisabled(guiAccess, y * 9 + x, 8 + 18 * x, 24 + 18 * y));
            }
        }
    }

    public MEItemBus getOwner() {
        return owner;
    }
}
