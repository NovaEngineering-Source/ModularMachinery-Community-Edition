package github.kasuminova.mmce.common.handler;

import github.kasuminova.mmce.client.util.BufferBuilderPool;
import hellfirepvp.modularmachinery.common.item.ItemBlockController;
import hellfirepvp.modularmachinery.common.lib.ItemsMM;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("MethodMayBeStatic")
public class ClientHandler {

    public static String convertBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " bytes";
        } else if (bytes < 1024 * 1024) {
            double kb = (double) bytes / 1024;
            return String.format("%.2f KB", kb);
        } else if (bytes < 1024 * 1024 * 1024) {
            double mb = (double) bytes / (1024 * 1024);
            return String.format("%.2f MB", mb);
        } else {
            double gb = (double) bytes / (1024 * 1024 * 1024);
            return String.format("%.2f GB", gb);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onMEItemBusAndPatternProviderItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Item item = stack.getItem();
        if (item != ItemsMM.meItemInputBus && item != ItemsMM.meItemOutputBus && item != ItemsMM.mePatternProvider) {
            return;
        }

        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null) {
            if (tag.hasKey("inventory")) {
                event.getToolTip().add(I18n.format("gui.meitembus.nbt_stored"));
            } else if (tag.hasKey("patternProvider")) {
                event.getToolTip().add(I18n.format("gui.mepatternprovider.nbt_stored"));
            }
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onBlockControllerItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Item item = stack.getItem();
        if (!(item instanceof ItemBlockController)) {
            return;
        }

        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null && tag.hasKey("owner")) {
            String ownerUUIDStr = tag.getString("owner");
            try {
                UUID ownerUUID = UUID.fromString(ownerUUIDStr);

                EntityPlayerSP player = Minecraft.getMinecraft().player;
                UUID playerUUID = player.getGameProfile().getId();
                if (playerUUID.equals(ownerUUID)) {
                    event.getToolTip().add(I18n.format("tooltip.item.controller.owner.self"));
                } else {
                    event.getToolTip().add(I18n.format("tooltip.item.controller.owner.not_self"));
                }
            } catch (Exception e) {
                event.getToolTip().add(TextFormatting.RED + "NBT read error.");
            }
        }
    }

    @SubscribeEvent
    public void onDebugText(RenderGameOverlayEvent.Text event) {
        if (!Minecraft.getMinecraft().gameSettings.showDebugInfo) {
            return;
        }

        List<String> left = event.getLeft();
        left.add("");
        left.add(String.format("%s[ModularMachinery - CE] %sBuffer Pool Size: %s%d InPool %s/ %s%d Total",
            TextFormatting.BLUE, TextFormatting.RESET,
            TextFormatting.GREEN, BufferBuilderPool.getPoolSize(),
            TextFormatting.RESET,
            TextFormatting.YELLOW, BufferBuilderPool.getCreatedBuffers())
        );
        left.add(String.format("%s[ModularMachinery - CE] %sBuffer Mem Usage: %s%s",
            TextFormatting.BLUE, TextFormatting.RESET,
            TextFormatting.YELLOW, convertBytes(BufferBuilderPool.getBufferMemUsage()))
        );
    }

}
