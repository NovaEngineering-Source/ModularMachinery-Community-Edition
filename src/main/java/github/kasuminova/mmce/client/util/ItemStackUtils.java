package github.kasuminova.mmce.client.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemStackUtils {

    public static ItemStack readNBTOversize(final NBTTagCompound tag) {
        ItemStack stack = new ItemStack(tag);
        stack.setCount(tag.getInteger("Count"));
        return stack;
    }

    public static NBTTagCompound writeNBTOversize(final ItemStack stack) {
        NBTTagCompound tag = stack.serializeNBT();
        if (stack.getCount() > Byte.MAX_VALUE) {
            tag.setInteger("Count", stack.getCount());
        }
        return tag;
    }

    public static NBTTagCompound writeNBTOversize(final ItemStack stack, NBTTagCompound tag) {
        stack.writeToNBT(tag);
        if (stack.getCount() > Byte.MAX_VALUE) {
            tag.setInteger("Count", stack.getCount());
        }
        return tag;
    }

}
