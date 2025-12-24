package github.kasuminova.mmce.client.gui;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.MathExpressionParser;
import appeng.client.gui.widgets.GuiTabButton;
import github.kasuminova.mmce.common.container.ContainerMEItemOutputBusStackSize;
import github.kasuminova.mmce.common.network.PktMEOutputBusStackSizeChange;
import github.kasuminova.mmce.common.network.PktSwitchGuiMEOutputBus;
import github.kasuminova.mmce.common.tile.MEItemOutputBus;
import hellfirepvp.modularmachinery.ModularMachinery;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import java.io.IOException;

public class GuiMEItemOutputBusStackSize extends AEBaseGui {

    private static final ResourceLocation TEXTURES = new ResourceLocation("modularmachinery", "textures/gui/stacksize.png");

    private GuiTextField stackSizeBox;
    private GuiTabButton originalGuiBtn;
    private final MEItemOutputBus outputBus;

    private GuiButton plus1;
    private GuiButton plus10;
    private GuiButton plus100;
    private GuiButton plus1000;
    private GuiButton minus1;
    private GuiButton minus10;
    private GuiButton minus100;
    private GuiButton minus1000;

    public GuiMEItemOutputBusStackSize(final InventoryPlayer inventoryPlayer, final MEItemOutputBus outputBus) {
        super(new ContainerMEItemOutputBusStackSize(inventoryPlayer, outputBus));
        this.outputBus = outputBus;
        this.ySize = 105;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        super.initGui();

        this.buttonList.add(this.plus1 = new GuiButton(0, this.guiLeft + 20, this.guiTop + 32, 22, 20, "+1"));
        this.buttonList.add(this.plus10 = new GuiButton(0, this.guiLeft + 48, this.guiTop + 32, 28, 20, "+10"));
        this.buttonList.add(this.plus100 = new GuiButton(0, this.guiLeft + 82, this.guiTop + 32, 32, 20, "+100"));
        this.buttonList.add(this.plus1000 = new GuiButton(0, this.guiLeft + 120, this.guiTop + 32, 38, 20, "+1000"));

        this.buttonList.add(this.minus1 = new GuiButton(0, this.guiLeft + 20, this.guiTop + 69, 22, 20, "-1"));
        this.buttonList.add(this.minus10 = new GuiButton(0, this.guiLeft + 48, this.guiTop + 69, 28, 20, "-10"));
        this.buttonList.add(this.minus100 = new GuiButton(0, this.guiLeft + 82, this.guiTop + 69, 32, 20, "-100"));
        this.buttonList.add(this.minus1000 = new GuiButton(0, this.guiLeft + 120, this.guiTop + 69, 38, 20, "-1000"));

        final ContainerMEItemOutputBusStackSize container = (ContainerMEItemOutputBusStackSize) this.inventorySlots;
        final ItemStack busIcon = new ItemStack(this.outputBus.getBlockType());

        if (!busIcon.isEmpty()) {
            this.buttonList.add(this.originalGuiBtn = new GuiTabButton(
                    this.guiLeft + 154,
                    this.guiTop,
                    busIcon,
                    busIcon.getDisplayName(),
                    this.itemRender
            ));
        }

        this.stackSizeBox = new GuiTextField(
                0,
                this.fontRenderer,
                this.guiLeft + 62,
                this.guiTop + 57,
                75,
                this.fontRenderer.FONT_HEIGHT
        );
        this.stackSizeBox.setEnableBackgroundDrawing(false);
        this.stackSizeBox.setMaxStringLength(32);
        this.stackSizeBox.setTextColor(0xFFFFFF);
        this.stackSizeBox.setVisible(true);
        this.stackSizeBox.setFocused(true);

        this.stackSizeBox.setText(String.valueOf(container.getStackSize()));
    }

    @Override
    protected void actionPerformed(@NotNull final GuiButton btn) throws IOException {
        super.actionPerformed(btn);

        if (btn == this.originalGuiBtn) {
            String text = this.stackSizeBox.getText();
            if (!text.isEmpty()) {
                try {
                    long value = parseValue(text);
                    if (value > Integer.MAX_VALUE) {
                        value = Integer.MAX_VALUE;
                    } else if (value < 1) {
                        value = 1;
                    }
                    this.sendStackSizeToServer((int) value);
                } catch (NumberFormatException ignored) {
                }
            }

            ModularMachinery.NET_CHANNEL.sendToServer(
                    new PktSwitchGuiMEOutputBus(this.outputBus.getPos(), 0)
            );
        }

        if (btn == this.plus1) this.addQty(1);
        else if (btn == this.plus10) this.addQty(10);
        else if (btn == this.plus100) this.addQty(100);
        else if (btn == this.plus1000) this.addQty(1000);
        else if (btn == this.minus1) this.addQty(-1);
        else if (btn == this.minus10) this.addQty(-10);
        else if (btn == this.minus100) this.addQty(-100);
        else if (btn == this.minus1000) this.addQty(-1000);
    }

    private void addQty(final int amount) {
        try {
            String text = this.stackSizeBox.getText();

            long currentValue = text.isEmpty() ? 0 : parseValue(text);
            long newValue = currentValue + amount;

            if (newValue > Integer.MAX_VALUE) {
                newValue = Integer.MAX_VALUE;
            } else if (newValue < 1) {
                newValue = 1;
            }

            this.stackSizeBox.setText(String.valueOf(newValue));
            this.sendStackSizeToServer((int) newValue);
        } catch (final NumberFormatException e) {
            this.stackSizeBox.setText("1");
            this.sendStackSizeToServer(1);
        }
    }

    private void sendStackSizeToServer(int stackSize) {
        ModularMachinery.NET_CHANNEL.sendToServer(
                new PktMEOutputBusStackSizeChange(this.outputBus.getPos(), stackSize)
        );
    }

    private long parseValue(String text) {
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            try {
                text = preprocessExponents(text);

                double result = MathExpressionParser.parse(text);
                if (Double.isNaN(result) || Double.isInfinite(result)) {
                    throw new NumberFormatException("Invalid expression");
                }

                return Math.round(result);
            } catch (Exception ex) {
                throw new NumberFormatException("Invalid number or expression");
            }
        }
    }

    private String preprocessExponents(String expression) {
        while (expression.contains("^")) {
            int caretIndex = expression.indexOf('^');

            int baseStart = caretIndex - 1;
            while (baseStart > 0 && (Character.isDigit(expression.charAt(baseStart - 1)) || expression.charAt(baseStart - 1) == '.')) {
                baseStart--;
            }

            int expEnd = caretIndex + 2;
            while (expEnd < expression.length() && (Character.isDigit(expression.charAt(expEnd)) || expression.charAt(expEnd) == '.')) {
                expEnd++;
            }

            String baseStr = expression.substring(baseStart, caretIndex);
            String expStr = expression.substring(caretIndex + 1, expEnd);

            double base = Double.parseDouble(baseStr);
            double exponent = Double.parseDouble(expStr);
            double result = Math.pow(base, exponent);

            expression = expression.substring(0, baseStart) + result + expression.substring(expEnd);
        }

        return expression;
    }

    @Override
    protected void keyTyped(final char character, final int key) throws IOException {
        if (!this.checkHotbarKeys(key)) {
            boolean isValidChar = Character.isDigit(character) ||
                    character == '+' || character == '-' ||
                    character == '*' || character == '/' ||
                    character == '^' ||
                    character == '(' || character == ')' ||
                    character == '.' || character == 'E' || character == 'e';
            boolean isControlKey = key == 14 || key == 211 || key == 203 || key == 205;

            if ((isValidChar || isControlKey) && this.stackSizeBox.textboxKeyTyped(character, key)) {
            } else {
                super.keyTyped(character, key);
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        final int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            final int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
            final int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

            int boxX = this.guiLeft + 62;
            int boxY = this.guiTop + 57;
            int boxWidth = 75;
            int boxHeight = this.fontRenderer.FONT_HEIGHT;

            if (x >= boxX && x <= boxX + boxWidth && y >= boxY && y <= boxY + boxHeight) {
                this.onMouseWheelEvent(wheel);
            }
        }
    }

    private void onMouseWheelEvent(int wheel) {
        boolean isShiftHeld = GuiScreen.isShiftKeyDown();
        boolean isCtrlHeld = GuiScreen.isCtrlKeyDown();

        String text = this.stackSizeBox.getText();
        long currentValue;

        try {
            currentValue = text.isEmpty() ? 1 : parseValue(text);
        } catch (NumberFormatException e) {
            currentValue = 1;
        }

        long newValue = currentValue;

        if (isShiftHeld && isCtrlHeld) {
            if (wheel > 0) {
                newValue = currentValue * 2;
            } else {
                newValue = currentValue / 2;
            }
        } else if (isShiftHeld) {
            if (wheel > 0) {
                newValue = currentValue + 1;
            } else {
                newValue = currentValue - 1;
            }
        }

        if (newValue > Integer.MAX_VALUE) {
            newValue = Integer.MAX_VALUE;
        } else if (newValue < 1) {
            newValue = 1;
        }

        this.stackSizeBox.setText(String.valueOf(newValue));
        this.sendStackSizeToServer((int) newValue);
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.fontRenderer.drawString(I18n.format("gui.meitembus.stack_size.name"), 8, 6, 0x404040);
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.mc.getTextureManager().bindTexture(TEXTURES);
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, this.xSize, this.ySize);

        this.stackSizeBox.drawTextBox();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);

        String text = this.stackSizeBox.getText();
        if (!text.isEmpty()) {
            try {
                long value = parseValue(text);
                int finalValue;
                if (value > Integer.MAX_VALUE) {
                    finalValue = Integer.MAX_VALUE;
                } else if (value < 1) {
                    finalValue = 1;
                } else {
                    finalValue = (int) value;
                }

                sendStackSizeToServer(finalValue);

            } catch (NumberFormatException e) {
                try {
                    ContainerMEItemOutputBusStackSize container = (ContainerMEItemOutputBusStackSize) this.inventorySlots;
                    sendStackSizeToServer(container.getStackSize());
                } catch (Exception ignored) {
                }
            }
        }
    }
}