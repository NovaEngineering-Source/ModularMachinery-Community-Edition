/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.integration.preview;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.client.gui.GuiScreenBlueprint;
import hellfirepvp.modularmachinery.common.integration.ModIntegrationJEI;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: CategoryStructurePreview
 * Created by HellFirePvP
 * Date: 11.07.2017 / 12:36
 */
public class CategoryStructurePreview implements IRecipeCategory<StructurePreviewWrapper> {

    private final IDrawable background;
    private final String    trTitle;

    public CategoryStructurePreview() {
        ResourceLocation location = new ResourceLocation(ModularMachinery.MODID, "textures/gui/guiblueprint_new.png");
        this.background = ModIntegrationJEI.jeiHelpers.getGuiHelper()
                                                      .drawableBuilder(location, 0, 0, GuiScreenBlueprint.X_SIZE, GuiScreenBlueprint.Y_SIZE)
                                                      .addPadding(0, 0, 0, 0)
                                                      .build();
        this.trTitle = I18n.format("jei.category.preview");
    }

    @Nonnull
    @Override
    public String getUid() {
        return ModIntegrationJEI.CATEGORY_PREVIEW;
    }

    @Nonnull
    @Override
    public String getTitle() {
        return trTitle;
    }

    @Nonnull
    @Override
    public String getModName() {
        return ModularMachinery.NAME;
    }

    @Nonnull
    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, @Nonnull StructurePreviewWrapper recipeWrapper, IIngredients ingredients) {
        IGuiItemStackGroup group = recipeLayout.getItemStacks();
        group.init(0, false, -999999, -999999);

        for (int i = 0; i < Math.min(ingredients.getInputs(VanillaTypes.ITEM).size(), 81); i++) {
            group.init(1 + i, true, -999999, -999999);
        }

        group.set(ingredients);
    }

}
