package github.kasuminova.mmce.client.preivew;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import github.kasuminova.mmce.client.gui.widget.event.WorldRendererCacheCleanEvent;
import github.kasuminova.mmce.client.gui.widget.impl.preview.MachineStructurePreviewPanel;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class PreviewPanels {
    protected static final Cache<DynamicMachine, MachineStructurePreviewPanel> PANEL_CACHE = CacheBuilder.newBuilder()
            .maximumSize(20)
            .expireAfterAccess(60, TimeUnit.SECONDS)
            .removalListener((RemovalListener<DynamicMachine, MachineStructurePreviewPanel>) notification -> {
                notification.getValue().onGuiEvent(new WorldRendererCacheCleanEvent(null));
            })
            .build();

    public static MachineStructurePreviewPanel getPanel(final DynamicMachine machine, final WidgetGui widgetGui) {
        try {
            return PANEL_CACHE.get(machine, () -> createPanel(machine, widgetGui));
        } catch (ExecutionException e) {
            return createPanel(machine, widgetGui);
        }
    }

    @Nonnull
    private static MachineStructurePreviewPanel createPanel(final DynamicMachine machine, final WidgetGui widgetGui) {
        MachineStructurePreviewPanel panel = new MachineStructurePreviewPanel(machine);
        panel.initWidget(widgetGui);
        return panel;
    }
}
