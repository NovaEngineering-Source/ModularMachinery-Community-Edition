package github.kasuminova.mmce.client.gui.widget.impl.preview;

import github.kasuminova.mmce.client.gui.widget.Button;
import github.kasuminova.mmce.client.gui.widget.Button4State;
import github.kasuminova.mmce.client.gui.widget.Button5State;
import github.kasuminova.mmce.client.gui.widget.container.Row;
import github.kasuminova.mmce.client.gui.widget.slot.SlotItemVirtual;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.client.ClientProxy;
import hellfirepvp.modularmachinery.client.util.DynamicMachineRenderContext;
import hellfirepvp.modularmachinery.common.data.Config;
import hellfirepvp.modularmachinery.common.item.ItemBlockController;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import hellfirepvp.modularmachinery.common.util.BlockArray;
import hellfirepvp.modularmachinery.common.util.ItemUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * All of our preview content is rendered here.
 */
public class MachineStructurePreviewPanel extends Row {
    public static final ResourceLocation WIDGETS_TEX_LOCATION = new ResourceLocation(ModularMachinery.MODID,
            "textures/gui/guiblueprint_new.png");
    public static final ResourceLocation WIDGETS_TEX_LOCATION_SECOND = new ResourceLocation(ModularMachinery.MODID,
            "textures/gui/guiblueprint_new_second.png");

    public static final int PANEL_WIDTH = 184;
    public static final int PANEL_HEIGHT = 220;

    public static final int WORLD_RENDERER_WIDTH = 172;
    public static final int WORLD_RENDERER_HEIGHT = 150;

    protected final WorldSceneRendererWidget renderer;

    public MachineStructurePreviewPanel(final DynamicMachine machine) {
        renderer = (WorldSceneRendererWidget) new WorldSceneRendererWidget(machine)
                .setWidthHeight(WORLD_RENDERER_WIDTH, WORLD_RENDERER_HEIGHT)
                .setAbsXY(6, 26);

        // ====================
        // Widgets...
        // ====================

        // Title, at panel top...
        StructurePreviewTitle title = new StructurePreviewTitle(machine);

        // Preview Status Bar, at renderer top...
        PreviewStatusBar previewStatusBar = new PreviewStatusBar(renderer);

        // Buttons, at preview bottom...
        Button5State menuBtn = new Button5State();
        Button5State toggleLayerRender = new Button5State();
        Button4State placeWorldPreview = new Button4State();
        Button5State enableCycleReplaceableBlocks = new Button5State();
        Button4State dynamicPatternPlus = new Button4State();
        Button4State dynamicPatternSubtract = new Button4State();

        // Buttons, at preview top right...
        Button machineExtraInfo = new Button();
        Button5State toggleFormed = new Button5State();
        Button5State showUpgrades = new Button5State();
        Button4State resetCenter = new Button4State();

        // Ingredient list, at panel bottom...
        IngredientList ingredientList = new IngredientList();

        // Selected block ingredient main stack, at left...
        SlotItemVirtual selectedBlockIngredientMain = SlotItemVirtual.ofJEI();
        // Selected block ingredient list, at left, under the selectedBlockIngredientMain...
        IngredientListVertical selectedBlockIngredientList = new IngredientListVertical();

        // 2D layer scrollbar, and 2 buttons, only shown when 2D preview enabled, at right...
        LayerRenderScrollbar layerScrollbar = new LayerRenderScrollbar(renderer);

        // Upgrades ingredient list, at right...
        UpgradeIngredientList upgradeIngredientList = new UpgradeIngredientList(renderer, machine);

        // ====================
        // Initialize texture, size, positions, tooltips...
        // ====================
        title.setAbsXY(5, 5);

        previewStatusBar.setMaxWidth(172)
                .setAbsXY(6, 26);

        menuBtn.setClickedTextureXY(184 + 15 + 15, 15)
                .setMouseDownTextureXY(184 + 15 + 15, 15)
                .setHoveredTextureXY(184 + 15, 15)
                .setTextureXY(184, 15)
                .setTextureLocation(WIDGETS_TEX_LOCATION)
                .setTooltipFunction(btn -> Collections.singletonList(I18n.format(
                        "gui.preview.button.menu.tip")))
                .setWidthHeight(13, 13);
        toggleLayerRender.setClickedTextureXY(184 + 15 + 15 + 15, 30)
                .setMouseDownTextureXY(184 + 15 + 15, 30)
                .setHoveredTextureXY(184 + 15, 30)
                .setTextureXY(184, 30)
                .setTextureLocation(WIDGETS_TEX_LOCATION)
                .setTooltipFunction(btn -> toggleLayerRender.isClicked()
                        ? Collections.singletonList(I18n.format("gui.preview.button.toggle_3d_render.tip"))
                        : Collections.singletonList(I18n.format("gui.preview.button.toggle_layer_render.tip")))
                .setWidthHeight(13, 13);
        placeWorldPreview
                .setMouseDownTextureXY(184 + 15 + 15, 45)
                .setHoveredTextureXY(184 + 15, 45)
                .setTextureXY(184, 45)
                .setTextureLocation(WIDGETS_TEX_LOCATION)
                .setTooltipFunction(btn -> Collections.singletonList(I18n.format(
                        "gui.preview.button.place_world_preview.tip")))
                .setWidthHeight(13, 13);
        enableCycleReplaceableBlocks.setClickedTextureXY(184 + 15 + 15, 105)
                .setMouseDownTextureXY(184 + 15 + 15, 105)
                .setHoveredTextureXY(184 + 15, 105)
                .setTextureXY(184,  105)
                .setTextureLocation(WIDGETS_TEX_LOCATION)
                .setTooltipFunction(btn -> enableCycleReplaceableBlocks.isClicked()
                        ? Collections.singletonList(I18n.format("gui.preview.button.disable_cycle_replaceable_blocks.tip"))
                        : Arrays.asList(
                                I18n.format("gui.preview.button.enable_cycle_replaceable_blocks.tip.0"),
                                I18n.format("gui.preview.button.enable_cycle_replaceable_blocks.tip.1")))
                .setWidthHeight(13, 13);
        dynamicPatternPlus
                .setMouseDownTextureXY(184 + 15 + 15, 60)
                .setHoveredTextureXY(184 + 15, 60)
                .setTextureXY(184, 60)
                .setTextureLocation(WIDGETS_TEX_LOCATION)
                .setTooltipFunction(btn -> Collections.singletonList(I18n.format(
                        "gui.preview.button.dynamic_pattern_plus.tip", renderer.getDynamicPatternSize())))
                .setWidthHeight(13, 13);
        dynamicPatternSubtract
                .setMouseDownTextureXY(184 + 15 + 15, 75)
                .setHoveredTextureXY(184 + 15, 75)
                .setTextureXY(184, 75)
                .setTextureLocation(WIDGETS_TEX_LOCATION)
                .setTooltipFunction(btn -> Collections.singletonList(I18n.format(
                        "gui.preview.button.dynamic_pattern_subtract.tip", renderer.getDynamicPatternSize())))
                .setWidthHeight(13, 13);

        machineExtraInfo.setHoveredTextureXY(184 + 15, 214)
                .setTextureXY(184, 214)
                .setTextureLocation(WIDGETS_TEX_LOCATION)
                .setTooltipFunction(btn -> getMachineExtraInfo(machine))
                .setWidthHeight(13, 13);
        toggleFormed.setClickedTextureXY(184 + 15 + 15 + 15, 0)
                .setMouseDownTextureXY(184 + 15 + 15, 0)
                .setHoveredTextureXY(184 + 15, 0)
                .setTextureXY(184, 0)
                .setTextureLocation(WIDGETS_TEX_LOCATION)
                .setTooltipFunction(btn -> toggleFormed.isClicked()
                        ? Collections.singletonList(I18n.format("gui.preview.button.toggle_unformed.tip"))
                        : Collections.singletonList(I18n.format("gui.preview.button.toggle_formed.tip")))
                .setWidthHeight(13, 13);
        showUpgrades.setClickedTextureXY(184 + 15 + 15, 90)
                .setMouseDownTextureXY(184 + 15 + 15, 90)
                .setHoveredTextureXY(184 + 15, 90)
                .setTextureXY(184, 90)
                .setTextureLocation(WIDGETS_TEX_LOCATION)
                .setTooltipFunction(btn -> showUpgrades.isClicked()
                        ? Collections.singletonList(I18n.format("gui.preview.button.toggle_upgrade_display.disable.tip"))
                        : Collections.singletonList(I18n.format("gui.preview.button.toggle_upgrade_display.enable.tip")))
                .setWidthHeight(13, 13);
        resetCenter.setMouseDownTextureXY(184 + 15 + 15, 229)
                .setHoveredTextureXY(184 + 15, 229)
                .setTextureXY(184, 229)
                .setTextureLocation(WIDGETS_TEX_LOCATION)
                .setTooltipFunction(btn -> Collections.singletonList(I18n.format("gui.preview.button.reset_center.tip")))
                .setWidthHeight(13, 13);

        ingredientList.setAbsXY(5, 179);

        selectedBlockIngredientMain.setSlotTexX(229).setSlotTexY(105)
                .setSlotTexLocation(WIDGETS_TEX_LOCATION)
                .setAbsXY(8, 28);
        selectedBlockIngredientList.setAbsXY(8, 48);

        // ====================
        // Assembly...
        // ====================

        // Bottom Menu
        Row bottomMenu = new Row();
        if (!machine.getDynamicPatterns().isEmpty()) {
            bottomMenu.addWidgets(
                    dynamicPatternPlus.setMarginRight(2).setDisabled(true),
                    dynamicPatternSubtract.setMarginRight(2).setDisabled(true)
            );
        }
        bottomMenu.addWidgets(
                placeWorldPreview.setMarginRight(2).setDisabled(true),
                enableCycleReplaceableBlocks.setClicked(true).setMarginRight(2).setDisabled(true),
                toggleLayerRender.setMarginRight(2).setDisabled(true),
                menuBtn.setMarginRight(2)
        );
        bottomMenu.setAbsXY(PANEL_WIDTH - (bottomMenu.getWidth() + 6), 161);

        // Right Top Menu
        Row rightTopMenu = new Row();
        boolean hasModifier = !machine.getModifiers().isEmpty() || !machine.getMultiBlockModifiers().isEmpty();
        if (hasModifier) {
            rightTopMenu.addWidgets(showUpgrades.setClicked(true).setMarginRight(2));
        }
        rightTopMenu.addWidgets(resetCenter.setMarginRight(2), toggleFormed.setMarginRight(2), machineExtraInfo.setMarginRight(2));
        rightTopMenu.setAbsXY(PANEL_WIDTH - (rightTopMenu.getWidth() + 6), 28);

        // Right menu
        Row rightMenu = (Row) new Row().addWidgets(
                upgradeIngredientList.setEnabled(hasModifier).setMarginRight(2),
                layerScrollbar.setDisabled(true).setMarginRight(2)
        );
        rightMenu.setAbsXY(PANEL_WIDTH - (rightMenu.getWidth() + (hasModifier ? 0 : 6)), 44);

        // Add all widgets to preview panel...
        addWidgets(
                title,
                previewStatusBar,
                ingredientList,
                selectedBlockIngredientMain.setDisabled(true), selectedBlockIngredientList.setDisabled(true),
                rightTopMenu, rightMenu, bottomMenu
        );
        addWidget(renderer);

        // ====================
        // EventHandlers...
        // ====================

        menuBtn.setOnClickedListener(btn -> handleMenuButton(menuBtn, bottomMenu));
        toggleLayerRender.setOnClickedListener(btn -> handleToggleLayerRenderButton(toggleLayerRender, layerScrollbar, rightMenu));
        placeWorldPreview.setOnClickedListener(btn -> handlePlaceWorldPreviewButton(machine));
        enableCycleReplaceableBlocks.setOnClickedListener(btn -> handleCycleReplaceableBlocksButton(enableCycleReplaceableBlocks));
        if (!machine.getDynamicPatterns().isEmpty()) {
            dynamicPatternPlus.setOnClickedListener(btn -> handleDynamicPatternPlusButton());
            dynamicPatternSubtract.setOnClickedListener(btn -> handleDynamicPatternSubtractButton());
        }

        resetCenter.setOnClickedListener(btn -> handleResetCenterButton());
        toggleFormed.setOnClickedListener(btn -> handleToggleFormedButton(toggleFormed));
        if (hasModifier) {
            showUpgrades.setOnClickedListener(btn -> handleShowUpgradesButton(upgradeIngredientList, showUpgrades, rightMenu));
        }

        layerScrollbar.setOnScrollChanged(this::handleLayerScrollbarChanged);
        renderer.setOnPatternUpdate(r -> handleRendererPatternUpdate(machine, r, ingredientList));
        renderer.setOnBlockSelected(relativePos -> handlePatternBlockSelected(relativePos, selectedBlockIngredientMain, selectedBlockIngredientList));
    }

    // Machine extra info.

    @Nonnull
    protected List<String> getMachineExtraInfo(final DynamicMachine machine) {
        BlockPos min = renderer.getPattern().getMin();
        BlockPos max = renderer.getPattern().getMax();
        List<String> tips = new ArrayList<>();
        tips.add(I18n.format("gui.preview.button.machine_info"));
        tips.add(I18n.format("gui.preview.button.machine_info.xyz.0"));
        tips.add(I18n.format("gui.preview.button.machine_info.xyz.1",
                max.getX() - min.getX() + 1,
                max.getY() - min.getY() + 1,
                max.getZ() - min.getZ() + 1
        ));
        tips.add(I18n.format("gui.preview.button.machine_info.controller_y_pos",
                Math.abs(machine.getPattern().getMin().getY())
        ));
        if (machine.getInternalParallelism() > 0) {
            tips.add(I18n.format("gui.preview.button.machine_info.internal_parallelism",
                    machine.getInternalParallelism()
            ));
        }
        if (machine.getMaxParallelism() != Config.maxMachineParallelism) {
            tips.add(I18n.format("gui.preview.button.machine_info.max_parallelism",
                    machine.getMaxParallelism()
            ));
        }
        if (machine.isHasFactory()) {
            tips.add(I18n.format("gui.preview.button.machine_info.max_threads",
                    machine.getMaxThreads()
            ));
        }
        if (!machine.getCoreThreadPreset().isEmpty()) {
            tips.add(I18n.format("gui.preview.button.machine_info.core_threads",
                    machine.getCoreThreadPreset().size()
            ));
        }
        if (!machine.getDynamicPatterns().isEmpty()) {
            tips.add(I18n.format("gui.preview.button.machine_info.dynamic_pattern"));
        }
        if (machine.isRequiresBlueprint()) {
            tips.add(I18n.format("gui.preview.button.machine_info.requires_blueprint"));
        }
        return tips;
    }

    // Handler methods.

    protected void handlePatternBlockSelected(final BlockPos relativePos, final SlotItemVirtual selectedBlockIngredientMain, final IngredientListVertical selectedBlockIngredientList) {
        BlockPos pos = relativePos == null ? null : relativePos.subtract(renderer.getRenderOffset());
        if (pos == null) {
            selectedBlockIngredientMain.setStackInSlot(ItemStack.EMPTY);
            selectedBlockIngredientMain.setDisabled(true);
            selectedBlockIngredientList.setStackList(Collections.emptyList(), Collections.emptyList());
            selectedBlockIngredientList.setDisabled(true);
            return;
        }

        World world = renderer.getWorldRenderer().getWorld();
        IBlockState clickedBlock = world.getBlockState(relativePos);
        BlockArray.BlockInformation clicked = renderer.getPattern().getPattern().get(pos);
        ItemStack clickedBlockStack = clickedBlock.getBlock().getPickBlock(
                clickedBlock, renderer.getWorldRenderer().getLastTraceResult(),
                world, pos,
                Minecraft.getMinecraft().player);
        List<ItemStack> replaceable = clicked.getIngredientList(pos, world).stream()
                .filter(replaceableStack -> !ItemUtils.matchStacks(clickedBlockStack, replaceableStack))
                .collect(Collectors.toList());
        selectedBlockIngredientMain.setStackInSlot(clickedBlockStack);
        selectedBlockIngredientMain.setEnabled(true);
        selectedBlockIngredientList.setStackList(replaceable, Collections.emptyList());
        selectedBlockIngredientList.setEnabled(!replaceable.isEmpty());
    }

    protected void handleRendererPatternUpdate(final DynamicMachine machine, final WorldSceneRendererWidget r, final IngredientList ingredientList) {
        List<ItemStack> stackList = r.getPattern().getDescriptiveStackList(r.getTickSnap(), r.getWorldRenderer().getWorld(), r.getRenderOffset());
        ingredientList.setStackList(stackList.stream()
                .sorted((left, right) -> {
                    if (left.getItem() instanceof ItemBlockController) {
                        return -1;
                    }
                    if (right.getItem() instanceof ItemBlockController) {
                        return 1;
                    }
                    return Integer.compare(right.getCount(), left.getCount());
                })
                .collect(Collectors.toList()), Collections.emptyList());
        if (r.isStructureFormed()) {
            TileEntity ctrlTE = r.getWorldRenderer().getWorld().getTileEntity(BlockPos.ORIGIN.add(r.getRenderOffset()));
            if (ctrlTE instanceof TileMultiblockMachineController controller) {
                controller.setFoundMachine(machine);
            }
        }
    }

    protected void handleLayerScrollbarChanged(final Integer renderLayer) {
        int minY = renderer.getPattern().getMin().getY();
        int maxY = renderer.getPattern().getMax().getY();
        renderer.setRenderLayer((maxY - renderLayer) + minY);
    }

    protected void handleResetCenterButton() {
        renderer.resetCenter();
    }

    protected void handleToggleFormedButton(final Button5State toggleFormed) {
        renderer.setStructureFormed(toggleFormed.isClicked());
    }

    protected static void handleShowUpgradesButton(final UpgradeIngredientList upgradeIngredientList, final Button5State showUpgrades, final Row rightMenu) {
        upgradeIngredientList.setEnabled(showUpgrades.isClicked());
        rightMenu.setAbsXY(PANEL_WIDTH - (rightMenu.getWidth() + 6), 44);
    }

    protected void handleDynamicPatternSubtractButton() {
        renderer.setDynamicPatternSize(Math.max(renderer.getDynamicPatternSize() - 1, 0));
    }

    protected void handleDynamicPatternPlusButton() {
        renderer.setDynamicPatternSize(renderer.getDynamicPatternSize() + 1);
    }

    protected void handleCycleReplaceableBlocksButton(final Button5State enableCycleReplaceableBlocks) {
        renderer.setCycleBlocks(enableCycleReplaceableBlocks.isClicked());
    }

    protected void handlePlaceWorldPreviewButton(final DynamicMachine machine) {
        DynamicMachineRenderContext renderContext = DynamicMachineRenderContext.createContext(machine, renderer.getDynamicPatternSize());
        renderContext.setShiftSnap(renderer.getTickSnap());
        ClientProxy.renderHelper.startPreview(renderContext);
        Minecraft.getMinecraft().displayGuiScreen(null);
    }

    protected void handleToggleLayerRenderButton(final Button5State toggleLayerRender, final LayerRenderScrollbar layerScrollbar, final Row rightMenu) {
        boolean layerRender = toggleLayerRender.isClicked();
        if (layerRender) {
            renderer.useLayerRender();
            int minY = renderer.getPattern().getMin().getY();
            int maxY = renderer.getPattern().getMax().getY();
            layerScrollbar.setEnabled(true);
            layerScrollbar.getScrollbar().setRange(minY, maxY);
            renderer.setRenderLayer(minY);
        } else {
            renderer.use3DRender();
            layerScrollbar.setDisabled(true);
        }
        rightMenu.setAbsXY(PANEL_WIDTH - (rightMenu.getWidth() + 6), 44);
    }

    protected static void handleMenuButton(final Button5State menuBtn, final Row bottomMenu) {
        boolean enable = menuBtn.isClicked();
        bottomMenu.getWidgets().stream()
                .filter(widget -> widget != menuBtn)
                .forEach(widget -> widget.setEnabled(enable));
        bottomMenu.setAbsXY(PANEL_WIDTH - (bottomMenu.getWidth() + 6), 161);
    }
}
