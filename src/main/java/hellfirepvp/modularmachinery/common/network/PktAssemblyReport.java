package hellfirepvp.modularmachinery.common.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class PktAssemblyReport implements IMessage, IMessageHandler<PktAssemblyReport, IMessage> {
    private List<List<ItemStack>>  itemStackIngList  = new ArrayList<>();
    private List<List<FluidStack>> fluidStackIngList = new ArrayList<>();

    public PktAssemblyReport() {
    }

    public PktAssemblyReport(final List<List<ItemStack>> itemStackIngList, final List<List<FluidStack>> fluidStackIngList) {
        this.itemStackIngList = itemStackIngList;
        this.fluidStackIngList = fluidStackIngList;
    }

    private static void sendItemStackListMessage(final List<List<ItemStack>> itemStackIngList, final EntityPlayer player) {
        for (final List<ItemStack> stackList : itemStackIngList) {
            if (stackList.isEmpty()) {
                continue;
            }

            if (stackList.size() == 1) {
                ItemStack stack = stackList.get(0);
                player.sendMessage(new TextComponentString(itemStackToString(stack)));
                continue;
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < stackList.size(); i++) {
                final ItemStack stack = stackList.get(i);

                sb.append(itemStackToString(stack));
                if (i + 1 < stackList.size()) {
                    sb.append(" ").append(I18n.format("message.assembly.tip.or")).append(" ");
                }
            }
            player.sendMessage(new TextComponentString(sb.toString()));
        }
    }

    private static void sendFluidStackListMessage(final List<List<FluidStack>> fluidStackIngList, final EntityPlayer player) {
        for (final List<FluidStack> stackList : fluidStackIngList) {
            if (stackList.isEmpty()) {
                continue;
            }

            if (stackList.size() == 1) {
                FluidStack stack = stackList.get(0);
                player.sendMessage(new TextComponentString(fluidStackToString(stack)));
                continue;
            }

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < stackList.size(); i++) {
                final FluidStack stack = stackList.get(i);

                sb.append(fluidStackToString(stack));
                if (i + 1 < stackList.size()) {
                    sb.append(" ").append(I18n.format("message.assembly.tip.or")).append(" ");
                }
            }
            player.sendMessage(new TextComponentString(sb.toString()));
        }
    }

    private static String itemStackToString(ItemStack stack) {
        return stack.getCount() + "x " + stack.getDisplayName();
    }

    private static String fluidStackToString(FluidStack stack) {
        return stack.amount + "mb " + stack.getLocalizedName();
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        NBTTagCompound tag = ByteBufUtils.readTag(buf);
        if (tag == null) {
            return;
        }

        NBTTagList itemStackTagList = tag.getTagList("itemStackList", Constants.NBT.TAG_LIST);
        for (int i = 0; i < itemStackTagList.tagCount(); i++) {
            NBTTagList stackTagList = (NBTTagList) itemStackTagList.get(i);

            List<ItemStack> itemStackList = new ArrayList<>();
            for (int i1 = 0; i1 < stackTagList.tagCount(); i1++) {
                NBTTagCompound stackTag = stackTagList.getCompoundTagAt(i1);

                if (!stackTag.isEmpty()) {
                    ItemStack stack = new ItemStack(stackTag);
                    if (!stack.isEmpty()) {
                        itemStackList.add(stack);
                    }
                }
            }

            if (!itemStackList.isEmpty()) {
                itemStackIngList.add(itemStackList);
            }
        }

        NBTTagList fluidStackTagList = tag.getTagList("fluidStackList", Constants.NBT.TAG_LIST);
        for (int i = 0; i < fluidStackTagList.tagCount(); i++) {
            NBTTagList stackTagList = (NBTTagList) fluidStackTagList.get(i);

            List<FluidStack> fluidStackList = new ArrayList<>();
            for (int i1 = 0; i1 < stackTagList.tagCount(); i1++) {
                NBTTagCompound stackTag = stackTagList.getCompoundTagAt(i1);

                if (!stackTag.isEmpty()) {
                    FluidStack stack = FluidStack.loadFluidStackFromNBT(stackTag);
                    if (stack != null) {
                        fluidStackList.add(stack);
                    }
                }
            }

            if (!fluidStackList.isEmpty()) {
                fluidStackIngList.add(fluidStackList);
            }
        }
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        NBTTagCompound tag = new NBTTagCompound();

        // ItemStack
        NBTTagList itemStackIngTagList = new NBTTagList();
        for (final List<ItemStack> itemStackList : itemStackIngList) {
            NBTTagList stackTagList = new NBTTagList();

            for (final ItemStack stack : itemStackList) {
                NBTTagCompound stackTag = new NBTTagCompound();
                stack.writeToNBT(stackTag);
                stackTagList.appendTag(stackTag);
            }
            itemStackIngTagList.appendTag(stackTagList);
        }
        tag.setTag("itemStackList", itemStackIngTagList);

        // FluidStack
        NBTTagList fluidStackIngTagList = new NBTTagList();
        for (final List<FluidStack> fluidStackList : fluidStackIngList) {
            NBTTagList stackTagList = new NBTTagList();

            for (final FluidStack stack : fluidStackList) {
                NBTTagCompound stackTag = new NBTTagCompound();
                stack.writeToNBT(stackTag);
                stackTagList.appendTag(stackTag);
            }

            fluidStackIngTagList.appendTag(stackTagList);
        }

        tag.setTag("fluidStackList", fluidStackIngTagList);
        ByteBufUtils.writeTag(buf, tag);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IMessage onMessage(final PktAssemblyReport message, final MessageContext ctx) {
        List<List<ItemStack>> itemStackIngList = message.itemStackIngList;
        List<List<FluidStack>> fluidStackIngList = message.fluidStackIngList;

        EntityPlayerSP player = Minecraft.getMinecraft().player;
        player.sendMessage(new TextComponentTranslation("message.assembly.tip.required"));

        sendItemStackListMessage(itemStackIngList, player);
        sendFluidStackListMessage(fluidStackIngList, player);

        return null;
    }
}
