package github.kasuminova.mmce.mixin.jei;

import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.widget.base.WidgetController;
import mezz.jei.gui.recipes.RecipeLayout;
import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.LinkedList;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(RecipeLayout.class)
public class MixinRecipeLayout {

    private static final ThreadLocal<LinkedList<RenderPos>> TRANSLATE_STACK = ThreadLocal.withInitial(LinkedList::new);

    @Redirect(method = "drawRecipe",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GlStateManager;translate(FFF)V",
            remap = true),
        remap = false)
    public void redirectGLTranslate(final float x, final float y, final float z) {
        GlStateManager.translate(x, y, z);
        RenderPos offset = new RenderPos((int) x, (int) y);
        WidgetController.TRANSLATE_STATE.set(WidgetController.TRANSLATE_STATE.get().add(offset));
        TRANSLATE_STACK.get().addFirst(offset);
    }

    @Redirect(method = "drawRecipe",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GlStateManager;popMatrix()V",
            remap = true),
        remap = false)
    public void redirectGLTranslatePop() {
        RenderPos removedOffset = TRANSLATE_STACK.get().removeFirst();
        WidgetController.TRANSLATE_STATE.set(WidgetController.TRANSLATE_STATE.get().subtract(removedOffset));
        GlStateManager.popMatrix();
    }

}
