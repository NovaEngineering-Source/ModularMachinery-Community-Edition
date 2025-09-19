/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.integration.recipe;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.integration.ModIntegrationJEI;
import mezz.jei.api.gui.IDrawable;
import net.minecraft.util.ResourceLocation;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: RecipeLayoutHelper
 * Created by HellFirePvP
 * Date: 11.07.2017 / 15:09
 */
public class RecipeLayoutHelper {

    static final ResourceLocation LOCATION_JEI_ICONS = new ResourceLocation(ModularMachinery.MODID, "textures/gui/jeirecipeicons_ce.png");

    static RecipePart PART_TANK_SHELL;
    static RecipePart PART_GAS_TANK_SHELL;
    static RecipePart PART_TANK_SHELL_BACKGROUND;
    static RecipePart PART_ENERGY_BACKGROUND;
    static RecipePart PART_ENERGY_FOREGROUND;
    static RecipePart PART_INVENTORY_CELL;
    static RecipePart PART_PROCESS_ARROW;
    static RecipePart PART_PROCESS_ARROW_ACTIVE;

    public static void init() {
        if (PART_TANK_SHELL != null) {
            return;
        }

        PART_TANK_SHELL = new RecipePart(LOCATION_JEI_ICONS, 0, 0, 18, 18);
        PART_GAS_TANK_SHELL = new RecipePart(LOCATION_JEI_ICONS, 0, 18, 18, 18);
        PART_TANK_SHELL_BACKGROUND = new RecipePart(LOCATION_JEI_ICONS, 54, 0, 18, 18);
        PART_ENERGY_FOREGROUND = new RecipePart(LOCATION_JEI_ICONS, 18, 0, 18, 54);
        PART_ENERGY_BACKGROUND = new RecipePart(LOCATION_JEI_ICONS, 36, 0, 18, 54);
        PART_INVENTORY_CELL = new RecipePart(LOCATION_JEI_ICONS, 54, 0, 18, 18);
        PART_PROCESS_ARROW = new RecipePart(LOCATION_JEI_ICONS, 72, 0, 22, 15);
        PART_PROCESS_ARROW_ACTIVE = new RecipePart(LOCATION_JEI_ICONS, 72, 15, 22, 15);
    }

    public static class RecipePart {

        public final IDrawable drawable;
        public final int       xSize, zSize;

        public RecipePart(ResourceLocation location, int textureX, int textureZ, int xSize, int zSize) {
            this.drawable = ModIntegrationJEI.jeiHelpers.getGuiHelper().createDrawable(location, textureX, textureZ, xSize, zSize);
            this.xSize = xSize;
            this.zSize = zSize;
        }

    }

}