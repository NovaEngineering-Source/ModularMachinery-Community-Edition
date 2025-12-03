package kport.modularmagic.common.tile;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.data.Config;
import hellfirepvp.modularmachinery.common.machine.IOType;
import hellfirepvp.modularmachinery.common.tiles.base.ColorableMachineTile;
import hellfirepvp.modularmachinery.common.tiles.base.MachineComponentTile;
import hellfirepvp.modularmachinery.common.tiles.base.MachineGroupInput;
import kport.modularmagic.common.crafting.helper.AspectProviderCopy;
import kport.modularmagic.common.tile.machinecomponent.MachineComponentAspectProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Optional;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumcraft.common.tiles.essentia.TileJarFillable;

public abstract class TileAspectProvider extends TileJarFillable implements MachineComponentTile, ColorableMachineTile {

    private int color = Config.machineColor;

    @Override
    public int getMachineColor() {
        return this.color;
    }

    @Override
    public void setMachineColor(int newColor) {
        if (color == newColor) {
            return;
        }
        this.color = newColor;
        ModularMachinery.EXECUTE_MANAGER.addSyncTask(() -> {
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
            this.markDirty();
        });
    }

    @Override
    public boolean canInputFrom(EnumFacing face) {
        return true;
    }

    @Override
    public boolean canOutputTo(EnumFacing face) {
        return true;
    }

    @Override
    public boolean isConnectable(EnumFacing face) {
        return true;
    }

    @Override
    @Optional.Method(modid = "thaumcraft")
    public void update() {
        if (!this.world.isRemote && this.amount < TileJarFillable.CAPACITY) {
            synchronized (this) {
                for (EnumFacing face : EnumFacing.VALUES) {
                    this.fillJar(face);
                }
            }
        }
    }

    @Override
    @Optional.Method(modid = "thaumcraft")
    public synchronized int takeEssentia(final Aspect aspect, final int amount, final EnumFacing face) {
        return super.takeEssentia(aspect, amount, face);
    }

    @Override
    @Optional.Method(modid = "thaumcraft")
    public synchronized int addEssentia(final Aspect aspect, final int amount, final EnumFacing face) {
        return super.addEssentia(aspect, amount, face);
    }

    @Override
    @Optional.Method(modid = "thaumcraft")
    public synchronized int addToContainer(final Aspect tt, final int am) {
        return super.addToContainer(tt, am);
    }

    @Override
    @Optional.Method(modid = "thaumcraft")
    public synchronized boolean takeFromContainer(final Aspect tt, final int am) {
        return super.takeFromContainer(tt, am);
    }

    @Override
    @Optional.Method(modid = "thaumcraft")
    public synchronized boolean takeFromContainer(final AspectList ot) {
        return super.takeFromContainer(ot);
    }

    @Optional.Method(modid = "thaumcraft")
    private void fillJar(EnumFacing face) {
        TileEntity te = ThaumcraftApiHelper.getConnectableTile(this.world, this.pos, face);
        if (te == null) {
            return;
        }

        EnumFacing opposite = face.getOpposite();
        IEssentiaTransport ic = (IEssentiaTransport) te;
        if (!ic.canOutputTo(opposite)) {
            return;
        }

        Aspect ta;
        if (this.aspect != null && this.amount > 0) {
            ta = this.aspect;
        } else if (ic.getEssentiaAmount(opposite) > 0 && ic.getSuctionAmount(opposite) < this.getSuctionAmount(face) && this.getSuctionAmount(face) >= ic.getMinimumSuction()) {
            ta = ic.getEssentiaType(opposite);
        } else {
            return;
        }

        if (ic.getSuctionAmount(opposite) < this.getSuctionAmount(face)) {
            this.addToContainer(ta, ic.takeEssentia(ta, 1, opposite));
        }
    }

    @NotNull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("casingColor", this.color);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.color = nbt.hasKey("casingColor") ? nbt.getInteger("casingColor") : Config.machineColor;
    }

    @Override
    public NBTTagCompound writeSyncNBT(NBTTagCompound nbt) {
        super.writeSyncNBT(nbt);
        nbt.setInteger("casingColor", this.color);
        return nbt;
    }

    @Override
    public void readSyncNBT(NBTTagCompound nbt) {
        super.readSyncNBT(nbt);
        this.color = nbt.hasKey("casingColor") ? nbt.getInteger("casingColor") : Config.machineColor;
    }

    public static class Input extends TileAspectProvider implements MachineGroupInput {

        private int groupId;
        private boolean isGroupInput;

        @Override
        public MachineComponentAspectProvider provideComponent() {
            return new MachineComponentAspectProvider(new AspectProviderCopy(this), IOType.INPUT);
        }

        @Override
        public void setGroupId(int groupId) {
            this.groupId = groupId;
        }

        @Override
        public boolean canGroupInput() {
            return true;
        }

        @Override
        public boolean isGroupInput() {
            return isGroupInput;
        }

        @Override
        public void setGroupInput(boolean b) {
            isGroupInput = b;
        }

        @Override
        public int getGroupId() {
            if (isGroupInput()) return groupId;
            return -1;
        }

        @NotNull
        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            super.writeToNBT(nbt);
            nbt.setInteger("groupId", this.groupId);
            nbt.setBoolean("isGroupInput", this.isGroupInput);
            return nbt;
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            super.readFromNBT(nbt);
            groupId = nbt.getInteger("groupId");
            isGroupInput = nbt.getBoolean("isGroupInput");
        }

        @Override
        public NBTTagCompound writeSyncNBT(NBTTagCompound nbt) {
            super.writeSyncNBT(nbt);
            nbt.setInteger("groupId", this.groupId);
            nbt.setBoolean("isGroupInput", this.isGroupInput);
            return nbt;
        }

        @Override
        public void readSyncNBT(NBTTagCompound nbt) {
            super.readSyncNBT(nbt);
            groupId = nbt.getInteger("groupId");
            isGroupInput = nbt.getBoolean("isGroupInput");
        }
    }

    public static class Output extends TileAspectProvider {
        @Override
        public MachineComponentAspectProvider provideComponent() {
            return new MachineComponentAspectProvider(new AspectProviderCopy(this), IOType.OUTPUT);
        }
    }
}
