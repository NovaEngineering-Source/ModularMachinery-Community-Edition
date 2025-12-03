package hellfirepvp.modularmachinery.common.container;

import hellfirepvp.modularmachinery.common.tiles.base.MachineGroupInput;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public class ContainerGroupInputConfig extends ContainerBase<TileEntity> {

    private final MachineGroupInput machine;

    public ContainerGroupInputConfig(TileEntity owner, EntityPlayer opening) {
        super(owner, opening);
        machine = (MachineGroupInput) owner;
    }

    public MachineGroupInput getMachine() {
        return machine;
    }
}
