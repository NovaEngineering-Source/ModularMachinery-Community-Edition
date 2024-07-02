package github.kasuminova.mmce.common.network;

import github.kasuminova.mmce.client.gui.GuiMEPatternProvider;
import github.kasuminova.mmce.common.tile.MEPatternProvider;
import github.kasuminova.mmce.common.util.InfItemFluidHandler;
import hellfirepvp.modularmachinery.common.base.Mods;
import io.netty.buffer.ByteBuf;
import mekanism.api.gas.GasStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public class PktMEPatternProviderHandlerItems implements IMessage, IMessageHandler<PktMEPatternProviderHandlerItems, IMessage> {

    private final List<ItemStack> itemStackList = new ArrayList<>();
    private final List<FluidStack> fluidStackList = new ArrayList<>();
    private final List<?> gasStackList = new ArrayList<>();

    public PktMEPatternProviderHandlerItems() {
    }

    public PktMEPatternProviderHandlerItems(final MEPatternProvider patternProvider) {
        InfItemFluidHandler infHandler = patternProvider.getInfHandler();
        infHandler.getItemStackList().stream()
                .filter(stack -> !stack.isEmpty())
                .forEach(itemStackList::add);
        infHandler.getFluidStackList().stream()
                .filter(Objects::nonNull)
                .forEach(fluidStackList::add);
        if (Mods.MEKANISM.isPresent() && Mods.MEKENG.isPresent()) {
            addGasToList(infHandler);
        }
    }

    @SuppressWarnings("unchecked")
    @Optional.Method(modid = "mekanism")
    private void addGasToList(final InfItemFluidHandler infHandler) {
        List<GasStack> gasStackList = (List<GasStack>) this.gasStackList;
        infHandler.getFluidStackList().stream()
                .filter(Objects::nonNull)
                .map(GasStack.class::cast)
                .forEach(gasStackList::add);
    }

    @Override
    public void fromBytes(final ByteBuf buf) {
        int itemStackListSize = buf.readInt();
        IntStream.range(0, itemStackListSize)
                .mapToObj(i -> ByteBufUtils.readItemStack(buf))
                .filter(read -> !read.isEmpty())
                .forEach(itemStackList::add);

        int fluidStackListSize = buf.readInt();
        IntStream.range(0, fluidStackListSize)
                .mapToObj(i -> FluidStack.loadFluidStackFromNBT(ByteBufUtils.readTag(buf)))
                .filter(Objects::nonNull)
                .forEach(fluidStackList::add);

        if (Mods.MEKANISM.isPresent() && Mods.MEKENG.isPresent()) {
            fromBytesMekGas(buf);
        }
    }

    @SuppressWarnings("unchecked")
    @Optional.Method(modid = "mekanism")
    private void fromBytesMekGas(final ByteBuf buf) {
        List<GasStack> gasStackList = (List<GasStack>) this.gasStackList;

        int gasStackListSize = buf.readInt();
        IntStream.range(0, gasStackListSize)
                .mapToObj(i -> GasStack.readFromNBT(ByteBufUtils.readTag(buf)))
                .filter(Objects::nonNull)
                .forEach(gasStackList::add);
    }

    @Override
    public void toBytes(final ByteBuf buf) {
        buf.writeInt(itemStackList.size());
        itemStackList.forEach(stack -> ByteBufUtils.writeItemStack(buf, stack));

        buf.writeInt(fluidStackList.size());
        fluidStackList.forEach(stack -> ByteBufUtils.writeTag(buf, stack.writeToNBT(new NBTTagCompound())));
    }

    @SuppressWarnings("unchecked")
    @Optional.Method(modid = "mekanism")
    private void toBytesMekGas(final ByteBuf buf) {
        List<GasStack> gasStackList = (List<GasStack>) this.gasStackList;

        buf.writeInt(gasStackList.size());
        gasStackList.forEach(stack -> ByteBufUtils.writeTag(buf, stack.write(new NBTTagCompound())));
    }

    @Override
    public IMessage onMessage(final PktMEPatternProviderHandlerItems message, final MessageContext ctx) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            processPacket(message);
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    protected static void processPacket(final PktMEPatternProviderHandlerItems message) {
        List<ItemStack> itemStackList = message.itemStackList;
        List<FluidStack> fluidStackList = message.fluidStackList;
        List<?> gasStackList = message.gasStackList;
        GuiScreen cur = Minecraft.getMinecraft().currentScreen;
        if (!(cur instanceof GuiMEPatternProvider patternProvider)) {
            return;
        }
        Minecraft.getMinecraft().addScheduledTask(() -> patternProvider.setStackList(itemStackList, fluidStackList, gasStackList));
    }

}
