package kport.gugu_utils.compat.jei;

import hellfirepvp.modularmachinery.common.integration.recipe.RecipeLayoutPart;
import net.minecraft.client.Minecraft;

import java.awt.*;

public abstract class LayoutWapper<T extends Object> extends RecipeLayoutPart<T> {
    private final int width;
    private final int height;
    private final int paddingX;
    private final int paddingY;
    private final int horizontalGap;
    private final int maxHorizontalCount;
    private final int verticalGap;
    private final int horizontalOrder;

    public LayoutWapper(Point offset, int width, int height, int paddingX, int paddingY, int horizontalGap, int verticalGap, int maxHorizontalCount, int horizontalOrder) {
        super(offset);
        this.width = width;
        this.height = height;
        this.paddingX = paddingX;
        this.paddingY = paddingY;
        this.horizontalGap = horizontalGap;
        this.maxHorizontalCount = maxHorizontalCount;
        this.verticalGap = verticalGap;
        this.horizontalOrder = horizontalOrder;
    }

    public LayoutWapper(Point offset, int width, int height, int wapperWidth, int wapperHeight, int contentOffsetX, int contentOffsetY, int horizontalGap, int verticalGap, int maxHorizontalCount, int horizontalOrder) {
        super(new Point());
        this.width = wapperWidth;
        this.height = wapperHeight;
        this.paddingX = (wapperWidth - width) / 2;
        this.paddingY = (wapperHeight - height) / 2;
        this.getOffset().setLocation(offset.x - this.paddingX + contentOffsetX, offset.y - this.paddingY + contentOffsetY);
        this.horizontalGap = horizontalGap;
        this.maxHorizontalCount = maxHorizontalCount;
        this.verticalGap = verticalGap;
        this.horizontalOrder = horizontalOrder;
    }

    @Override
    public int getComponentWidth() {
        return width;
    }

    @Override
    public int getComponentHeight() {
        return height;
    }

    @Override
    public int getRendererPaddingX() {
        return paddingX;
    }

    @Override
    public int getRendererPaddingY() {
        return paddingY;
    }

    @Override
    public int getMaxHorizontalCount() {
        return maxHorizontalCount;
    }

    @Override
    public int getComponentHorizontalGap() {
        return horizontalGap;
    }

    @Override
    public int getComponentVerticalGap() {
        return verticalGap;
    }

    @Override
    public int getComponentHorizontalSortingOrder() {
        return horizontalOrder;
    }

    @Override
    public boolean canBeScaled() {
        return true;
    }

    @Override
    public void drawBackground(Minecraft mc) {

    }
}
