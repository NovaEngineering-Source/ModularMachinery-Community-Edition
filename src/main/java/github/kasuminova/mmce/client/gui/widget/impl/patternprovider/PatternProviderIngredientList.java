package github.kasuminova.mmce.client.gui.widget.impl.patternprovider;

import github.kasuminova.mmce.client.gui.GuiMEPatternProvider;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import github.kasuminova.mmce.client.gui.widget.container.Row;
import github.kasuminova.mmce.client.gui.widget.impl.preview.IngredientList;
import github.kasuminova.mmce.client.gui.widget.slot.SlotFluidVirtual;
import github.kasuminova.mmce.client.gui.widget.slot.SlotItemVirtual;
import github.kasuminova.mmce.client.gui.widget.slot.SlotVirtual;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class PatternProviderIngredientList extends IngredientList {

    @Override
    public void initWidget(final WidgetGui gui) {
        super.initWidget(gui);
        scrollbar.setMargin(5, 1, 1, 1);
        scrollbar.setWidthHeight(9, 124);
        scrollbar.getScroll()
                .setMouseDownTextureXY(198, 232)
                .setHoveredTextureXY(187, 232)
                .setTextureXY(176, 232)
                .setUnavailableTextureXY(209, 232)
                .setTextureLocation(GuiMEPatternProvider.GUI_TEXTURE)
                .setWidthHeight(9, 18);
    }

    public PatternProviderIngredientList setStackList(final List<ItemStack> list, List<FluidStack> fluidList) {
        getWidgets().clear();

        Row row = new Row();
        int stackPerRow = 0;
        int totalSize = list.size() + fluidList.size();
        for (int i = 0; i < list.size(); i++) {
            final ItemStack stack = list.get(i);
            row.addWidget(initSlot(SlotItemVirtual.ofJEI(stack)));
            stackPerRow++;
            if (stackPerRow >= maxStackPerRow && i + 1 < totalSize) {
                addWidget(row.setUseScissor(false));
                row = new Row();
                stackPerRow = 0;
            }
        }
        for (int i = 0; i < fluidList.size(); i++) {
            final FluidStack stack = fluidList.get(i);
            row.addWidget(initSlot(SlotFluidVirtual.ofJEI(stack)));
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

    protected static SlotVirtual initSlot(final SlotVirtual slot) {
        slot.setSlotTexLocation(GuiMEPatternProvider.GUI_TEXTURE)
                .setSlotTexX(220).setSlotTexY(232);
        return slot;
    }

}
