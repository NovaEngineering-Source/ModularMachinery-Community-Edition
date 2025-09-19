package github.kasuminova.mmce.common.tile;

import net.minecraft.nbt.NBTTagCompound;

public interface SettingsTransfer {
    NBTTagCompound downloadSettings();

    void uploadSettings(NBTTagCompound settings);
}
