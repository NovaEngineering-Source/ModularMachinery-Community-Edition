package github.kasuminova.mmce.client.gui.widget.impl.preview;

import github.kasuminova.mmce.client.gui.widget.Scrollbar;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import github.kasuminova.mmce.client.gui.widget.container.Row;
import github.kasuminova.mmce.client.gui.widget.container.ScrollingColumn;
import github.kasuminova.mmce.client.gui.widget.slot.SlotFluidVirtual;
import github.kasuminova.mmce.client.gui.widget.slot.SlotItemVirtual;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

import static github.kasuminova.mmce.client.gui.widget.impl.preview.MachineStructurePreviewPanel.WIDGETS_TEX_LOCATION;

public class IngredientList extends ScrollingColumn {

    public static final int MAX_STACK_PER_ROW = 9;

    protected int maxStackPerRow = MAX_STACK_PER_ROW;

    public IngredientList() {
        setWidthHeight(174, 36);
    }

    @Override
    public void update(final WidgetGui gui) {
        super.update(gui);
        scrollbar.setScrollUnit(scrollbar.getRange() / 5);
    }

    @Override
    public void initWidget(final WidgetGui gui) {
        super.initWidget(gui);
        scrollbar.setMargin(3, 1, 1, 1);
        scrollbar.setWidthHeight(8, 34);
        scrollbar.getScroll()
                .setMouseDownTextureXY(204, 143)
                .setHoveredTextureXY(194, 143)
                .setTextureXY(184, 143)
                .setUnavailableTextureXY(214, 143)
                .setTextureLocation(WIDGETS_TEX_LOCATION)
                .setWidthHeight(8, 13);
    }

    public Scrollbar getScrollbar() {
        return scrollbar;
    }

    public IngredientList setStackList(final List<ItemStack> list, List<FluidStack> fluidList) {
        getWidgets().clear();

        Row row = new Row();
        int stackPerRow = 0;
        int totalSize = list.size() + fluidList.size();
        for (int i = 0; i < list.size(); i++) {
            final ItemStack stack = list.get(i);
            row.addWidget(SlotItemVirtual.ofJEI(stack)
                    .setSlotTexLocation(WIDGETS_TEX_LOCATION)
                    .setSlotTexX(184).setSlotTexY(194)
            );
            stackPerRow++;
            if (stackPerRow >= maxStackPerRow && i + 1 < totalSize) {
                addWidget(row.setUseScissor(false));
                row = new Row();
                stackPerRow = 0;
            }
        }
        for (int i = 0; i < fluidList.size(); i++) {
            final FluidStack stack = fluidList.get(i);
            row.addWidget(SlotFluidVirtual.ofJEI(stack)
                    .setSlotTexLocation(WIDGETS_TEX_LOCATION)
                    .setSlotTexX(184).setSlotTexY(194)
            );
            stackPerRow++;
            if (stackPerRow >= maxStackPerRow && i + 1 < totalSize) {
                addWidget(row.setUseScissor(false));
                row = new Row();
                stackPerRow = 0;
            }
        }
        addWidget(row.setUseScissor(false));
        return this;
    }

    public int getMaxStackPerRow() {
        return maxStackPerRow;
    }

    public void setMaxStackPerRow(final int maxStackPerRow) {
        this.maxStackPerRow = maxStackPerRow;
    }

}
