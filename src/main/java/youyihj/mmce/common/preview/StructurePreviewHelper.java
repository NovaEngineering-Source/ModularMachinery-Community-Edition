package youyihj.mmce.common.preview;

import com.google.common.base.Objects;
import hellfirepvp.modularmachinery.client.ClientProxy;
import hellfirepvp.modularmachinery.client.util.BlockArrayPreviewRenderHelper;
import hellfirepvp.modularmachinery.client.util.DynamicMachineRenderContext;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

public class StructurePreviewHelper {
    private static BlockPos renderPos = null;

    public static void renderMachinePreview(DynamicMachine machine, BlockPos pos) {
        BlockArrayPreviewRenderHelper renderHelper = ClientProxy.renderHelper;
        if (!Objects.equal(renderPos, pos)) {
            renderHelper.unloadWorld();
        }
        renderPos = pos;
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player != null) {
            player.sendMessage(
                new TextComponentTranslation("message.machine_projector.project", machine.getLocalizedName())
            );
        }
        DynamicMachineRenderContext context = DynamicMachineRenderContext.createContext(machine);
        context.snapSamples();
        renderHelper.startPreview(context);
        renderHelper.placePreview();
    }

    public static void reset() {
        renderPos = null;
    }
}
