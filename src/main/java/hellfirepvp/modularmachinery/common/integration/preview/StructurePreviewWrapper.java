/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.integration.preview;

import github.kasuminova.mmce.client.gui.integration.GuiBlueprintScreenJEI;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.widget.base.WidgetController;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import github.kasuminova.mmce.client.preivew.PreviewPanels;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.base.Mods;
import hellfirepvp.modularmachinery.common.block.BlockController;
import hellfirepvp.modularmachinery.common.block.BlockFactoryController;
import hellfirepvp.modularmachinery.common.item.ItemBlueprint;
import hellfirepvp.modularmachinery.common.lib.ItemsMM;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.modifier.AbstractModifierReplacement;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.gui.recipes.RecipeLayout;
import mezz.jei.gui.recipes.RecipesGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: StructurePreviewWrapper
 * Created by HellFirePvP
 * Date: 11.07.2017 / 12:38
 */
public class StructurePreviewWrapper implements IRecipeWrapper {
    private static final Field recipeLayouts;
    private static final Field recipeWrapper;

    //I blame mezz for this, making stuff not accessible and badly organizing the original values,
    //so it's horrible to draw custom stuff onto the GUI frame.
    static {
        Field field;
        try {
            field = RecipesGui.class.getDeclaredField("recipeLayouts");
            field.setAccessible(true);
        } catch (Exception exc) {
            field = null;
        }
        recipeLayouts = field;
        try {
            field = RecipeLayout.class.getDeclaredField("recipeWrapper");
            field.setAccessible(true);
        } catch (Exception exc) {
            field = null;
        }
        recipeWrapper = field;
    }

    private final DynamicMachine        machine;
    private       GuiBlueprintScreenJEI gui = null;

    public StructurePreviewWrapper(DynamicMachine machine) {
        this.machine = machine;
    }

    public static IRecipeWrapper getWrapper(RecipeLayout layout) {
        try {
            return (IRecipeWrapper) recipeWrapper.get(layout);
        } catch (IllegalAccessException e) {
            ModularMachinery.log.error(e);
        }
        return null;
    }

    public static List<RecipeLayout> getRecipeLayouts(RecipesGui recipesGui) {
        try {
            return (List<RecipeLayout>) recipeLayouts.get(recipesGui);
        } catch (IllegalAccessException e) {
            ModularMachinery.log.error(e);
        }
        return null;
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        GuiScreen current = Minecraft.getMinecraft().currentScreen;
        World clWorld = minecraft.world;
        if (clWorld == null || !(current instanceof RecipesGui recipesGUI)) {
            return; //Wtf. where are we rendering in.
        }

        RenderPos translateOffset = WidgetController.TRANSLATE_STATE.get();
        int guiLeft = translateOffset.posX();
        int guiTop = translateOffset.posY();

        if (gui == null) {
            gui = new GuiBlueprintScreenJEI();
            gui.setWorldAndResolution(minecraft, recipesGUI.width, recipesGUI.height);
            gui.setWidgetController(new WidgetController(WidgetGui.of(gui, recipeWidth, recipeHeight, guiLeft, guiTop)));
        }
        gui.width = recipesGUI.width;
        gui.height = recipesGUI.height;
        gui.setGuiLeft(guiLeft);
        gui.setGuiTop(guiTop);
        gui.getWidgetController().getGui().setGuiLeft(guiLeft).setGuiTop(guiTop);

        WidgetController controller = gui.getWidgetController();
        controller.getWidgets().clear();
        controller.addWidget(PreviewPanels.getPanel(machine, controller.getGui()));

        gui.updateScreen();
        gui.drawScreen(mouseX + guiLeft, mouseY + guiTop, minecraft.getRenderPartialTicks());
    }

    public GuiBlueprintScreenJEI getGuiBlueprintScreenJEI() {
        return gui;
    }

    @Override
    public void getIngredients(@Nonnull IIngredients ingredients) {
        ItemStack bOut = new ItemStack(ItemsMM.blueprint);
        ItemBlueprint.setAssociatedMachine(bOut, this.machine);
        BlockController ctrl = BlockController.getControllerWithMachine(this.machine);
        List<ItemStack> stackList = new ArrayList<>();
        if (ctrl != null) {
            ItemStack ctrlStack = new ItemStack(ctrl);
            stackList.add(ctrlStack);
        }
        BlockController mocCtrl = BlockController.getMocControllerWithMachine(this.machine);
        if (mocCtrl != null) {
            ItemStack ctrlStack = new ItemStack(mocCtrl);
            stackList.add(ctrlStack);
        }
        BlockFactoryController factory = BlockFactoryController.getControllerWithMachine(this.machine);
        if (factory != null) {
            ItemStack factoryStack = new ItemStack(factory);
            stackList.add(factoryStack);
        }
        stackList.add(bOut);

        List<List<ItemStack>> ingredientList;
        if (gui != null && Mods.AE2.isPresent()) {
            WidgetController controller = this.gui.getWidgetController();
            var panel = PreviewPanels.getPanel(this.machine, controller.getGui()).getRenderer();
            var list = panel.getPattern().getDescriptiveStackList(panel.getTickSnap(), panel.getWorldRenderer().getWorld(), panel.getRenderOffset());
            list.remove(0);
            List<List<ItemStack>> finalList = new ArrayList<>();
            list.forEach(itemStack -> finalList.add(Collections.singletonList(itemStack)));
            ingredientList = finalList;
        } else {
            ingredientList = this.machine.getPattern().getIngredientList();
        }
        machine.getModifiers().values().stream()
               .flatMap(Collection::stream)
               .map(AbstractModifierReplacement::getDescriptiveStack)
               .forEach(stack -> ingredientList.add(Collections.singletonList(stack)));
        machine.getMultiBlockModifiers().stream()
               .map(AbstractModifierReplacement::getDescriptiveStack)
               .forEach(stack -> ingredientList.add(Collections.singletonList(stack)));

        ingredients.setInputLists(VanillaTypes.ITEM, ingredientList);
        ingredients.setOutputs(VanillaTypes.ITEM, stackList);
    }
}
