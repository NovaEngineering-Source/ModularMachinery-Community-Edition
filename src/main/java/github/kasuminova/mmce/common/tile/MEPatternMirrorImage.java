package github.kasuminova.mmce.common.tile;

import github.kasuminova.mmce.common.tile.base.MachineCombinationComponent;
import github.kasuminova.mmce.common.util.InfItemFluidHandler;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.tiles.base.MachineComponentTile;
import hellfirepvp.modularmachinery.common.tiles.base.SelectiveUpdateTileEntity;
import hellfirepvp.modularmachinery.common.tiles.base.TileColorableMachineComponent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

public class MEPatternMirrorImage extends TileColorableMachineComponent implements SelectiveUpdateTileEntity, MachineComponentTile, MachineCombinationComponent {

    public BlockPos providerPos;
    public InfItemFluidHandler handler;

    public MEPatternMirrorImage() {
        handler = new InfItemFluidHandler();
    }

    @Nullable
    @Override
    public MachineComponent<InfItemFluidHandler> provideComponent() {
        if (!this.world.isRemote && providerPos != null && ((WorldServer) this.world).getChunkProvider().chunkExists(providerPos.getX() >> 4, providerPos.getZ() >> 4)) {
            TileEntity tileEntity = this.world.getTileEntity(providerPos);
            if (tileEntity instanceof MEPatternProvider mep) {
                return mep.provideComponent();
            }
        }
        return null;
    }

    @NotNull
    @Override
    public Collection<MachineComponent<?>> provideComponents() {
        if (!this.world.isRemote && providerPos != null && ((WorldServer) this.world).getChunkProvider().chunkExists(providerPos.getX() >> 4, providerPos.getZ() >> 4)) {
            TileEntity tileEntity = this.world.getTileEntity(providerPos);
            if (tileEntity instanceof MEPatternProvider mep) {
                return mep.provideComponents();
            }
        }
        return Collections.emptyList();
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
