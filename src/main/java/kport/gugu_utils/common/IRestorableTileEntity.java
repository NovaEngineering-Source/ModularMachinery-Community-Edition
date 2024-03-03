package kport.gugu_utils.common;

import net.minecraft.nbt.NBTTagCompound;

public interface IRestorableTileEntity {
    void readRestorableFromNBT(NBTTagCompound tagCompound);
    NBTTagCompound writeRestorableToNBT(NBTTagCompound compound);
}
