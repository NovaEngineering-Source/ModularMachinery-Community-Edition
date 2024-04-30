package github.kasuminova.mmce.client.gui.widget.impl.preview;

import com.cleanroommc.client.shader.ShaderManager;
import github.kasuminova.mmce.client.gui.widget.HorizontalLine;
import github.kasuminova.mmce.client.gui.widget.MultiLineLabel;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import github.kasuminova.mmce.client.gui.widget.container.Column;
import hellfirepvp.modularmachinery.common.util.MiscUtils;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.I18n;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PreviewStatusBar extends Column {

    protected final WorldSceneRendererWidget renderer;

    protected final MultiLineLabel messageLabel = new MultiLineLabel(Collections.emptyList());
    protected final HorizontalLine progressLine = new HorizontalLine();

    protected int maxWidth = 0;

    protected boolean shaderPackLoaded = false;
    protected boolean vboUnsupported = false;
    protected boolean vboDisabled = false;

    public PreviewStatusBar(WorldSceneRendererWidget renderer) {
        this.renderer = renderer;
    }

    @Override
    public void initWidget(final WidgetGui gui) {
        super.initWidget(gui);
        messageLabel.setScale(.72f)
                .setMarginLeft(22);
        progressLine.setColor(0xFF87CEFA)
                .setHeight(2);
        addWidgets(progressLine, messageLabel);
    }

    @Override
    public void update(final WidgetGui gui) {
        super.update(gui);
        float progress = renderer.getWorldRenderer().getCompileProgress();

        shaderPackLoaded = ShaderManager.isOptifineShaderPackLoaded();
        vboUnsupported = !OpenGlHelper.vboSupported;
        vboDisabled = !OpenGlHelper.useVbo();

        if (progress > 0) {
            progressLine.setWidth((int) Math.floor(maxWidth * progress));
        } else {
            progressLine.setWidth(0);
        }

        List<String> contents = new ArrayList<>();
        if (progress > 0) {
            contents.add(I18n.format("gui.preview.compiling.progress",
                    MiscUtils.formatFloat(progress * 100f, 1)));
        }
        if (shaderPackLoaded) {
            contents.add(I18n.format("gui.preview.optifine_shader_pack_warn"));
        }
        if (vboUnsupported) {
            contents.add(I18n.format("gui.preview.vbo_unsupported_warn"));
        } else if (vboDisabled) {
            contents.add(I18n.format("gui.preview.vbo_disabled_warn"));
        }
        messageLabel.setContents(contents);
        setMaxWidth(maxWidth);
    }

    public PreviewStatusBar setMaxWidth(final int maxWidth) {
        this.maxWidth = maxWidth;
        this.messageLabel.setWidth(maxWidth - messageLabel.getMarginLeft() - messageLabel.getMarginRight());
        return this;
    }
}
