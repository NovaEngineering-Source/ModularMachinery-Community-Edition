package github.kasuminova.mmce.client.gui.widget.slot;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import hellfirepvp.modularmachinery.client.ClientProxy;
import hellfirepvp.modularmachinery.common.integration.ModIntegrationJEI;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.config.KeyBindings;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Optional;

public class SlotItemVirtualJEI extends SlotItemVirtual {

    protected boolean mouseOver = false;

    public SlotItemVirtualJEI() {
    }

    public SlotItemVirtualJEI(final ItemStack stackInSlot) {
        super(stackInSlot);
    }

    @Override
    public void render(final WidgetGui widgetGui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        super.render(widgetGui, renderSize, renderPos, mousePos);
        mouseOver = isMouseOver(mousePos);
    }

    @Override
    @Optional.Method(modid = "jei")
    public boolean onMouseClick(final MousePos mousePos, final RenderPos renderPos, final int mouseButton) {
        if (stackInSlot.isEmpty()) {
            return false;
        }
        return switch (mouseButton) {
            case 0 -> showStackFocus(IFocus.Mode.OUTPUT);
            case 1 -> showStackFocus(IFocus.Mode.INPUT);
            default -> false;
        };
    }

    @Override
    @Optional.Method(modid = "jei")
    public boolean onKeyTyped(final char typedChar, final int keyCode) {
        if (!mouseOver) {
            return false;
        }

        int showRecipeKeyCode = KeyBindings.showRecipe.getKeyCode();
        int showUsesKeyCode = KeyBindings.showUses.getKeyCode();
        int bookmarkKeyCode = KeyBindings.bookmark.getKeyCode();

        if (showRecipeKeyCode > 0 && showRecipeKeyCode <= 255 && showRecipeKeyCode == keyCode) {
            return showStackFocus(IFocus.Mode.OUTPUT);
        }
        if (showUsesKeyCode > 0 && showUsesKeyCode <= 255 && showUsesKeyCode == keyCode) {
            return showStackFocus(IFocus.Mode.INPUT);
        }
        if (bookmarkKeyCode > 0 && bookmarkKeyCode <= 255 && bookmarkKeyCode == keyCode) {
            ModIntegrationJEI.addItemStackToBookmarkList(stackInSlot);
        }

        return false;
    }

    @Optional.Method(modid = "jei")
    protected boolean showStackFocus(final IFocus.Mode output) {
        ClientProxy.clientScheduler.addRunnable(() -> {
            IFocus<ItemStack> focus = ModIntegrationJEI.recipeRegistry.createFocus(output, stackInSlot);
            ModIntegrationJEI.jeiRuntime.getRecipesGui().show(focus);
        }, 0);
        return true;
    }

}