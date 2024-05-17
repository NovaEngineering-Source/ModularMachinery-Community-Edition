package github.kasuminova.mmce.client.gui.widget.impl.preview;

import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import github.kasuminova.mmce.client.gui.widget.slot.SlotItemVirtual;
import github.kasuminova.mmce.client.gui.widget.slot.SlotItemVirtualSelectable;
import github.kasuminova.mmce.client.util.UpgradeIngredient;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static github.kasuminova.mmce.client.gui.widget.impl.preview.MachineStructurePreviewPanel.WIDGETS_TEX_LOCATION_SECOND;

public class UpgradeIngredientList extends IngredientListVertical {

    protected static final Color OVERLAY_COLOR = new Color(0, 255, 127, 150);

    protected final WorldSceneRendererWidget renderer;
    protected final DynamicMachine machine;

    protected final Map<BlockPos, List<SlotItemVirtualSelectable>> posToUpgradeSlot = new HashMap<>();

    public UpgradeIngredientList(WorldSceneRendererWidget renderer, DynamicMachine machine) {
        setWidthHeight(25, 114);
        this.renderer = renderer;
        this.machine = machine;
        this.listBgTexLocation = WIDGETS_TEX_LOCATION_SECOND;
        this.listBgTexX = 0;
        this.listBgTexY = 0;
        this.listBgTexHeight = 114;
    }

    @Override
    public void initWidget(final WidgetGui gui) {
        super.initWidget(gui);
        scrollbar.setWidthHeight(6, 112);
        List<UpgradeIngredient> ingredients = new ArrayList<>();
        machine.getModifiers().values().stream()
                .flatMap(Collection::stream)
                .map(replacement -> UpgradeIngredient.of(replacement.getDescriptiveStack(), replacement))
                .forEach(ingredients::add);
        machine.getMultiBlockModifiers().stream()
                .map(replacement -> UpgradeIngredient.of(replacement.getDescriptiveStack(), replacement))
                .forEach(ingredients::add);
        setUpgradeStackList(ingredients);
    }

    public UpgradeIngredientList setUpgradeStackList(final List<UpgradeIngredient> ingredients) {
        getWidgets().clear();
        for (UpgradeIngredient upgradeIngredient : ingredients) {
            SlotItemVirtualSelectable slot = SlotItemVirtual.ofSelectable(upgradeIngredient.descStack());
            slot.setOnClickedListener(__ -> onSlotSelectedStateChanged(slot, upgradeIngredient));
            slot.setTooltipFunction(stack -> {
                List<String> tips = new ArrayList<>(upgradeIngredient.replacement().getDescriptionLines());
                if (slot.isClicked()) {
                    tips.add(I18n.format("gui.preview.button.toggle_upgrade_display.item.disable_overlay.tip"));
                } else {
                    tips.add(I18n.format("gui.preview.button.toggle_upgrade_display.item.enable_overlay.tip"));
                }
                return tips;
            });
            for (final BlockPos pos : upgradeIngredient.replacementPattern().keySet()) {
                posToUpgradeSlot.computeIfAbsent(pos, list -> new LinkedList<>()).add(slot);
            }
            addWidget(slot);
        }
        return this;
    }

    @Override
    public IngredientListVertical setStackList(final List<ItemStack> list, final List<FluidStack> fluidList) {
        throw new UnsupportedOperationException(
                "UpgradeIngredientList does not support setStackList(List<ItemStack> list), please use setUpgradeStackList(List<UpgradeIngredient> ingredients)"
        );
    }

    public void onSlotSelectedStateChanged(final SlotItemVirtualSelectable slot, final UpgradeIngredient ingredient) {
        if (slot.isClicked()) {
            renderer.addBlockOverlays(ingredient.replacementPattern().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> OVERLAY_COLOR, (a, b) -> b)));
            return;
        }

        posSet:
        for (final BlockPos pos : ingredient.replacementPattern().keySet()) {
            for (final SlotItemVirtualSelectable anotherSlot : posToUpgradeSlot.get(pos)) {
                // If another slot in the same coordinates is clicked, no rendering is removed from that position.
                if (anotherSlot.isClicked()) {
                    continue posSet;
                }
            }
            renderer.removeBlockOverlay(pos);
        }
    }

}
