package kport.gugu_utils.handler;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.selection.PlayerStructureSelectionHelper;
import kport.gugu_utils.common.tools.ItemRangedConstructTool;
import kport.gugu_utils.tools.RenderTools;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientEventHandler {
    public static int elapsedTicks = 0;

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void tickEnd(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.type != TickEvent.Type.CLIENT || event.side != Side.CLIENT) {
            return;
        }

        elapsedTicks++;

    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onRenderLast(RenderWorldLastEvent event) {
        if (Minecraft.getMinecraft().player == null) return;

        ItemStack held = Minecraft.getMinecraft().player.getHeldItemMainhand();
        if (held.isEmpty()) {
            held = Minecraft.getMinecraft().player.getHeldItemOffhand();
        }
        if (!held.isEmpty() && held.getItem() instanceof ItemRangedConstructTool) {
            BlockPos firstBind = ItemRangedConstructTool.getNbtPos(held);
            if (firstBind.getY() >= 0) {
                RenderTools.renderBlockOutline(firstBind, event.getPartialTicks());
            } else {

                PlayerStructureSelectionHelper.StructureSelection sel = PlayerStructureSelectionHelper.clientSelection;
                if (sel != null) {
                    List<BlockPos> toRender = sel.getSelectedPositions().stream()
                            .filter((pos) -> pos.distanceSq(Minecraft.getMinecraft().player.getPosition()) <= 1024)
                            .collect(Collectors.toList());
//                RenderingUtils.drawWhiteOutlineCubes(toRender, event.getPartialTicks());
                    if (ModularMachinery.proxy.getClientPlayer().isSneaking()) {
                        RenderTools.renderBlocksOutline(toRender, event.getPartialTicks(), true);
                    } else {
                        RenderTools.renderBlocksOutline(toRender, event.getPartialTicks());
                    }

                }
            }

        }
    }


}
