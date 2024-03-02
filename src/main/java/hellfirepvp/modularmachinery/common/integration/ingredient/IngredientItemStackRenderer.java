package hellfirepvp.modularmachinery.common.integration.ingredient;

import hellfirepvp.modularmachinery.common.util.MiscUtils;
import mezz.jei.plugins.vanilla.ingredients.item.ItemStackRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IngredientItemStackRenderer extends ItemStackRenderer {

    public static final IngredientItemStackRenderer DEFAULT_INSTANCE = new IngredientItemStackRenderer(Collections.emptyList());

    private final List<IngredientItemStack> preDefined = new ArrayList<>();

    public IngredientItemStackRenderer(final List<IngredientItemStack> preDefined) {
        this.preDefined.addAll(preDefined);
    }

    public static void renderRequirementOverlyIntoGUI(final FontRenderer fr, final int xPos, final int yPos, int min, int max) {
        String s;

        float smallScale = .5f;
        float defaultScale = 1f;
        float scale;
        if (min == max) {
            if (min <= 1) {
                return;
            }
            scale = min >= 1000 ? smallScale : defaultScale;
            s = String.format("%s", isShiftDown() ? min : MiscUtils.formatNumberToInt(min));
        } else {
            scale = smallScale;
            s = String.format("%s~%s", MiscUtils.formatNumberToInt(min), MiscUtils.formatNumberToInt(max));
        }

        GlStateManager.disableLighting();
        GlStateManager.disableBlend();
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1.0F);
        GlStateManager.translate(0.0F, 0.0F, 160F);

        boolean unicodeFlag = fr.getUnicodeFlag();
        if (scale == smallScale) {
            fr.setUnicodeFlag(false);
            fr.drawStringWithShadow(s, (xPos + 16 - (fr.getStringWidth(s) * scale)) / scale, (yPos + 12) / scale, 0xFFFFFF);
        } else {
            fr.drawStringWithShadow(s, xPos + 17 - fr.getStringWidth(s), yPos + 9, 0xFFFFFF);
        }
        fr.setUnicodeFlag(unicodeFlag);

        GlStateManager.popMatrix();
        GlStateManager.enableBlend();
        GlStateManager.enableLighting();
    }

    protected static boolean isShiftDown() {
        return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
    }

    @Override
    public void render(@Nonnull final Minecraft minecraft, final int xPos, final int yPos, @Nullable final ItemStack stack) {
        if (stack == null) {
            return;
        }

        GlStateManager.enableDepth();
        RenderHelper.enableGUIStandardItemLighting();
        FontRenderer font = getFontRenderer(minecraft, stack);

        minecraft.getRenderItem().renderItemAndEffectIntoGUI(null, stack, xPos, yPos);
        IngredientItemStack ingredient = findStack(stack);
        if (ingredient == null) {
            renderRequirementOverlyIntoGUI(font, xPos, yPos, stack.getCount(), stack.getCount());
            minecraft.getRenderItem().renderItemOverlayIntoGUI(font, stack, xPos, yPos, "");
            return;
        } else {
            int min = ingredient.min();
            int max = ingredient.max();
            renderRequirementOverlyIntoGUI(font, xPos, yPos, min, max);
            minecraft.getRenderItem().renderItemOverlayIntoGUI(font, stack, xPos, yPos, "");
        }

        GlStateManager.disableBlend();
        RenderHelper.disableStandardItemLighting();
    }

    @Nullable
    protected IngredientItemStack findStack(final ItemStack stack) {
        if (preDefined.isEmpty()) {
            return null;
        }
        return preDefined.stream()
                .filter(ingredient -> ingredient.stack() == stack)
                .findFirst()
                .orElse(null);
    }
}
