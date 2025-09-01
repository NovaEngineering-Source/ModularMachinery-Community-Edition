package github.kasuminova.mmce.client.gui.integration.handler;

import appeng.client.gui.AEGuiHandler;
import appeng.container.interfaces.IJEIGhostIngredients;
import appeng.container.slot.IJEITargetSlot;
import github.kasuminova.mmce.client.gui.GuiMEItemInputBus;
import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.client.gui.GuiScreen;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MEInputGhostSlotHandler implements IGhostIngredientHandler<GuiMEItemInputBus>, IAdvancedGuiHandler<GuiMEItemInputBus> {

    private static final AEGuiHandler aeGuiHandler = new AEGuiHandler();


    @Override
    public <I> List<Target<I>> getTargets(GuiMEItemInputBus guiMEItemInputBus, I ingredient, boolean doStart) {
        ArrayList<Target<I>> targets = new ArrayList<>();
        IJEIGhostIngredients ghostGui = guiMEItemInputBus;
        List<Target<?>> phantomTargets = ghostGui.getPhantomTargets(ingredient);
        targets.addAll((List<Target<I>>) (Object) phantomTargets);
        if (doStart && GuiScreen.isShiftKeyDown() && Mouse.isButtonDown(0)) {
            for (Target<I> target : targets) {
                if (ghostGui.getFakeSlotTargetMap().get(target) instanceof IJEITargetSlot jeiSlot) {
                    if (jeiSlot.needAccept()) {
                        target.accept(ingredient);
                        break;
                    }
                }
            }
        }
        return targets;
    }

    @Override
    public void onComplete() {

    }

    @Override
    public Class<GuiMEItemInputBus> getGuiContainerClass() {
        return GuiMEItemInputBus.class;
    }

    @Nullable
    @Override
    public List<Rectangle> getGuiExtraAreas(GuiMEItemInputBus guiContainer) {
        return guiContainer.getJEIExclusionArea();
    }

    @Nullable
    @Override
    public Object getIngredientUnderMouse(GuiMEItemInputBus guiContainer, int mouseX, int mouseY) {
        return aeGuiHandler.getIngredientUnderMouse(guiContainer, mouseX, mouseY);
    }
}
