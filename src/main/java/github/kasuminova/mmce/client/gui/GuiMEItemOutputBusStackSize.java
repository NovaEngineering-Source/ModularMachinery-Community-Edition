package github.kasuminova.mmce.client.gui;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.MathExpressionParser;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.core.localization.GuiText;
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
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;

/**
 * GUI for configuring the stack size of an ME Item Output Bus.
 * This GUI mimics AE2's Priority GUI design but is adapted for stack size configuration.
 */
public class GuiMEItemOutputBusStackSize extends AEBaseGui {

    private static final ResourceLocation TEXTURES = new ResourceLocation("modularmachinery", "textures/gui/stacksize.png");

    private GuiTextField stackSizeBox;  // Changed from GuiNumberBox to allow expressions
    private GuiTabButton originalGuiBtn;
    private final MEItemOutputBus outputBus;

    // Button references
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
        this.ySize = 105; // Same height as AE2's Priority GUI
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        super.initGui();

        // Create the +/- buttons (matching AE2's Priority GUI layout)
        this.buttonList.add(this.plus1 = new GuiButton(0, this.guiLeft + 20, this.guiTop + 32, 22, 20, "+1"));
        this.buttonList.add(this.plus10 = new GuiButton(0, this.guiLeft + 48, this.guiTop + 32, 28, 20, "+10"));
        this.buttonList.add(this.plus100 = new GuiButton(0, this.guiLeft + 82, this.guiTop + 32, 32, 20, "+100"));
        this.buttonList.add(this.plus1000 = new GuiButton(0, this.guiLeft + 120, this.guiTop + 32, 38, 20, "+1000"));

        this.buttonList.add(this.minus1 = new GuiButton(0, this.guiLeft + 20, this.guiTop + 69, 22, 20, "-1"));
        this.buttonList.add(this.minus10 = new GuiButton(0, this.guiLeft + 48, this.guiTop + 69, 28, 20, "-10"));
        this.buttonList.add(this.minus100 = new GuiButton(0, this.guiLeft + 82, this.guiTop + 69, 32, 20, "-100"));
        this.buttonList.add(this.minus1000 = new GuiButton(0, this.guiLeft + 120, this.guiTop + 69, 38, 20, "-1000"));

        // Create button to return to main Output Bus GUI
        final ContainerMEItemOutputBusStackSize container = (ContainerMEItemOutputBusStackSize) this.inventorySlots;
        final ItemStack busIcon = new ItemStack(this.outputBus.getBlockType());

        if (!busIcon.isEmpty()) {
            this.buttonList.add(this.originalGuiBtn = new GuiTabButton(
                    this.guiLeft + 154,
                    this.guiTop,
                    busIcon,
                    "ME Machinery Item Output Bus",  // Fixed label
                    this.itemRender
            ));
        }

        // Create the number input box - using GuiTextField to allow math expressions
        this.stackSizeBox = new GuiTextField(
                0,  // componentId
                this.fontRenderer,
                this.guiLeft + 62,
                this.guiTop + 57,
                75,
                this.fontRenderer.FONT_HEIGHT
        );
        this.stackSizeBox.setEnableBackgroundDrawing(false);
        this.stackSizeBox.setMaxStringLength(32);  // Longer to allow expressions like "(100+50)*2"
        this.stackSizeBox.setTextColor(0xFFFFFF);
        this.stackSizeBox.setVisible(true);
        this.stackSizeBox.setFocused(true);

        // Set initial value from container
        this.stackSizeBox.setText(String.valueOf(container.getStackSize()));
    }

    @Override
    protected void actionPerformed(final GuiButton btn) throws IOException {
        super.actionPerformed(btn);

        if (btn == this.originalGuiBtn) {
            // Calculate and send before switching GUI
            String text = this.stackSizeBox.getText();
            if (!text.isEmpty()) {
                try {
                    long value = parseValue(text);
                    // Clamp to valid range
                    if (value > Integer.MAX_VALUE) {
                        value = Integer.MAX_VALUE;
                    } else if (value < 1) {
                        value = 1;
                    }
                    this.sendStackSizeToServer((int) value);
                } catch (NumberFormatException e) {
                    // Ignore parsing errors
                }
            }

            // Switch back to main GUI
            ModularMachinery.NET_CHANNEL.sendToServer(
                    new PktSwitchGuiMEOutputBus(this.outputBus.getPos(), 0)
            );
        }

        // Handle +/- buttons
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

            // Allow empty field - treat as 0 for calculation
            long currentValue = text.isEmpty() ? 0 : parseValue(text);
            long newValue = currentValue + amount;

            // Clamp to valid range - this now handles values > Integer.MAX_VALUE
            if (newValue > Integer.MAX_VALUE) {
                newValue = Integer.MAX_VALUE;
            } else if (newValue < 1) {
                newValue = 1;
            }

            this.stackSizeBox.setText(String.valueOf(newValue));
            this.sendStackSizeToServer((int) newValue);
        } catch (final NumberFormatException e) {
            // If parsing fails, default to 1
            this.stackSizeBox.setText("1");
            this.sendStackSizeToServer(1);
        }
    }

    private void sendStackSizeToServer(int stackSize) {
        System.out.println("GuiMEItemOutputBusStackSize: Sending stack size to server: " + stackSize);
        ModularMachinery.NET_CHANNEL.sendToServer(
                new PktMEOutputBusStackSizeChange(this.outputBus.getPos(), stackSize)
        );
    }

    /**
     * Parses the value, supporting both plain integers and mathematical expressions.
     * Returns a long to allow values > Integer.MAX_VALUE which will be clamped later.
     * Examples: "500", "5*100", "1000/2", "250+250", "2^10", "9999999999999"
     */
    private long parseValue(String text) {
        // First try plain long parsing (fast path)
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            // If that fails, try mathematical expression parsing
            try {
                // Preprocess: Convert ^ (exponent) to power operations
                // Example: "2^10" becomes a calculation
                text = preprocessExponents(text);

                double result = MathExpressionParser.parse(text);
                if (Double.isNaN(result) || Double.isInfinite(result)) {
                    throw new NumberFormatException("Invalid expression");
                }

                // Return as long - can exceed Integer.MAX_VALUE
                return (long) Math.round(result);
            } catch (Exception ex) {
                throw new NumberFormatException("Invalid number or expression");
            }
        }
    }

    /**
     * Preprocesses the expression to handle ^ (exponent) operator.
     * Converts expressions like "2^10" into "1024" by evaluating power operations.
     */
    private String preprocessExponents(String expression) {
        // Keep evaluating exponents until none remain
        while (expression.contains("^")) {
            // Find the first ^ operator
            int caretIndex = expression.indexOf('^');

            // Find the base (number before ^)
            int baseStart = caretIndex - 1;
            while (baseStart > 0 && (Character.isDigit(expression.charAt(baseStart - 1)) || expression.charAt(baseStart - 1) == '.')) {
                baseStart--;
            }

            // Find the exponent (number after ^)
            int expEnd = caretIndex + 2;
            while (expEnd < expression.length() && (Character.isDigit(expression.charAt(expEnd)) || expression.charAt(expEnd) == '.')) {
                expEnd++;
            }

            // Extract base and exponent
            String baseStr = expression.substring(baseStart, caretIndex);
            String expStr = expression.substring(caretIndex + 1, expEnd);

            // Calculate the power
            double base = Double.parseDouble(baseStr);
            double exponent = Double.parseDouble(expStr);
            double result = Math.pow(base, exponent);

            // Replace the expression with the result
            expression = expression.substring(0, baseStart) + result + expression.substring(expEnd);
        }

        return expression;
    }

    @Override
    protected void keyTyped(final char character, final int key) throws IOException {
        if (!this.checkHotbarKeys(key)) {
            // Allow digits, operators (including ^), decimal point, backspace, delete, and arrow keys
            boolean isValidChar = Character.isDigit(character) ||
                    character == '+' || character == '-' ||
                    character == '*' || character == '/' ||
                    character == '^' ||  // Added exponent operator
                    character == '(' || character == ')' ||
                    character == '.' || character == 'E' || character == 'e';
            boolean isControlKey = key == 14 || key == 211 || key == 203 || key == 205; // backspace, delete, left, right

            if ((isValidChar || isControlKey) && this.stackSizeBox.textboxKeyTyped(character, key)) {
                // Just let the user type - don't evaluate or send to server yet
                // Evaluation happens when GUI closes or user presses ENTER
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
            // Get mouse position
            final int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
            final int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

            // Check if mouse is over the number box
            int boxX = this.guiLeft + 62;
            int boxY = this.guiTop + 57;
            int boxWidth = 75;
            int boxHeight = this.fontRenderer.FONT_HEIGHT;

            if (x >= boxX && x <= boxX + boxWidth && y >= boxY && y <= boxY + boxHeight) {
                this.onMouseWheelEvent(wheel);
            }
        }
    }

    /**
     * Handles mouse wheel scrolling for the stack size value.
     */
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
            // SHIFT + CTRL: Double or halve
            if (wheel > 0) {
                newValue = currentValue * 2;
            } else {
                newValue = currentValue / 2;
            }
        } else if (isShiftHeld) {
            // SHIFT: +/- 1
            if (wheel > 0) {
                newValue = currentValue + 1;
            } else {
                newValue = currentValue - 1;
            }
        }

        // Clamp to valid range - handles overflow
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
        // FIX #1: Only draw "Stack Size" once (removed duplicate)
        this.fontRenderer.drawString("Stack Size", 8, 6, 0x404040);
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.mc.getTextureManager().bindTexture(TEXTURES);  // ← Use the constant you defined
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, this.xSize, this.ySize);

        this.stackSizeBox.drawTextBox();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);

        // Send the final stack size to server when GUI closes
        String text = this.stackSizeBox.getText();
        if (!text.isEmpty()) {
            try {
                long value = parseValue(text);
                // Clamp to valid range
                int finalValue;
                if (value > Integer.MAX_VALUE) {
                    finalValue = Integer.MAX_VALUE;
                } else if (value < 1) {
                    finalValue = 1;
                } else {
                    finalValue = (int) value;
                }

                // Send to server
                sendStackSizeToServer(finalValue);

            } catch (NumberFormatException e) {
                // If parsing fails, try to get current value from container
                try {
                    ContainerMEItemOutputBusStackSize container = (ContainerMEItemOutputBusStackSize) this.inventorySlots;
                    sendStackSizeToServer(container.getStackSize());
                } catch (Exception ex) {
                    // Last resort - do nothing, keep existing value
                }
            }
        }
    }
}  // ← THIS CLOSING BRACE IS CRITICAL - it closes the CLASS