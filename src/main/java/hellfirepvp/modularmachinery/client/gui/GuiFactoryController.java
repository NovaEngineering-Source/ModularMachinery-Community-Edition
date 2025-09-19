package hellfirepvp.modularmachinery.client.gui;

import github.kasuminova.mmce.common.event.client.ControllerGUIRenderEvent;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.client.gui.widget.GuiScrollbar;
import hellfirepvp.modularmachinery.common.container.ContainerFactoryController;
import hellfirepvp.modularmachinery.common.crafting.ActiveMachineRecipe;
import hellfirepvp.modularmachinery.common.crafting.helper.CraftingStatus;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.machine.factory.FactoryRecipeThread;
import hellfirepvp.modularmachinery.common.tiles.TileFactoryController;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import hellfirepvp.modularmachinery.common.util.MiscUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class GuiFactoryController extends GuiContainerBase<ContainerFactoryController> {
    public static final  double           FONT_SCALE                = 0.72;
    private static final ResourceLocation TEXTURES_FACTORY          = new ResourceLocation(ModularMachinery.MODID, "textures/gui/guifactory.png");
    private static final ResourceLocation TEXTURES_FACTORY_ELEMENTS = new ResourceLocation(ModularMachinery.MODID, "textures/gui/guifactoryelements.png");
    private static final int              SCROLLBAR_TOP             = 8;
    private static final int              SCROLLBAR_LEFT            = 94;
    private static final int              SCROLLBAR_HEIGHT          = 197;
    private static final int              MAX_PAGE_ELEMENTS         = 6;
    private static final int              FACTORY_ELEMENT_WIDTH     = 86;
    private static final int              FACTORY_ELEMENT_HEIGHT    = 32;
    private static final int              TEXT_DRAW_OFFSET_X        = 113;
    private static final int              TEXT_DRAW_OFFSET_Y        = 12;
    private static final int              RECIPE_QUEUE_OFFSET_X     = 8;
    private static final int              RECIPE_QUEUE_OFFSET_Y     = 8;

    private final GuiScrollbar          scrollbar = new GuiScrollbar();
    private final TileFactoryController factory;

    public GuiFactoryController(TileFactoryController factory, EntityPlayer player) {
        super(new ContainerFactoryController(factory, player));
        this.factory = factory;
        this.xSize = 280;
        this.ySize = 213;
    }

    private static int drawBlueprintInfo(int offsetX, int y, FontRenderer fr, DynamicMachine machine, boolean formed) {
        String drawnHead;
        int offsetY = y;

        if (machine != null) {
            drawnHead = I18n.format("gui.controller.blueprint", "");
            List<String> out = fr.listFormattedStringToWidth(machine.getLocalizedName(), MathHelper.floor(135 * (1 / GuiFactoryController.FONT_SCALE)));
            fr.drawStringWithShadow(drawnHead, offsetX, offsetY, 0xFFFFFF);
            for (String draw : out) {
                offsetY += 10;
                fr.drawStringWithShadow(draw, offsetX, offsetY, 0xFFFFFF);
            }
            offsetY += 15;
        } else if (!formed) {
            drawnHead = I18n.format("gui.controller.blueprint", I18n.format("gui.controller.blueprint.none"));
            fr.drawStringWithShadow(drawnHead, offsetX, offsetY, 0xFFFFFF);
            offsetY += 15;
        }

        return offsetY;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        drawRecipeQueue();
        drawFactoryStatus();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURES_FACTORY);
        final int x = (this.width - this.xSize) / 2;
        final int y = (this.height - this.ySize) / 2;
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, this.xSize, this.ySize, this.xSize, this.ySize);

        updateScrollbar(x, y);
        scrollbar.draw(this, mc);
    }

    private void drawRecipeQueue() {
        int offsetY = RECIPE_QUEUE_OFFSET_Y;
        int currentScroll = scrollbar.getCurrentScroll();

        Collection<FactoryRecipeThread> coreThreadList = factory.getCoreRecipeThreads().values();
        List<FactoryRecipeThread> threads = factory.getFactoryRecipeThreadList();
        List<FactoryRecipeThread> recipeThreadList = new ArrayList<>((int) ((coreThreadList.size() + threads.size()) * 1.5));
        recipeThreadList.addAll(coreThreadList);
        recipeThreadList.addAll(threads);

        for (int i = 0; i < Math.min(MAX_PAGE_ELEMENTS, recipeThreadList.size()); i++) {
            FactoryRecipeThread thread = recipeThreadList.get(i + currentScroll);
            drawRecipeInfo(thread, i + currentScroll, offsetY);
            offsetY += FACTORY_ELEMENT_HEIGHT + 1;
        }
    }

    private void drawRecipeInfo(FactoryRecipeThread thread, int id, int offsetY) {
        CraftingStatus status = thread.getStatus();
        ActiveMachineRecipe activeRecipe = thread.getActiveRecipe();

        this.mc.getTextureManager().bindTexture(TEXTURES_FACTORY_ELEMENTS);

        // Core Thread Color
        if (thread.isCoreThread()) {
            GlStateManager.color(0.7F, 0.9F, 1.0F, 1.0F);
        } else {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
        drawTexturedModalRect(RECIPE_QUEUE_OFFSET_X, offsetY, 0, 0, FACTORY_ELEMENT_WIDTH, FACTORY_ELEMENT_HEIGHT);

        // Thread Status Color
        if (status.isCrafting()) {
            GlStateManager.color(0.6F, 1.0F, 0.75F, 1.0F);
        } else {
            GlStateManager.color(1.0F, 0.6F, 0.6F, 1.0F);
        }

        if (activeRecipe != null) {
            float progress = (float) activeRecipe.getTick() / activeRecipe.getTotalTick();
            drawTexturedModalRect(RECIPE_QUEUE_OFFSET_X, offsetY, 0, 0, (int) (FACTORY_ELEMENT_WIDTH * progress), FACTORY_ELEMENT_HEIGHT);
        }

        // Text Status
        drawRecipeStatus(thread, id, offsetY + 2);
    }

    private void drawRecipeStatus(FactoryRecipeThread thread, int id, int y) {
        GlStateManager.pushMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.scale(FONT_SCALE, FONT_SCALE, FONT_SCALE);

        FontRenderer fr = this.fontRenderer;
        int offsetX = (int) (RECIPE_QUEUE_OFFSET_X / FONT_SCALE) + 2;
        int offsetY = (int) (y / FONT_SCALE);

        ActiveMachineRecipe activeRecipe = thread.getActiveRecipe();
        CraftingStatus status = thread.getStatus();
        int parallelism = activeRecipe == null ? 1 : activeRecipe.getParallelism();

        String threadName;
        if (thread.isCoreThread()) {
            String name = thread.getThreadName();
            threadName = I18n.hasKey(name) ? I18n.format(name) : name;
        } else {
            threadName = I18n.format("gui.factory.thread", id);
        }

        if (parallelism > 1) {
            // Example: Thread #0 (Parallelism: 9)
            fr.drawString(threadName +
                    " (" + I18n.format("gui.controller.parallelism", parallelism) + ')',
                offsetX, offsetY, 0x222222);
        } else {
            fr.drawString(threadName,
                offsetX, offsetY, 0x222222);
        }
        offsetY += 12;

        List<String> out = fr.listFormattedStringToWidth(I18n.format(status.getUnlocMessage()), (int) ((FACTORY_ELEMENT_WIDTH - 6) / FONT_SCALE));
        for (String draw : out) {
            fr.drawString(draw, offsetX, offsetY, 0x222222);
            offsetY += 10;
        }

        if (activeRecipe != null && activeRecipe.getTotalTick() > 0) {
            int progress = (activeRecipe.getTick() * 100) / activeRecipe.getTotalTick();
            fr.drawString(I18n.format("gui.controller.status.crafting.progress",
                    progress + "%"),
                offsetX, offsetY, 0x222222);
        }

        GlStateManager.popMatrix();
    }

    private void drawFactoryStatus() {
        GlStateManager.pushMatrix();
        GlStateManager.scale(FONT_SCALE, FONT_SCALE, FONT_SCALE);
        int offsetX = (int) (TEXT_DRAW_OFFSET_X / FONT_SCALE);
        int offsetY = TEXT_DRAW_OFFSET_Y;

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        FontRenderer fr = this.fontRenderer;

        int redstone = factory.getWorld().getStrongPower(factory.getPos());
        if (redstone > 0) {
            String drawnStop = I18n.format("gui.controller.status.redstone_stopped");
            List<String> out = fr.listFormattedStringToWidth(drawnStop, MathHelper.floor(135 * (1 / FONT_SCALE)));
            for (String draw : out) {
                fr.drawStringWithShadow(draw, offsetX, offsetY, 0xFFFFFF);
                offsetY += 10;
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
            return;
        }

        DynamicMachine machine = factory.getBlueprintMachine();
        offsetY = drawBlueprintInfo(offsetX, offsetY, fr, machine, factory.isStructureFormed());

        DynamicMachine found = factory.getFoundMachine();
        offsetY = drawStructureInfo(offsetX, offsetY, fr, found);

        if (!factory.isStructureFormed()) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
            return;
        }
        offsetY += 15;

        offsetY = drawFactoryRecipeSearchStatusInfo(offsetX, offsetY, fr);

        int tmp = offsetY;
        offsetY = drawFactoryThreadsInfo(offsetX, offsetY, fr);
        offsetY = drawParallelismInfo(offsetX, offsetY, fr);
        if (tmp != offsetY) {
            offsetY += 5;
        }

        int usedTimeCache = TileMultiblockMachineController.usedTimeCache;
        float searchUsedTimeCache = TileMultiblockMachineController.searchUsedTimeCache;
        String workMode = TileMultiblockMachineController.workModeCache.getDisplayName();
        fr.drawStringWithShadow(String.format("Avg: %sμs/t (Search: %sms), WorkMode: %s",
                usedTimeCache,
                MiscUtils.formatFloat(searchUsedTimeCache / 1000F, 2),
                workMode),
            offsetX, offsetY, 0xFFFFFF
        );

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    private int drawFactoryRecipeSearchStatusInfo(int offsetX, int y, FontRenderer fr) {
        if (!factory.hasIdleThread()) {
            return y;
        }
        int offsetY = y;

        String status = I18n.format("gui.controller.status");
        fr.drawStringWithShadow(status, offsetX, offsetY, 0xFFFFFF);
        String statusKey = factory.getControllerStatus().getUnlocMessage();

        List<String> out = fr.listFormattedStringToWidth(I18n.format(statusKey), MathHelper.floor(135 * (1 / FONT_SCALE)));
        for (String draw : out) {
            offsetY += 10;
            fr.drawStringWithShadow(draw, offsetX, offsetY, 0xFFFFFF);
        }

        return offsetY + 15;
    }

    private int drawFactoryThreadsInfo(int offsetX, int offsetY, FontRenderer fr) {
        if (factory.getMaxThreads() <= 0) {
            return offsetY;
        }
        fr.drawStringWithShadow(I18n.format("gui.factory.threads",
                factory.getFactoryRecipeThreadList().size(), factory.getMaxThreads()),
            offsetX, offsetY, 0xFFFFFF);
        return offsetY + 10;
    }

    private int drawParallelismInfo(int offsetX, int y, FontRenderer fr) {
        int offsetY = y;

        int parallelism = 1;
        int maxParallelism = factory.getTotalParallelism();
        if (maxParallelism <= 1) {
            return offsetY;
        }

        for (FactoryRecipeThread thread : factory.getFactoryRecipeThreadList()) {
            ActiveMachineRecipe activeRecipe = thread.getActiveRecipe();
            if (activeRecipe != null) {
                parallelism += (activeRecipe.getParallelism() - 1);
            }
        }
        for (final FactoryRecipeThread thread : factory.getCoreRecipeThreads().values()) {
            ActiveMachineRecipe activeRecipe = thread.getActiveRecipe();
            if (activeRecipe != null) {
                parallelism += (activeRecipe.getParallelism() - 1);
            }
        }

        if (parallelism <= 1) {
            return offsetY;
        }

        String parallelismStr = I18n.format("gui.controller.parallelism", parallelism);
        fr.drawStringWithShadow(parallelismStr, offsetX, offsetY, 0xFFFFFF);
        offsetY += 10;
        String maxParallelismStr = I18n.format("gui.controller.max_parallelism", maxParallelism);
        fr.drawStringWithShadow(maxParallelismStr, offsetX, offsetY, 0xFFFFFF);
        offsetY += 10;

        return offsetY;
    }

    private int drawStructureInfo(int offsetX, int y, FontRenderer fr, DynamicMachine found) {
        int offsetY = y;
        String drawnHead;

        if (found != null) {
            drawnHead = I18n.format("gui.controller.structure", "");
            List<String> out = fr.listFormattedStringToWidth(found.getLocalizedName(), MathHelper.floor(135 * (1 / GuiFactoryController.FONT_SCALE)));
            fr.drawStringWithShadow(drawnHead, offsetX, offsetY, 0xFFFFFF);
            for (String draw : out) {
                offsetY += 10;
                fr.drawStringWithShadow(draw, offsetX, offsetY, 0xFFFFFF);
            }

            offsetY = drawExtraInfo(offsetX, offsetY, fr);
        } else {
            drawnHead = I18n.format("gui.controller.structure", I18n.format("gui.controller.structure.none"));
            fr.drawStringWithShadow(drawnHead, offsetX, offsetY, 0xFFFFFF);
            offsetY += 15;
        }

        return offsetY;
    }

    private int drawExtraInfo(int offsetX, int y, FontRenderer fr) {
        int offsetY = y;
        ControllerGUIRenderEvent event = new ControllerGUIRenderEvent(factory);
        event.postEvent();

        String[] extraInfo = event.getExtraInfo();
        if (extraInfo.length != 0) {
            List<String> waitForDraw = new ArrayList<>();
            for (String s : extraInfo) {
                waitForDraw.addAll(fr.listFormattedStringToWidth(s, MathHelper.floor(135 * (1 / GuiFactoryController.FONT_SCALE))));
            }
            offsetY += 5;
            for (String s : waitForDraw) {
                offsetY += 10;
                fr.drawStringWithShadow(s, offsetX, offsetY, 0xFFFFFF);
            }
        }
        return offsetY;
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        final int i = Mouse.getEventDWheel();
        if (i != 0) {
            scrollbar.wheel(i);
        }
    }

    private void updateScrollbar(int displayX, int displayY) {
        scrollbar.setLeft(SCROLLBAR_LEFT + displayX).setTop(SCROLLBAR_TOP + displayY).setHeight(SCROLLBAR_HEIGHT);

        Map<String, FactoryRecipeThread> coreThreads = factory.getCoreRecipeThreads();
        List<FactoryRecipeThread> threadList = factory.getFactoryRecipeThreadList();
        scrollbar.setRange(0, Math.max(0, coreThreads.size() + threadList.size() - MAX_PAGE_ELEMENTS), 1);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        scrollbar.click(mouseX, mouseY);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        scrollbar.click(mouseX, mouseY);
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void setWidthHeight() {
    }
}
