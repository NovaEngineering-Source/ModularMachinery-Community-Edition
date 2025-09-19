package github.kasuminova.mmce.client.gui.widget.impl.preview;

import github.kasuminova.mmce.client.gui.widget.MultiLineLabel;
import github.kasuminova.mmce.client.gui.widget.container.Row;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;

import java.util.Collections;

public class StructurePreviewTitle extends Row {

    protected TitleStyle titleStyle = TitleStyle.PREFIX_AND_TITLE;

    public StructurePreviewTitle(final DynamicMachine machine) {
        MultiLineLabel prefix = new MultiLineLabel(Collections.singletonList(machine.getPrefix()));
        MultiLineLabel title = new MultiLineLabel(Collections.singletonList(machine.getLocalizedName()));
        prefix.setCenterAligned(true).setVerticalCentering(true)
              .setWidthHeight(36, 18).setMargin(0).setMarginDown(1).setMarginRight(2);
        title.setCenterAligned(true).setVerticalCentering(true)
             .setWidthHeight(136, 18).setMargin(0).setMarginDown(1);
        addWidgets(prefix, title);
    }

    public TitleStyle getTitleStyle() {
        return titleStyle;
    }

    public StructurePreviewTitle setTitleStyle(final TitleStyle titleStyle) {
        this.titleStyle = titleStyle;
        return this;
    }

    public enum TitleStyle {
        TITLE_ONLY,
        ICON_ITEMSTACK_AND_TITLE,
        PREFIX_AND_TITLE,
    }

}
