package github.kasuminova.mmce.client.gui.util;

import com.github.bsideup.jabel.Desugar;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.function.Consumer;

@Desugar
@ParametersAreNonnullByDefault
@SuppressWarnings("unused")
public record TextureProperties(@Nullable ResourceLocation texRes, int texX, int texY, int width, int height) {

    public static final TextureProperties EMPTY = new TextureProperties(null, 0, 0, 0, 0);

    public static TextureProperties of(@Nullable final ResourceLocation texRes, final int texX, final int texY, final int width, final int height) {
        return new TextureProperties(texRes, texX, texY, width, height);
    }

    public static TextureProperties of(final int texX, final int texY, final int width, final int height) {
        return of(null, texX, texY, width, height);
    }

    public static TextureProperties of(final int texX, final int texY, final int widthHeight) {
        return of(null, texX, texY, widthHeight, widthHeight);
    }

    public static TextureProperties of(@Nullable final ResourceLocation texRes, final int texX, final int texY) {
        return of(texRes, texX, texY, 0, 0);
    }

    public static TextureProperties of(final int texX, final int texY) {
        return of(null, texX, texY);
    }

    public void bind(final TextureManager textureManager) {
        if (texRes != null) {
            bind(textureManager, texRes);
        }
    }

    private static void bind(final TextureManager textureManager, final ResourceLocation texRes) {
        textureManager.bindTexture(texRes);
    }

    // Default render

    public void render(final RenderPos renderPos, final WidgetGui gui) {
        render(renderPos, gui.getGui());
    }

    public void render(final RenderPos renderPos, final GuiScreen gui) {
        bind(gui.mc.getTextureManager());
        gui.drawTexturedModalRect(renderPos.posX(), renderPos.posY(), texX, texY, width, height);
    }

    public void renderIfPresent(final RenderPos renderPos, final WidgetGui gui) {
        ifPresent(t -> t.render(renderPos, gui));
    }

    public void renderIfPresent(final RenderPos renderPos, final GuiScreen gui) {
        ifPresent(t -> t.render(renderPos, gui));
    }

    public void renderIfPresent(final RenderPos renderPos, final WidgetGui gui, final Consumer<TextureProperties> before) {
        ifPresent(t -> before.andThen(t1 -> t.render(renderPos, gui)).accept(t));
    }

    public void renderIfPresent(final RenderPos renderPos, final GuiScreen gui, final Consumer<TextureProperties> before) {
        ifPresent(t -> before.andThen(t1 -> t.render(renderPos, gui)).accept(t));
    }

    public void renderIfPresent(final RenderPos renderPos, final WidgetGui gui, @Nullable final Consumer<TextureProperties> before, @Nullable final Consumer<TextureProperties> after) {
        ifPresent(t -> {
            Optional.ofNullable(before).ifPresent(bc -> bc.accept(t));
            t.render(renderPos, gui);
            Optional.ofNullable(after).ifPresent(ac -> ac.accept(t));
        });
    }

    public void renderIfPresent(final RenderPos renderPos, final GuiScreen gui, @Nullable final Consumer<TextureProperties> before, @Nullable final Consumer<TextureProperties> after) {
        ifPresent(t -> {
            Optional.ofNullable(before).ifPresent(bc -> bc.accept(t));
            t.render(renderPos, gui);
            Optional.ofNullable(after).ifPresent(ac -> ac.accept(t));
        });
    }

    public void ifPresent(final Consumer<TextureProperties> runnable) {
        if (texRes != null && width > 0 && height > 0) {
            runnable.accept(this);
        }
    }

    // Custom texture render

    public void render(@Nullable final ResourceLocation customTexRes, final RenderPos renderPos, final WidgetGui gui) {
        render(customTexRes, renderPos, gui.getGui());
    }

    public void render(@Nullable final ResourceLocation customTexRes, final RenderPos renderPos, final GuiScreen gui) {
        TextureManager textureManager = gui.mc.getTextureManager();
        if (texRes != null) {
            bind(textureManager);
        } else {
            if (customTexRes != null) {
                bind(textureManager, customTexRes);
            } else {
                return;
            }
        }
        gui.drawTexturedModalRect(renderPos.posX(), renderPos.posY(), texX, texY, width, height);
    }

    public void renderIfPresent(@Nullable final ResourceLocation customTexRes, final RenderPos renderPos, final WidgetGui gui) {
        ifSizePresent(t -> t.render(customTexRes, renderPos, gui));
    }

    public void renderIfPresent(@Nullable final ResourceLocation customTexRes, final RenderPos renderPos, final GuiScreen gui) {
        ifSizePresent(t -> t.render(customTexRes, renderPos, gui));
    }

    public void renderIfPresent(@Nullable final ResourceLocation customTexRes, final RenderPos renderPos, final WidgetGui gui, final Consumer<TextureProperties> before) {
        ifSizePresent(t -> before.andThen(t1 -> t.render(customTexRes, renderPos, gui)).accept(t));
    }

    public void renderIfPresent(@Nullable final ResourceLocation customTexRes, final RenderPos renderPos, final GuiScreen gui, final Consumer<TextureProperties> before) {
        ifSizePresent(t -> before.andThen(t1 -> t.render(customTexRes, renderPos, gui)).accept(t));
    }

    public void renderIfPresent(@Nullable final ResourceLocation customTexRes, final RenderPos renderPos, final WidgetGui gui, @Nullable final Consumer<TextureProperties> before, @Nullable final Consumer<TextureProperties> after) {
        ifPresent(t -> {
            Optional.ofNullable(before).ifPresent(bc -> bc.accept(t));
            t.render(customTexRes, renderPos, gui);
            Optional.ofNullable(after).ifPresent(ac -> ac.accept(t));
        });
    }

    public void renderIfPresent(@Nullable final ResourceLocation customTexRes, final RenderPos renderPos, final GuiScreen gui, @Nullable final Consumer<TextureProperties> before, @Nullable final Consumer<TextureProperties> after) {
        ifPresent(t -> {
            Optional.ofNullable(before).ifPresent(bc -> bc.accept(t));
            t.render(customTexRes, renderPos, gui);
            Optional.ofNullable(after).ifPresent(ac -> ac.accept(t));
        });
    }

    public void ifSizePresent(final Consumer<TextureProperties> runnable) {
        if (width > 0 && height > 0) {
            runnable.accept(this);
        }
    }

    // Custom texture and size render

    public void render(@Nullable final ResourceLocation customTexRes, final RenderPos renderPos, final RenderSize renderSize, final WidgetGui gui) {
        render(customTexRes, renderPos, renderSize, gui.getGui());
    }

    public void render(@Nullable final ResourceLocation customTexRes, final RenderPos renderPos, final RenderSize renderSize, final GuiScreen gui) {
        TextureManager textureManager = gui.mc.getTextureManager();
        if (texRes != null) {
            bind(textureManager);
        } else {
            if (customTexRes != null) {
                bind(textureManager, customTexRes);
            } else {
                return;
            }
        }
        gui.drawTexturedModalRect(renderPos.posX(), renderPos.posY(), texX, texY, renderSize.width(), renderSize.height());
    }

    // Custom size render

    public void render(final RenderPos renderPos, final RenderSize renderSize, final WidgetGui gui) {
        render(renderPos, renderSize, gui.getGui());
    }

    public void render(final RenderPos renderPos, final RenderSize renderSize, final GuiScreen gui) {
        bind(gui.mc.getTextureManager());
        gui.drawTexturedModalRect(renderPos.posX(), renderPos.posY(), texX, texY, renderSize.width(), renderSize.height());
    }

    public void renderIfPresent(final RenderPos renderPos, final RenderSize renderSize, final WidgetGui gui) {
        ifTexPresent(t -> t.render(renderPos, renderSize, gui));
    }

    public void renderIfPresent(final RenderPos renderPos, final RenderSize renderSize, final GuiScreen gui) {
        ifTexPresent(t -> t.render(renderPos, renderSize, gui));
    }

    public void renderIfPresent(final RenderPos renderPos, final RenderSize renderSize, final WidgetGui gui, final Consumer<TextureProperties> before) {
        ifTexPresent(t -> before.andThen(t1 -> t.render(renderPos, renderSize, gui)).accept(t));
    }

    public void renderIfPresent(final RenderPos renderPos, final RenderSize renderSize, final GuiScreen gui, final Consumer<TextureProperties> before) {
        ifTexPresent(t -> before.andThen(t1 -> t.render(renderPos, renderSize, gui)).accept(t));
    }

    public void renderIfPresent(final RenderPos renderPos, final RenderSize renderSize, final WidgetGui gui, @Nullable final Consumer<TextureProperties> before, @Nullable final Consumer<TextureProperties> after) {
        ifPresent(t -> {
            Optional.ofNullable(before).ifPresent(bc -> bc.accept(t));
            t.render(renderPos, renderSize, gui);
            Optional.ofNullable(after).ifPresent(ac -> ac.accept(t));
        });
    }

    public void renderIfPresent(final RenderPos renderPos, final RenderSize renderSize, final GuiScreen gui, @Nullable final Consumer<TextureProperties> before, @Nullable final Consumer<TextureProperties> after) {
        ifPresent(t -> {
            Optional.ofNullable(before).ifPresent(bc -> bc.accept(t));
            t.render(renderPos, renderSize, gui);
            Optional.ofNullable(after).ifPresent(ac -> ac.accept(t));
        });
    }

    public void ifTexPresent(final Consumer<TextureProperties> runnable) {
        if (texRes != null) {
            runnable.accept(this);
        }
    }

}
