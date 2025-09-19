/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.network;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.client.gui.GuiContainerFluidHatch;
import hellfirepvp.modularmachinery.common.container.ContainerFluidHatch;
import hellfirepvp.modularmachinery.common.tiles.base.TileFluidTank;
import hellfirepvp.modularmachinery.common.util.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: PktInteractFluidTankGui
 * Created by HellFirePvP
 * Date: 02.03.2019 / 14:20
 */
public class PktInteractFluidTankGui implements IMessage, IMessageHandler<PktInteractFluidTankGui, IMessage> {

    private ItemStack held;

    public PktInteractFluidTankGui() {
        this(ItemStack.EMPTY);
    }

    public PktInteractFluidTankGui(ItemStack held) {
        this.held = held;
    }

    @SideOnly(Side.CLIENT)
    private static void updateClientHand(PktInteractFluidTankGui pkt) {
        GuiScreen gui = Minecraft.getMinecraft().currentScreen;

        if (gui instanceof GuiContainerFluidHatch) {
            EntityPlayer player = Minecraft.getMinecraft().player;

            if (!player.inventory.getItemStack().isEmpty()) { //Has to have had an item before... obviously.. right?
                player.inventory.setItemStack(pkt.held);
            }
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.held = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeItemStack(buf, this.held);
    }

    @Override
    public IMessage onMessage(PktInteractFluidTankGui pkt, MessageContext ctx) {
        if (ctx.side == Side.SERVER) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
                EntityPlayerMP player = ctx.getServerHandler().player;
                if (!(player.openContainer instanceof ContainerFluidHatch fluidHatch)) {
                    return;
                }

                TileFluidTank hatch = fluidHatch.getOwner();
                ItemStack holding = player.inventory.getItemStack();
                if (holding.isEmpty()) {
                    return;
                }

                FluidActionResult fas = FluidUtil.tryEmptyContainer(holding, hatch.getTank(), Fluid.BUCKET_VOLUME, player, true);
                if (!fas.isSuccess()) {
                    return;
                }

                ItemStack result = fas.getResult();
                player.inventory.setItemStack(result);
                ModularMachinery.NET_CHANNEL.sendTo(new PktInteractFluidTankGui(result), player);
            });
            return null;
        }

        updateClientHand(pkt);
        return null;
    }
}
