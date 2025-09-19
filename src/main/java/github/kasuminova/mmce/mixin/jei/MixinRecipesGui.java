package github.kasuminova.mmce.mixin.jei;

import github.kasuminova.mmce.client.gui.integration.GuiBlueprintScreenJEI;
import hellfirepvp.modularmachinery.common.integration.preview.StructurePreviewWrapper;
import hellfirepvp.modularmachinery.common.item.ItemBlueprint;
import hellfirepvp.modularmachinery.common.lib.ItemsMM;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.gui.Focus;
import mezz.jei.gui.recipes.IRecipeGuiLogic;
import mezz.jei.gui.recipes.RecipeLayout;
import mezz.jei.gui.recipes.RecipesGui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@Mixin(RecipesGui.class)
public class MixinRecipesGui extends GuiScreen {

    @Final
    @Shadow(remap = false)
    private List<RecipeLayout> recipeLayouts;
    @Final
    @Shadow(remap = false)
    private IRecipeGuiLogic    logic;

    @Unique
    @Nonnull
    private static List<StructurePreviewWrapper> getStructurePreviewWrappers(final List<RecipeLayout> layouts) {
        List<StructurePreviewWrapper> wrappers = new ArrayList<>();
        for (RecipeLayout layout : layouts) {
            IRecipeWrapper wrapper = StructurePreviewWrapper.getWrapper(layout);
            if (!(wrapper instanceof StructurePreviewWrapper previewWrapper)) {
                continue;
            }
            wrappers.add(previewWrapper);
        }
        return wrappers;
    }

    @Redirect(method = "actionPerformed", at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;"))
    protected Object actionPerformedMixin(List<RecipeLayout> instance, int i) {
        boolean isPreview = false;
        for (RecipeLayout recipeLayout : recipeLayouts) {
            if (((AccessorRecipeLayout) recipeLayout).getRecipeWrapper() instanceof StructurePreviewWrapper s) {
                return randomComplement$getRecipe(s);
            }
        }
        return instance.get(i);
    }

    @Unique
    private RecipeLayout randomComplement$getRecipe(StructurePreviewWrapper preview) {
        ItemStack bOut = new ItemStack(ItemsMM.blueprint);
        ItemBlueprint.setAssociatedMachine(bOut, ((AccessorStructurePreviewWrapper) preview).getMachine());
        return RecipeLayout.create(0, this.logic.getSelectedRecipeCategory(), preview, new Focus<>(IFocus.Mode.OUTPUT, bOut), 0, 0);
    }

    @Inject(method = "handleMouseInput", at = @At("HEAD"), cancellable = true)
    public void onHandleMouseInput(final CallbackInfo ci) {
        if (mc == null) {
            return;
        }
        RecipesGui recipesGui = (RecipesGui) (Object) this;

        boolean find;
        List<RecipeLayout> layouts = StructurePreviewWrapper.getRecipeLayouts(recipesGui);
        if (layouts == null) {
            return;
        }

        List<StructurePreviewWrapper> wrappers = getStructurePreviewWrappers(layouts);
        find = !wrappers.isEmpty();
        for (final StructurePreviewWrapper wrapper : wrappers) {
            GuiBlueprintScreenJEI gui = wrapper.getGuiBlueprintScreenJEI();
            if (gui != null) {
                gui.handleMouseInput();
            }
        }

        if (!find) {
            return;
        }
        final int x = Mouse.getEventX() * recipesGui.width / recipesGui.mc.displayWidth;
        final int y = recipesGui.height - Mouse.getEventY() * recipesGui.height / recipesGui.mc.displayHeight - 1;
        if (recipesGui.isMouseOver(x, y) && Mouse.getEventDWheel() != 0) {
            ci.cancel();
        }
    }

    @Inject(method = "keyTyped", at = @At("HEAD"))
    public void onKeyTyped(final char typedChar, final int keyCode, final CallbackInfo ci) {
        if (mc == null) {
            return;
        }
        RecipesGui recipesGui = (RecipesGui) (Object) this;

        List<RecipeLayout> layouts = StructurePreviewWrapper.getRecipeLayouts(recipesGui);
        if (layouts == null) {
            return;
        }

        List<StructurePreviewWrapper> wrappers = getStructurePreviewWrappers(layouts);
        for (final StructurePreviewWrapper wrapper : wrappers) {
            GuiBlueprintScreenJEI gui = wrapper.getGuiBlueprintScreenJEI();
            if (gui != null) {
                gui.getWidgetController().onKeyTyped(typedChar, keyCode);
            }
        }
    }

    @Mixin(value = StructurePreviewWrapper.class, remap = false)
    public interface AccessorStructurePreviewWrapper {

        @Accessor
        DynamicMachine getMachine();
    }

    @Mixin(value = RecipeLayout.class, remap = false)
    public interface AccessorRecipeLayout {

        @Accessor
        IRecipeWrapper getRecipeWrapper();
    }
}
