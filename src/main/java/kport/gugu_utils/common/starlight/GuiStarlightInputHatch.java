package kport.gugu_utils.common.starlight;

import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.sky.RenderAstralSkybox;
import hellfirepvp.astralsorcery.client.util.Blending;
import hellfirepvp.astralsorcery.client.util.RenderConstellation;
import hellfirepvp.astralsorcery.client.util.SpriteLibrary;
import hellfirepvp.astralsorcery.client.util.TextureHelper;
import hellfirepvp.astralsorcery.client.util.resource.AssetLibrary;
import hellfirepvp.astralsorcery.client.util.resource.AssetLoader;
import hellfirepvp.astralsorcery.client.util.resource.BindableResource;
import hellfirepvp.astralsorcery.client.util.resource.SpriteSheetResource;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.common.util.data.Tuple;
import hellfirepvp.modularmachinery.ModularMachinery;
import kport.gugu_utils.tools.RenderUtils;
import kport.gugu_utils.tools.ResourceUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Collections;
import java.util.Random;

public class GuiStarlightInputHatch extends GuiContainer {
    public static final int WIDTH = 176;
    public static final int HEIGHT = 166;
    private static final Random rand = new Random();
    private static final BindableResource texBlack = AssetLibrary.loadTexture(AssetLoader.TextureLocation.MISC, "black");
    private static final ResourceLocation background = new ResourceLocation(ModularMachinery.MODID, "textures/gui/gui_starlightinputhatch.png");
    private static final String KEY_TOOLTIP_STARLIGHT = ResourceUtils.j("tooltips", ModularMachinery.MODID, "starlight");

    private final TileStarlightInputHatch hatch;
    private final ContainerStarlightInputHatch container;

    public GuiStarlightInputHatch(TileStarlightInputHatch tileEntity, ContainerStarlightInputHatch container) {
        super(container);

        xSize = WIDTH;
        ySize = HEIGHT;

        hatch = tileEntity;
        this.container = container;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();

        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        texBlack.bind();
        RenderUtils.drawRect(guiLeft + 6, guiTop + 69, zLevel, 165, 10);

        float percFilled;
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        percFilled = hatch.getAmbientStarlightPercent();


        if (percFilled > 0) {
            SpriteSheetResource spriteStarlight = SpriteLibrary.spriteStarlight;
            spriteStarlight.getResource().bindTexture();
            long t = hatch.getTicksExisted();
            Tuple<Double, Double> uvOffset = spriteStarlight.getUVOffset(t);
            RenderUtils.drawRect(guiLeft + 6, guiTop + 69, zLevel, (int) (165 * percFilled), 10,
                    uvOffset.key, uvOffset.value,
                    spriteStarlight.getULength() * percFilled, spriteStarlight.getVLength());


            int req = container.getClientLastRequirement();
            int has = container.getClientStarlightStored();
            if (has < req) {
                int max = hatch.getMaxStarlightStorage();
                req = Math.min(max, req);
                has = Math.min(max, has);
                float percReq = (float) (req - has) / (float) max;
                int from = (int) (165 * percFilled);
                int to = (int) (165 * percReq);
//
//                GL11.glColor4f(0.3F, 0.6F, 1.0F, 0.6F);
//                drawRect(guiLeft + 6 + from, guiTop + 69, zLevel, to, 10,
//                        uvOffset.key + spriteStarlight.getULength() * percFilled, uvOffset.value,
//                        spriteStarlight.getULength() * percReq, spriteStarlight.getVLength());
                GL11.glEnable(GL11.GL_ALPHA_TEST);
                GL11.glAlphaFunc(GL11.GL_GREATER, 0.4F);
                Color c = new Color(0x5078E7);
                GL11.glColor4f(c.getRed() / 255F, c.getGreen() / 255F, c.getBlue() / 255F, 1F);
                RenderUtils.drawRect(guiLeft + 6 + from, guiTop + 69, zLevel, to, 10,
                        uvOffset.key + spriteStarlight.getULength() * percFilled, uvOffset.value,
                        spriteStarlight.getULength() * percReq, spriteStarlight.getVLength());
                GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
                GL11.glDisable(GL11.GL_ALPHA_TEST);

            }

        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        mc.getTextureManager().bindTexture(background);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        GL11.glPopMatrix();
        GL11.glPopAttrib();

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);

        if (mouseX > guiLeft + 56 && mouseX < guiLeft + 120 && mouseY > guiTop + 4 && mouseY < guiTop + 68) {
            IConstellation c = container.getClientFocusedConstellation();
            if (c != null && ResearchManager.clientProgress.hasConstellationDiscovered(c.getUnlocalizedName())) {
                drawHoveringText(Collections.singletonList(I18n.format(c.getUnlocalizedName())), mouseX, mouseY, fontRenderer);
            }
        } else if (mouseX > guiLeft + 10 && mouseX < guiLeft + 168 && mouseY > guiTop + 70 && mouseY < guiTop + 77) {
            drawHoveringText(Collections.singletonList(I18n.format(KEY_TOOLTIP_STARLIGHT, hatch.getStarlightStored())), mouseX, mouseY, fontRenderer);
        }
    }

    @Override
    public void initGui() {
        this.xSize = WIDTH;
        this.ySize = HEIGHT;
        super.initGui();
    }

    @Override
    public void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        TextureHelper.refreshTextureBindState();

        float pTicks = Minecraft.getMinecraft().getRenderPartialTicks();


        RenderAstralSkybox.TEX_STAR_1.bind();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        Blending.DEFAULT.apply();
        rand.setSeed(0x889582997FF29A92L);
        for (int i = 0; i < 16; i++) {

            int x = rand.nextInt(54);
            int y = rand.nextInt(54);

            GL11.glPushMatrix();
            float brightness = 0.3F + (RenderConstellation.stdFlicker(ClientScheduler.getClientTick(), pTicks, 10 + rand.nextInt(20))) * 0.6F;
            GL11.glColor4f(brightness, brightness, brightness, brightness);
            RenderUtils.drawRect(60 + x, 7 + y, zLevel, 5, 5);
            GL11.glColor4f(1, 1, 1, 1);
            GL11.glPopMatrix();
        }

        GL11.glPopAttrib();
        TextureHelper.refreshTextureBindState();

        IConstellation c = container.getClientFocusedConstellation();
        if (c != null && ResearchManager.clientProgress.hasConstellationDiscovered(c.getUnlocalizedName())) {
            rand.setSeed(0x61FF25A5B7C24109L);

            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            GL11.glPushMatrix();

            GL11.glColor4f(1F, 1F, 1F, 1F);

            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_BLEND);
            Blending.DEFAULT.apply();

            RenderConstellation.renderConstellationIntoGUI(c, 61, 9, zLevel, 58, 58, 2, new RenderConstellation.BrightnessFunction() {
                @Override
                public float getBrightness() {
                    return RenderConstellation.conCFlicker(Minecraft.getMinecraft().world.getTotalWorldTime(), pTicks, 5 + rand.nextInt(5));
                }
            }, true, false);

            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_ALPHA_TEST);

            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }

        TextureHelper.setActiveTextureToAtlasSprite();
        TextureHelper.refreshTextureBindState();


    }

}
