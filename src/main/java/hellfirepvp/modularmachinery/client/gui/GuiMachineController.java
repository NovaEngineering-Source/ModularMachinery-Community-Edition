/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.client.gui;

import github.kasuminova.mmce.common.event.client.ControllerGUIRenderEvent;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.container.ContainerController;
import hellfirepvp.modularmachinery.common.crafting.ActiveMachineRecipe;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.tiles.TileMachineController;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import hellfirepvp.modularmachinery.common.util.MiscUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: GuiMachineController
 * Created by HellFirePvP
 * Date: 12.07.2017 / 23:34
 */
public class GuiMachineController extends GuiContainerBase<ContainerController> {

    public static final ResourceLocation TEXTURES_CONTROLLER = new ResourceLocation(ModularMachinery.MODID, "textures/gui/guicontroller_large.png");

    private final TileMachineController controller;

    public GuiMachineController(TileMachineController controller, EntityPlayer opening) {
        super(new ContainerController(controller, opening));
        this.controller = controller;
        this.ySize = 213;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        GlStateManager.pushMatrix();
        double scale = 0.72;
        GlStateManager.scale(scale, scale, scale);
        int offsetX = 12;
        int offsetY = 12;

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        FontRenderer fr = this.fontRenderer;

        int redstone = controller.getWorld().getStrongPower(controller.getPos());
        if (redstone > 0) {
            String drawnStop = I18n.format("gui.controller.status.redstone_stopped");
            List<String> out = fr.listFormattedStringToWidth(drawnStop, MathHelper.floor(135 * (1 / scale)));
            for (String draw : out) {
                offsetY += 10;
                fr.drawStringWithShadow(draw, offsetX, offsetY, 0xFFFFFF);
                offsetY += 10;
            }
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
            return;
        }

        DynamicMachine machine = controller.getBlueprintMachine();
        String drawnHead;
        if (machine != null) {
            drawnHead = I18n.format("gui.controller.blueprint", "");
            List<String> out = fr.listFormattedStringToWidth(machine.getLocalizedName(), MathHelper.floor(135 * (1 / scale)));
            fr.drawStringWithShadow(drawnHead, offsetX, offsetY, 0xFFFFFF);
            for (String draw : out) {
                offsetY += 10;
                fr.drawStringWithShadow(draw, offsetX, offsetY, 0xFFFFFF);
            }
            offsetY += 15;
        } else if (!controller.isStructureFormed()) {
            drawnHead = I18n.format("gui.controller.blueprint", I18n.format("gui.controller.blueprint.none"));
            fr.drawStringWithShadow(drawnHead, offsetX, offsetY, 0xFFFFFF);
            offsetY += 15;
        }

        DynamicMachine found = controller.getFoundMachine();

        if (found != null) {
            drawnHead = I18n.format("gui.controller.structure", "");
            List<String> out = fr.listFormattedStringToWidth(found.getLocalizedName(), MathHelper.floor(135 * (1 / scale)));
            fr.drawStringWithShadow(drawnHead, offsetX, offsetY, 0xFFFFFF);
            for (String draw : out) {
                offsetY += 10;
                fr.drawStringWithShadow(draw, offsetX, offsetY, 0xFFFFFF);
            }

            //Render Extra Info
            ControllerGUIRenderEvent event = new ControllerGUIRenderEvent(controller);
            event.postEvent();

            String[] extraInfo = event.getExtraInfo();
            if (extraInfo.length != 0) {
                List<String> waitForDraw = new ArrayList<>();
                for (String s : extraInfo) {
                    waitForDraw.addAll(fr.listFormattedStringToWidth(s, MathHelper.floor(135 * (1 / GuiFactoryController.FONT_SCALE))));
                }
                offsetY += 5;
                for (String s : waitForDraw) {
                    offsetY += 10;
                    fr.drawStringWithShadow(s, offsetX, offsetY, 0xFFFFFF);
                }
            }
        } else {
            drawnHead = I18n.format("gui.controller.structure", I18n.format("gui.controller.structure.none"));
            fr.drawStringWithShadow(drawnHead, offsetX, offsetY, 0xFFFFFF);
        }
        offsetY += 15;

        String status = I18n.format("gui.controller.status");
        fr.drawStringWithShadow(status, offsetX, offsetY, 0xFFFFFF);
        String statusKey = controller.getControllerStatus().getUnlocMessage();

        List<String> out = fr.listFormattedStringToWidth(I18n.format(statusKey), MathHelper.floor(135 * (1 / scale)));
        for (String draw : out) {
            offsetY += 10;
            fr.drawStringWithShadow(draw, offsetX, offsetY, 0xFFFFFF);
        }
        offsetY += 15;

        ActiveMachineRecipe activeRecipe = controller.getActiveRecipe();
        if (activeRecipe != null) {
            if (activeRecipe.getTotalTick() > 0) {
                int progress = (activeRecipe.getTick() * 100) / activeRecipe.getTotalTick();
                String progressStr = I18n.format("gui.controller.status.crafting.progress",  progress + "%");
                fr.drawStringWithShadow(progressStr, offsetX, offsetY, 0xFFFFFF);
                offsetY += 15;
            }

            int parallelism = activeRecipe.getParallelism();
            int maxParallelism = activeRecipe.getMaxParallelism();
            if (parallelism > 1) {
                String parallelismStr = I18n.format("gui.controller.parallelism", parallelism);
                fr.drawStringWithShadow(parallelismStr, offsetX, offsetY, 0xFFFFFF);
                offsetY += 10;
                String maxParallelismStr = I18n.format("gui.controller.max_parallelism", maxParallelism);
                fr.drawStringWithShadow(maxParallelismStr, offsetX, offsetY, 0xFFFFFF);
                offsetY += 15;
            }
        }

        int usedTimeCache = TileMultiblockMachineController.usedTimeCache;
        float searchUsedTimeCache = TileMultiblockMachineController.searchUsedTimeCache;
        String workMode = TileMultiblockMachineController.workModeCache.getDisplayName();
        out = fr.listFormattedStringToWidth(
                String.format(
                        "Avg: %sμs/t (Search: %sms), WorkMode: %s", 
                        usedTimeCache, 
                        MiscUtils.formatFloat(searchUsedTimeCache / 1000F, 2), 
                        workMode
                ),
                MathHelper.floor(135 * (1 / scale))
        );
        for (String draw : out) {
            offsetY += 10;
            fr.drawStringWithShadow(draw, offsetX, offsetY, 0xFFFFFF);
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURES_CONTROLLER);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
    }

    @Override
    protected void setWidthHeight() {
    }
}
