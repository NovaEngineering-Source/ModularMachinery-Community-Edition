package github.kasuminova.mmce.common.tile;

import github.kasuminova.mmce.common.util.InfItemFluidHandler;
import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.lib.ComponentTypesMM;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.tiles.base.MachineComponentTile;
import hellfirepvp.modularmachinery.common.tiles.base.SelectiveUpdateTileEntity;
import hellfirepvp.modularmachinery.common.tiles.base.TileColorableMachineComponent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public class MEPatternMirrorImage extends TileColorableMachineComponent implements SelectiveUpdateTileEntity, MachineComponentTile {

    public BlockPos            providerPos;
    public InfItemFluidHandler handler;

    public MEPatternMirrorImage() {
        handler = new InfItemFluidHandler();
    }

    @Nullable
    @Override
    public MachineComponent<InfItemFluidHandler> provideComponent() {
        if (providerPos != null) {
            TileEntity tileEntity = this.world.getTileEntity(providerPos);
            if (tileEntity instanceof MEPatternProvider) {
                return new MachineComponent<>(IOType.INPUT) {
                    public ComponentType getComponentType() {
                        return ComponentTypesMM.COMPONENT_ITEM_FLUID_GAS;
                    }

                    public InfItemFluidHandler getContainerProvider() {
                        return ((MEPatternProvider) tileEntity).getInfHandler();
                    }
                };
            }
        }
        return new MachineComponent<>(IOType.INPUT) {
            public ComponentType getComponentType() {
                return ComponentTypesMM.COMPONENT_ITEM_FLUID_GAS;
            }

            public InfItemFluidHandler getContainerProvider() {
                return handler;
            }
        };
    }

    @Override
    public void readCustomNBT(NBTTagCompound compound) {
        super.readCustomNBT(compound);
        if (compound.hasKey("providerPos")) {
            this.providerPos = BlockPos.fromLong(compound.getLong("providerPos"));
        }
    }

    @Override
    public void writeCustomNBT(NBTTagCompound compound) {
        super.writeCustomNBT(compound);
        if (providerPos != null) {
            compound.setLong("providerPos", providerPos.toLong());
        }
    }

}
