package kport.modularmagic.common.integration.jei.render;

import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.sky.RenderAstralSkybox;
import hellfirepvp.astralsorcery.client.util.RenderConstellation;
import hellfirepvp.astralsorcery.client.util.resource.AssetLibrary;
import hellfirepvp.astralsorcery.client.util.resource.AssetLoader;
import hellfirepvp.astralsorcery.client.util.resource.BindableResource;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import kport.modularmagic.common.integration.jei.ingredient.Constellation;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ConstellationRenderer implements IIngredientRenderer<Constellation> {

    private static final BindableResource texBlack = AssetLibrary.loadTexture(AssetLoader.TextureLocation.MISC, "black");
    private              Random           rand     = new Random();

    @Override
    public void render(Minecraft minecraft, int xPosition, int yPosition, @Nullable Constellation ingredient) {
        if (ingredient == null) {
            return;
        }

        GlStateManager.pushMatrix();

        texBlack.bind();
        drawRect(xPosition, yPosition, 58, 58);

        RenderAstralSkybox.TEX_STAR_1.bind();
        rand.setSeed(0x889582997FF29A92L);
        for (int i = 0; i < 16; i++) {

            int x = rand.nextInt(54);
            int y = rand.nextInt(54);

            GlStateManager.pushMatrix();
            float brightness = 0.3F + (RenderConstellation.stdFlicker(ClientScheduler.getClientTick(), minecraft.getRenderPartialTicks(), 10 + rand.nextInt(20))) * 0.6F;
            GlStateManager.color(brightness, brightness, brightness, brightness);
            drawRect(xPosition + x, yPosition + y, 5, 5);
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.popMatrix();
        }

        IConstellation constellation = ingredient.getConstellation();
        if (constellation != null && canSeeConstellation(constellation)) {
            rand.setSeed(0x61FF25A5B7C24109L);
            RenderConstellation.renderConstellationIntoGUI(constellation, xPosition, yPosition, 0, 58, 58, 2, new RenderConstellation.BrightnessFunction() {
                @Override
                public float getBrightness() {
                    return RenderConstellation.conCFlicker(Minecraft.getMinecraft().world.getTotalWorldTime(), minecraft.getRenderPartialTicks(), 5 + rand.nextInt(5));
                }
            }, true, false);
        }

        GlStateManager.popMatrix();
    }

    @Override
    public List<String> getTooltip(Minecraft minecraft, Constellation ingredient, ITooltipFlag tooltipFlag) {
        List<String> tooltip = new ArrayList<>();
        if (canSeeConstellation(ingredient.getConstellation())) {
            tooltip.add(I18n.format(ingredient.getConstellation().getUnlocalizedName()));
        } else {
            tooltip.add(I18n.format("error.modularmachinery.requirement.constellation.unknown"));
        }
        return tooltip;
    }

    @Override
    public FontRenderer getFontRenderer(Minecraft minecraft, Constellation ingredient) {
        return minecraft.fontRenderer;
    }

    public boolean canSeeConstellation(IConstellation constellation) {
        return ResearchManager.clientProgress.hasConstellationDiscovered(constellation);
    }

    private void drawRect(int offsetX, int offsetY, int width, int height) {
        Tessellator tes = Tessellator.getInstance();
        BufferBuilder vb = tes.getBuffer();
        vb.begin(7, DefaultVertexFormats.POSITION_TEX);
        vb.pos(offsetX, offsetY + height, 0).tex(0, 1).endVertex();
        vb.pos(offsetX + width, offsetY + height, 0).tex(1, 1).endVertex();
        vb.pos(offsetX + width, offsetY, 0).tex(1, 0).endVertex();
        vb.pos(offsetX, offsetY, 0).tex(0, 0).endVertex();
        tes.draw();
    }


}
