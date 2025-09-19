package github.kasuminova.mmce.client.gui.widget;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.widget.base.DynamicWidget;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

import java.util.LinkedList;
import java.util.List;

public class MultiLineLabel extends DynamicWidget {
    public static final int DEFAULT_FONT_HEIGHT = 10;

    protected List<String> contents;

    protected boolean leftAligned         = true;
    protected boolean rightAligned        = false;
    protected boolean verticalCentering   = false;
    protected boolean autoRecalculateSize = true;
    protected boolean autoWrap            = true;

    protected float scale = 1.0F;

    public MultiLineLabel(List<String> contents) {
        this.contents = contents;
        this.width = Math.round(getMaxStringWidth());
        this.height = Math.round(getTotalHeight());
        setMargin(2);
    }

    @Override
    public void render(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        final float scale = this.scale;
        if (scale != 1F) {
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, scale);
        }

        int maxWidth = (renderSize.isWidthLimited() && autoWrap) ? Math.round(Math.max((float) renderSize.width() / scale, DEFAULT_FONT_HEIGHT / scale)) : -1;
        int maxHeight = renderSize.isHeightLimited() ? Math.round((float) renderSize.height() / scale) : Math.round(height / scale);
        float totalHeight = getTotalHeight();
        float posX = renderPos.posX() / scale;
        float posY = renderPos.posY() / scale + (verticalCentering ? (maxHeight - totalHeight) / 2F : 0);

        FontRenderer fr = gui.getGui().mc.fontRenderer;

        List<String> toRender;
        if (maxWidth == -1) {
            toRender = contents;
        } else {
            toRender = new LinkedList<>();
            for (String s : contents) {
                toRender.addAll(fr.listFormattedStringToWidth(s, maxWidth));
            }
        }

        float offsetY = 0;
        float fontHeight = DEFAULT_FONT_HEIGHT * scale;
        for (final String s : toRender) {
            fr.drawStringWithShadow(s, posX + getLineRenderOffset(s, fr), posY + offsetY, 0xFFFFFF);
            offsetY += fontHeight;
            if (offsetY > maxHeight + fontHeight) {
                break;
            }
        }

        if (scale != 1F) {
            GlStateManager.popMatrix();
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F);
    }

    // Utils

    protected float getLineRenderOffset(final String s, final FontRenderer fr) {
        if (leftAligned && !rightAligned) {
            return 0;
        }

        int width = getWidth();
        float stringWidth = fr.getStringWidth(s) * scale;

        if (leftAligned && rightAligned) {
            return (width - (stringWidth)) / 2F;
        } else if (rightAligned) {
            return (width - (stringWidth));
        } else {
            return (width - (stringWidth)) / 2F;
        }
    }

    public float getMaxStringWidth() {
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;

        float maxWidth = 0;
        for (final String content : contents) {
            float width = fr.getStringWidth(content) * scale;
            if (width > maxWidth) {
                maxWidth = width;
            }
        }

        return maxWidth;
    }

    public float getTotalHeight() {
        return getTotalHeight(contents);
    }

    public float getTotalHeight(List<String> contents) {
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;

        float height = 0;
        for (final String content : contents) {
            List<String> listed = fr.listFormattedStringToWidth(content, Math.round(width / scale));
            height += (float) DEFAULT_FONT_HEIGHT * scale * listed.size();
        }

        return height * scale;
    }

    // Getters / Setters

    public float getScale() {
        return scale;
    }

    public MultiLineLabel setScale(final float scale) {
        this.scale = scale;
        this.height = Math.round(getTotalHeight());
        return this;
    }

    @Override
    public MultiLineLabel setWidth(final int width) {
        super.setWidth(width);
        this.height = Math.round(getTotalHeight());
        return this;
    }

    public List<String> getContents() {
        return contents;
    }

    public MultiLineLabel setContents(final List<String> contents) {
        this.contents = contents;
        if (autoRecalculateSize) {
            this.width = Math.round(getMaxStringWidth());
            this.height = Math.round(getTotalHeight());
        }
        return this;
    }

    // Align

    public boolean isLeftAligned() {
        return leftAligned;
    }

    public MultiLineLabel setLeftAligned(final boolean leftAligned) {
        this.rightAligned = !leftAligned;
        this.leftAligned = leftAligned;
        return this;
    }

    public boolean isRightAligned() {
        return rightAligned;
    }

    public MultiLineLabel setRightAligned(final boolean rightAligned) {
        this.leftAligned = !rightAligned;
        this.rightAligned = rightAligned;
        return this;
    }

    public boolean isCenterAligned() {
        return this.leftAligned && this.rightAligned;
    }

    public MultiLineLabel setCenterAligned(final boolean centerAligned) {
        if (centerAligned) {
            this.leftAligned = true;
            this.rightAligned = true;
            return this;
        }
        // Default setting is left aligned.
        this.leftAligned = true;
        this.rightAligned = false;
        return this;
    }

    public boolean isVerticalCentering() {
        return verticalCentering;
    }

    public MultiLineLabel setVerticalCentering(final boolean verticalCentering) {
        this.verticalCentering = verticalCentering;
        return this;
    }

    // Auto recalculate size

    public boolean isAutoRecalculateSize() {
        return autoRecalculateSize;
    }

    public MultiLineLabel setAutoRecalculateSize(final boolean autoRecalculateSize) {
        this.autoRecalculateSize = autoRecalculateSize;
        return this;
    }

    // Auto wrap

    public boolean isAutoWrap() {
        return autoWrap;
    }

    public MultiLineLabel setAutoWrap(final boolean autoWrap) {
        this.autoWrap = autoWrap;
        return this;
    }
}
