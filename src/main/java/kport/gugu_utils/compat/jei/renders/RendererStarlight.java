package kport.gugu_utils.compat.jei.renders;

import codechicken.lib.render.state.GlStateTracker;
import com.google.common.collect.Lists;
import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.util.RenderConstellation;
import hellfirepvp.astralsorcery.client.util.TextureHelper;
import hellfirepvp.astralsorcery.client.util.resource.AssetLibrary;
import hellfirepvp.astralsorcery.client.util.resource.AssetLoader;
import hellfirepvp.astralsorcery.client.util.resource.BindableResource;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.modularmachinery.common.lib.BlocksMM;
import kport.gugu_utils.compat.jei.ingedients.IngredientStarlight;
import kport.gugu_utils.common.starlight.StarlightHatchVariant;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

public class RendererStarlight implements IIngredientRenderer<IngredientStarlight> {
    public static final RendererStarlight INSTANCE = new RendererStarlight();

    static final BindableResource resStar = AssetLibrary.loadTexture(AssetLoader.TextureLocation.ENVIRONMENT, "star1");

    @Override
    public void render(Minecraft minecraft, int offsetX, int offsetY, @Nullable IngredientStarlight ingredient) {
        GlStateTracker.pushState();
        GlStateManager.pushAttrib();
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        drawInfoStar(offsetX + 8, offsetY + 8, 10, 16, minecraft.getRenderPartialTicks());
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.popAttrib();
        GlStateManager.color(1F, 1F, 1F, 1F);

        if (ingredient != null && ingredient.getConstellation() != null) {
            IConstellation focus = ingredient.getConstellation();
            GlStateManager.pushAttrib();
            GlStateManager.disableDepth();
            GlStateManager.disableAlpha();
            RenderConstellation.renderConstellationIntoGUI(new Color(0xEEEEEE), focus,
                    offsetX - 15, offsetY - 12, 0,
                    75, 75, 2F, new RenderConstellation.BrightnessFunction() {
                        @Override
                        public float getBrightness() {
                            return 0.3F;
                        }
                    }, true, false);
            GlStateManager.enableAlpha();
            GlStateManager.enableDepth();
            GlStateManager.popAttrib();
        }
        GlStateTracker.popState();
        GlStateManager.color(1F, 1F, 1F, 1F);
    }



    @Override
    public List<String> getTooltip(Minecraft minecraft, IngredientStarlight ingredient, ITooltipFlag tooltipFlag) {
        List<String> out = Lists.newLinkedList();

        if (ingredient.getValue() instanceof Integer && ((int) ingredient.getValue()) > 0) {

            StarlightHatchVariant highestPossible = StarlightHatchVariant.BRILLIANT;

            long indexSel = (ClientScheduler.getClientTick() / 30) % (highestPossible.ordinal() + 1);
            StarlightHatchVariant levelSelected = StarlightHatchVariant.values()[((int) indexSel)];
            int max = levelSelected.getStarlightMaxStorage();
            Item i = Item.getItemFromBlock(BlocksMM.blockStarlightProviderInput);
            String locTier = i.getTranslationKey(new ItemStack(i, 1, levelSelected.ordinal()));
            locTier = I18n.format(locTier + ".name");
            String displReq = "  " + getDescriptionFromStarlightAmount(locTier, (int) ingredient.getValue(), max);
            String dsc = I18n.format("astralsorcery.journal.recipe.amt.desc");
            out.add(dsc);
            out.add(displReq);
        }

        if (ingredient.getConstellation() != null) {
            String ulName = ingredient.getConstellation().getUnlocalizedName();
            out.add(I18n.format("tooltips.gugu-utils.starlight.constellation", I18n.format(ulName)));
        }
        return out;
    }


    public String getDescriptionFromStarlightAmount(String locTierTitle, int amtRequired, int maxAmount) {
        String base = "astralsorcery.journal.recipe.amt.";
        String ext;
        float perc = ((float) amtRequired) / ((float) maxAmount);
        if (perc <= 0.1) {
            ext = "lowest";
        } else if (perc <= 0.25) {
            ext = "low";
        } else if (perc <= 0.5) {
            ext = "avg";
        } else if (perc <= 0.75) {
            ext = "more";
        } else if (perc <= 0.9) {
            ext = "high";
        } else if (perc > 1) {
            ext = "toomuch";
        } else {
            ext = "highest";
        }
        return String.format("%s: %s", locTierTitle, I18n.format(String.format("%s%s", base, ext)));
    }


    public Rectangle drawInfoStar(float offsetX, float offsetY, float zLevel, float widthHeightBase, float pTicks) {

        float tick = ClientScheduler.getClientTick() + pTicks;
        float deg = (tick * 2) % 360F;
        float wh = widthHeightBase - (widthHeightBase / 6F) * (MathHelper.sin((float) Math.toRadians(((tick) * 4) % 360F)) + 1F);
        drawInfoStarSingle(offsetX, offsetY, zLevel, wh, Math.toRadians(deg));

        deg = ((tick + 22.5F) * 2) % 360F;
        wh = widthHeightBase - (widthHeightBase / 6F) * (MathHelper.sin((float) Math.toRadians(((tick + 45F) * 4) % 360F)) + 1F);
        drawInfoStarSingle(offsetX, offsetY, zLevel, wh, Math.toRadians(deg));

        return new Rectangle(MathHelper.floor(offsetX - widthHeightBase / 2F), MathHelper.floor(offsetY - widthHeightBase / 2F),
                MathHelper.floor(widthHeightBase), MathHelper.floor(widthHeightBase));
    }

    public void drawInfoStarSingle(float offsetX, float offsetY, float zLevel, float widthHeight, double deg) {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();

        resStar.bind();
        Vector3 offset = new Vector3(-widthHeight / 2D, -widthHeight / 2D, 0).rotate(deg, Vector3.RotAxis.Z_AXIS);
        Vector3 uv01 = new Vector3(-widthHeight / 2D, widthHeight / 2D, 0).rotate(deg, Vector3.RotAxis.Z_AXIS);
        Vector3 uv11 = new Vector3(widthHeight / 2D, widthHeight / 2D, 0).rotate(deg, Vector3.RotAxis.Z_AXIS);
        Vector3 uv10 = new Vector3(widthHeight / 2D, -widthHeight / 2D, 0).rotate(deg, Vector3.RotAxis.Z_AXIS);

        Tessellator tes = Tessellator.getInstance();
        BufferBuilder vb = tes.getBuffer();
        vb.begin(7, DefaultVertexFormats.POSITION_TEX);
        vb.pos(offsetX + uv01.getX(), offsetY + uv01.getY(), zLevel).tex(0, 1).endVertex();
        vb.pos(offsetX + uv11.getX(), offsetY + uv11.getY(), zLevel).tex(1, 1).endVertex();
        vb.pos(offsetX + uv10.getX(), offsetY + uv10.getY(), zLevel).tex(1, 0).endVertex();
        vb.pos(offsetX + offset.getX(), offsetY + offset.getY(), zLevel).tex(0, 0).endVertex();
        tes.draw();

        TextureHelper.refreshTextureBindState();
        TextureHelper.setActiveTextureToAtlasSprite();
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }


}
