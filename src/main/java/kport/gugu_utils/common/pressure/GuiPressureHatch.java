package kport.gugu_utils.common.pressure;

import kport.gugu_utils.common.tile.TilePressureHatch;
import static kport.gugu_utils.tools.ResourceUtils.j;
import me.desht.pneumaticcraft.client.gui.GuiPneumaticContainerBase;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;

import java.awt.*;

public class GuiPressureHatch extends GuiPneumaticContainerBase<TilePressureHatch> {
    public GuiPressureHatch(InventoryPlayer player, TilePressureHatch te) {
        super(new ContainerPressureHatch(player, te), te, me.desht.pneumaticcraft.lib.Textures.GUI_4UPGRADE_SLOTS);
    }


    @Override
    protected void drawGuiContainerForegroundLayer(int x, int y) {
        super.drawGuiContainerForegroundLayer(x, y);
        String containerName = I18n.format(j(te.getName(), "name"));
        fontRenderer.drawString(containerName, xSize / 2 - fontRenderer.getStringWidth(containerName) / 2, 6, 4210752);
        fontRenderer.drawString("Upgr.", 53, 19, 4210752);
    }

    @Override
    protected Point getInvNameOffset() {
        return null;
    }

}
