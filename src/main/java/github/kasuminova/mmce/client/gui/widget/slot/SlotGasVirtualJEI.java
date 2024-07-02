package github.kasuminova.mmce.client.gui.widget.slot;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import hellfirepvp.modularmachinery.client.ClientProxy;
import hellfirepvp.modularmachinery.common.integration.ModIntegrationJEI;
import mekanism.api.gas.GasStack;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.config.KeyBindings;
import net.minecraftforge.fml.common.Optional;

public class SlotGasVirtualJEI extends SlotGasVirtual {

    public SlotGasVirtualJEI(final GasStack fluidStack) {
        super(fluidStack);
    }

    public SlotGasVirtualJEI() {
    }

    @Override
    public void render(final WidgetGui widgetGui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        super.render(widgetGui, renderSize, renderPos, mousePos);
    }

    @Override
    @Optional.Method(modid = "jei")
    public boolean onMouseClick(final MousePos mousePos, final RenderPos renderPos, final int mouseButton) {
        if (gasStack == null) {
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

        if (showRecipeKeyCode > 0 && showRecipeKeyCode <= 255 && showRecipeKeyCode == keyCode) {
            return showStackFocus(IFocus.Mode.OUTPUT);
        }
        if (showUsesKeyCode > 0 && showUsesKeyCode <= 255 && showUsesKeyCode == keyCode) {
            return showStackFocus(IFocus.Mode.INPUT);
        }

        return false;
    }

    @Optional.Method(modid = "jei")
    protected boolean showStackFocus(final IFocus.Mode output) {
        ClientProxy.clientScheduler.addRunnable(() -> {
            IFocus<GasStack> focus = ModIntegrationJEI.recipeRegistry.createFocus(output, gasStack);
            ModIntegrationJEI.jeiRuntime.getRecipesGui().show(focus);
        }, 0);
        return true;
    }

}
