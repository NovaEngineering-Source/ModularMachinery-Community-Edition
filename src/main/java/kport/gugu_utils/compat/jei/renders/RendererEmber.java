package kport.gugu_utils.compat.jei.renders;

import com.google.common.collect.Lists;
import hellfirepvp.modularmachinery.common.lib.BlocksMM;
import kport.gugu_utils.compat.jei.ingedients.IngredientEmber;
import kport.gugu_utils.common.embers.EmbersHatchVariant;
import kport.gugu_utils.tools.RenderUtils;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

public class RendererEmber implements IIngredientRenderer<IngredientEmber> {
    public static final RendererEmber INSTANCE = new RendererEmber();

    private static final String KEY_EMBER_AMOUNT = "tooltips.gugu-utils.ember_amount";
    private static final String KEY_EMBER_HATCH_REQUIREMENT = "tooltips.gugu-utils.ember_minimum_hatch";

    private RendererEmber() {

    }

    public EmbersHatchVariant getHatchLevel(double ember) {
        for (EmbersHatchVariant variant : EmbersHatchVariant.values()) {
            if (variant.getEmberMaxStorage() >= ember) {
                return variant;
            }
        }
        return EmbersHatchVariant.EXTREME;

    }

    @Override
    public void render(Minecraft minecraft, int xPosition, int yPosition, @Nullable IngredientEmber ingredient) {
        int maxEmber = Math.max(800, getHatchLevel((double) ingredient.getValue()).getEmberMaxStorage());

        GlStateManager.pushMatrix();
        GlStateManager.enableAlpha();
        RenderUtils.renderEmberBarVertical(xPosition, yPosition, 0.7f, (float) (double) ingredient.getValue() / maxEmber);
        GlStateManager.disableAlpha();
        GlStateManager.popMatrix();
    }

    @Override
    public List<String> getTooltip(Minecraft minecraft, IngredientEmber ingredient, ITooltipFlag tooltipFlag) {
        List<String> out = Lists.newLinkedList();

        if (ingredient.getValue() instanceof Double && ((double) ingredient.getValue()) > 0) {
            out.add(I18n.format(KEY_EMBER_AMOUNT, ingredient.getValue()));

            int meta = getHatchLevel((Double) ingredient.getValue()).ordinal();
            out.add(I18n.format(KEY_EMBER_HATCH_REQUIREMENT, new ItemStack(BlocksMM.blockEmberInputProvider, 1, meta).getDisplayName()));
        }

        return out;
    }
}
