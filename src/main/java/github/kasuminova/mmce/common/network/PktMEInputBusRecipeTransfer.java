package github.kasuminova.mmce.common.network;

import appeng.container.slot.SlotFake;
import appeng.helpers.ItemStackHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

public class PktMEInputBusRecipeTransfer implements IMessage, IMessageHandler<PktMEInputBusRecipeTransfer, IMessage> {

    private ArrayList<ItemStack> inputs;

    public PktMEInputBusRecipeTransfer() {
    }

    public PktMEInputBusRecipeTransfer(ArrayList<ItemStack> inputs) {
        this.inputs = inputs;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        inputs = new ArrayList<>();
        NBTTagCompound tagCompound = ByteBufUtils.readTag(buf);
        NBTTagList tagList = tagCompound.getTagList("itemInputs", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound tag = tagList.getCompoundTagAt(i);
            if (!tagList.isEmpty()) {
                ItemStack stack = new ItemStack(tag);
                if (!stack.isEmpty()) {
                    inputs.add(stack);
                }
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        final NBTTagList recipeInputs = new NBTTagList();
        for (ItemStack input : inputs) {
            recipeInputs.appendTag(ItemStackHelper.stackToNBT(input));
        }
        NBTTagCompound recipeTag = new NBTTagCompound();
        recipeTag.setTag("itemInputs", recipeInputs);
        ByteBufUtils.writeTag(buf, recipeTag);
    }

    @Override
    public IMessage onMessage(PktMEInputBusRecipeTransfer message, MessageContext ctx) {
        final EntityPlayerMP player = ctx.getServerHandler().player;
        final Container container = player.openContainer;
        ArrayList<ItemStack> inputs = message.inputs;
        List<Slot> inventorySlots = container.inventorySlots;
        ext:
        for (ItemStack input : inputs) {
            for (Slot slot : inventorySlots) {
                if (slot instanceof SlotFake slotFake) {
                    if (!slotFake.getHasStack()) {
                        slotFake.putStack(input.copy());
                        continue ext;
                    }
                }
            }
        }

        return null;
    }
}
