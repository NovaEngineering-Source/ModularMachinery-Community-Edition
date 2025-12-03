package github.kasuminova.mmce.client.gui;

import appeng.client.gui.widgets.GuiCustomSlot;
import appeng.fluids.client.gui.widgets.GuiFluidTank;
import github.kasuminova.mmce.client.gui.slot.GuiFullCapFluidTank;
import github.kasuminova.mmce.client.gui.util.TextureProperties;
import github.kasuminova.mmce.client.gui.widget.Button;
import github.kasuminova.mmce.client.gui.widget.Button4State;
import github.kasuminova.mmce.client.gui.widget.ButtonElements;
import github.kasuminova.mmce.client.gui.widget.MultiLineLabel;
import github.kasuminova.mmce.client.gui.widget.base.WidgetController;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import github.kasuminova.mmce.client.gui.widget.container.Row;
import github.kasuminova.mmce.client.gui.widget.impl.patternprovider.PatternProviderIngredientList;
import github.kasuminova.mmce.common.container.ContainerMEPatternProvider;
import github.kasuminova.mmce.common.network.PktMEPatternProviderAction;
import github.kasuminova.mmce.common.tile.MEPatternProvider;
import github.kasuminova.mmce.common.util.InfItemFluidHandler;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.base.Mods;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GuiMEPatternProvider extends AEBaseGuiContainerDynamic {

    public static final ResourceLocation GUI_TEXTURE =
        new ResourceLocation(ModularMachinery.MODID, "textures/gui/mepatternprovider.png");

    protected final MEPatternProvider owner;

    protected final PatternProviderIngredientList stackList = new PatternProviderIngredientList();
    protected final ButtonElements<MEPatternProvider.WorkModeSetting> workModeSetting = new ButtonElements<>();

    public GuiMEPatternProvider(final MEPatternProvider owner, final EntityPlayer player) {
        super(new ContainerMEPatternProvider(owner, player));
        this.owner = owner;
        this.xSize = 256;
        this.ySize = 196;
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        this.widgetController = new WidgetController(WidgetGui.of(this, this.xSize, this.ySize, guiLeft, guiTop));

        // Titles...
        this.widgetController.addWidget(
            new MultiLineLabel(Collections.singletonList(I18n.format("gui.mepatternprovider.title")))
                .setAutoWrap(false)
                .setMargin(0)
                .setAbsXY(7, 11)
        );
        this.widgetController.addWidget(
            new MultiLineLabel(Collections.singletonList(I18n.format("gui.mepatternprovider.cached")))
                .setAutoWrap(false)
                .setMargin(0)
                .setAbsXY(180, 11)
        );
        this.widgetController.addWidget(
            new MultiLineLabel(Collections.singletonList(I18n.format("gui.mepatternprovider.inventory")))
                .setAutoWrap(false)
                .setMargin(0)
                .setAbsXY(7, 101)
        );
        this.widgetController.addWidget(
            new MultiLineLabel(Collections.singletonList(I18n.format("gui.mepatternprovider.single_inv")))
                .setAutoWrap(false)
                .setMargin(0)
                .setAbsXY(180, 158)
        );

        // Init StackList...
        stackList.setMaxStackPerRow(3);
        stackList.setWidthHeight(69, 126);
        stackList.setAbsXY(180, 27);

        // Init ReturnItems...
        Button4State returnItems = new Button4State();
        returnItems
            .setMouseDownTexture(176 + 18 + 18, 214)
            .setHoveredTexture(176 + 18, 214)
            .setTexture(176, 214)
            .setTextureLocation(GuiMEPatternProvider.GUI_TEXTURE)
            .setTooltipFunction((btn) -> {
                List<String> tooltips = new ArrayList<>();
                tooltips.add(I18n.format("gui.mepatternprovider.return_items"));
                tooltips.add(I18n.format("gui.mepatternprovider.return_items.desc"));
                return tooltips;
            })
            .setOnClickedListener((btn) -> ModularMachinery.NET_CHANNEL.sendToServer(new PktMEPatternProviderAction(PktMEPatternProviderAction.Action.RETURN_ITEMS)))
            .setWidthHeight(16, 16);

        // Init WorkModeSetting...
        workModeSetting
            // ButtonTexture 1
            .addElement(MEPatternProvider.WorkModeSetting.DEFAULT, TextureProperties.of(140, 196, 16, 16))
            // ButtonTexture 2
            .addElement(MEPatternProvider.WorkModeSetting.BLOCKING_MODE, TextureProperties.of(140 + 18, 196, 16, 16))
            // ButtonTexture 3
            .addElement(MEPatternProvider.WorkModeSetting.CRAFTING_LOCK_MODE, TextureProperties.of(140 + 18 + 18, 196, 16, 16))
            // ButtonTexture 0
            .addElement(MEPatternProvider.WorkModeSetting.ENHANCED_BLOCKING_MODE, TextureProperties.of(140 - 18, 196, 16, 16))
            .addElement(MEPatternProvider.WorkModeSetting.ISOLATION_INPUT, TextureProperties.of(140 - 18 - 18, 196, 16, 16))
            // ButtonTexture 5
            .setMouseDownTexture(140 + 18 + 18 + 18 + 18 + 18, 196)
            // ButtonTexture 5
            .setHoveredTexture(140 + 18 + 18 + 18 + 18, 196)
            // ButtonTexture 4
            .setTexture(140 + 18 + 18 + 18, 196)
            .setTextureLocation(GuiMEPatternProvider.GUI_TEXTURE)
            .setTooltipFunction((btn) -> {
                MEPatternProvider.WorkModeSetting current = workModeSetting.getCurrentSelection();
                List<String> tooltips = new ArrayList<>();
                tooltips.add(I18n.format("gui.mepatternprovider.work_mode.desc"));
                tooltips.add((current == MEPatternProvider.WorkModeSetting.DEFAULT ? I18n.format("gui.mepatternprovider.current") : "")
                    + I18n.format("gui.mepatternprovider.default.desc"));
                tooltips.add((current == MEPatternProvider.WorkModeSetting.BLOCKING_MODE ? I18n.format("gui.mepatternprovider.current") : "")
                    + I18n.format("gui.mepatternprovider.blocking_mode.desc"));
                tooltips.add((current == MEPatternProvider.WorkModeSetting.CRAFTING_LOCK_MODE ? I18n.format("gui.mepatternprovider.current") : "")
                    + I18n.format("gui.mepatternprovider.crafting_lock_mode.desc"));
                tooltips.add((current == MEPatternProvider.WorkModeSetting.ENHANCED_BLOCKING_MODE ? I18n.format("gui.mepatternprovider.current") : "")
                    + I18n.format("gui.mepatternprovider.enhanced_blocking_mode.desc"));
                tooltips.add((current == MEPatternProvider.WorkModeSetting.ISOLATION_INPUT ? I18n.format("gui.mepatternprovider.current") : "")
                    + I18n.format("gui.mepatternprovider.isolation_input.desc"));
                return tooltips;
            })
            .setOnClickedListener((btn) -> {
                MEPatternProvider.WorkModeSetting current = workModeSetting.getCurrentSelection();
                if (current == null) {
                    return;
                }
                switch (current) {
                    case DEFAULT ->
                        ModularMachinery.NET_CHANNEL.sendToServer(new PktMEPatternProviderAction(PktMEPatternProviderAction.Action.ENABLE_DEFAULT_MODE));
                    case BLOCKING_MODE ->
                        ModularMachinery.NET_CHANNEL.sendToServer(new PktMEPatternProviderAction(PktMEPatternProviderAction.Action.ENABLE_BLOCKING_MODE));
                    case CRAFTING_LOCK_MODE ->
                        ModularMachinery.NET_CHANNEL.sendToServer(new PktMEPatternProviderAction(PktMEPatternProviderAction.Action.ENABLE_CRAFTING_LOCK_MODE));
                    case ENHANCED_BLOCKING_MODE ->
                        ModularMachinery.NET_CHANNEL.sendToServer(new PktMEPatternProviderAction(PktMEPatternProviderAction.Action.ENABLE_ENHANCED_BLOCKING_MODE));
                    case ISOLATION_INPUT ->
                        ModularMachinery.NET_CHANNEL.sendToServer(new PktMEPatternProviderAction(PktMEPatternProviderAction.Action.ENABLE_ENHANCED_ISOLATION_INPUT));
                }
            })
            .setWidthHeight(16, 16);

        // Init Single Inv Tip Button...
        Button singleInvTip = new Button();
        singleInvTip
            .setTextureLocation(GuiMEPatternProvider.GUI_TEXTURE)
            .setTexture(230, 214)
            .setHoveredTexture(230 + 11, 214)
            .setTooltipFunction((btn) -> {
                List<String> tooltips = new ArrayList<>();
                tooltips.add(I18n.format("gui.mepatternprovider.single_inv.desc"));
                return tooltips;
            })
            .setWidthHeight(9, 11)
            .setAbsXY(240, 157);

        // Init Widget Containers...
        Row stackListButtons = new Row();
        stackListButtons.addWidgets(returnItems.setMarginRight(2), workModeSetting).setAbsXY(215, 7);

        this.widgetController.addWidget(stackList);
        this.widgetController.addWidget(stackListButtons);
        this.widgetController.addWidget(singleInvTip);

        // Update state...
        updateGUIState();
    }

    @Override
    public void initGui() {
        super.initGui();

        final GuiFullCapFluidTank guiTank = new GuiFullCapFluidTank(owner.getSubFluidHandler(),
            0, 0, 232, 172, 16, 16
        );

        // AE2 Unofficial Extended Life Check
        if (Mods.AE2EL.isPresent()) {
            this.guiSlots.add(guiTank);
        } else {
            // Default AE2
            ObfuscationReflectionHelper.setPrivateValue(GuiCustomSlot.class, guiTank, getGuiLeft(), "x");
            ObfuscationReflectionHelper.setPrivateValue(GuiCustomSlot.class, guiTank, getGuiTop(), "y");
            List<Object> buttonList = (List) this.buttonList;
            buttonList.add(guiTank);
        }
    }

    @Override
    protected void mouseClicked(int xCoord, int yCoord, int btn) throws IOException {
        for (GuiCustomSlot slot : this.guiSlots) {
            if (slot instanceof GuiFluidTank) {
                if (this.isPointInRegion(slot.xPos(), slot.yPos(), slot.getWidth(), slot.getHeight(), xCoord, yCoord) && slot.canClick(this.mc.player)) {
                    slot.slotClicked(this.mc.player.inventory.getItemStack(), btn);
                    return;
                }
            }
        }
        super.mouseClicked(xCoord, yCoord, btn);
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(GUI_TEXTURE);
        this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        super.drawBG(offsetX, offsetY, mouseX, mouseY);
    }

    public MEPatternProvider getOwner() {
        return owner;
    }

    public void updateGUIState() {
        InfItemFluidHandler infHandler = owner.getInfHandler();

        List<ItemStack> itemStacks = new ObjectArrayList<>(infHandler.getItemStackList());
        List<FluidStack> fluids = new ObjectArrayList<>(infHandler.getFluidStackList());
        List<Object> gass = new ObjectArrayList<>(infHandler.getGasStackList());

        for (var component : owner.getCombinationComponents()) {
            var h = (InfItemFluidHandler) component.getContainerProvider();
            itemStacks.addAll(h.getItemStackList());
            fluids.addAll(h.getFluidStackList());
            gass.addAll(h.getGasStackList());
        }

        stackList.setStackList(itemStacks, fluids, gass);
        workModeSetting.setCurrentSelection(owner.getWorkMode());
    }

    public void setStackList(final NBTTagCompound tagCompound) {
        owner.readProviderHandlerNBT(tagCompound, true);
        updateGUIState();
    }

}