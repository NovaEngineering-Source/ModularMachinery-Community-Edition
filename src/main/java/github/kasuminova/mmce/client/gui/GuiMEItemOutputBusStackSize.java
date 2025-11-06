package github.kasuminova.mmce.client.gui;

import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiNumberBox;
import appeng.client.gui.widgets.GuiTabButton;
import github.kasuminova.mmce.common.container.ContainerMEItemOutputBusStackSize;
import github.kasuminova.mmce.common.network.PktMEOutputBusStackSizeChange;
import github.kasuminova.mmce.common.network.PktSwitchGuiMEOutputBus;
import github.kasuminova.mmce.common.tile.MEItemOutputBus;
import hellfirepvp.modularmachinery.ModularMachinery;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;

/**
 * GUI for configuring the stack size limit of an ME Item Output Bus.
 * This GUI mimics AE2's Priority GUI design but is adapted for stack size configuration.
 */
public class GuiMEItemOutputBusStackSize extends AEBaseGui {

    private GuiNumberBox stackSizeBox;
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
                    "ME Item Output Bus",
                    this.itemRender
            ));
        }

        // Create the number input box (centered, matching AE2's Priority GUI)
        this.stackSizeBox = new GuiNumberBox(
                this.fontRenderer,
                this.guiLeft + 62,
                this.guiTop + 57,
                59,
                this.fontRenderer.FONT_HEIGHT,
                Integer.class
        );
        this.stackSizeBox.setEnableBackgroundDrawing(false);
        this.stackSizeBox.setMaxStringLength(10); // Max int is 10 digits
        this.stackSizeBox.setTextColor(0xFFFFFF);
        this.stackSizeBox.setVisible(true);
        this.stackSizeBox.setFocused(true);

        // Set initial value from container
        this.stackSizeBox.setText(String.valueOf(container.getStackSize()));
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        // Draw title
        this.fontRenderer.drawString("Stack Size", 8, 6, 4210752);
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        // Use AE2's priority.png texture
        this.bindTexture("guis/priority.png");
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, this.xSize, this.ySize);

        // Draw the number box
        this.stackSizeBox.drawTextBox();
    }

    @Override
    protected void actionPerformed(final GuiButton btn) throws IOException {
        super.actionPerformed(btn);

        // Handle return to main GUI button
        if (btn == this.originalGuiBtn) {
            // Send packet to switch back to main Output Bus GUI
            ModularMachinery.NET_CHANNEL.sendToServer(
                    new PktSwitchGuiMEOutputBus(this.outputBus.getPos(), 0)
            );
            return;
        }

        // Handle +/- buttons
        if (btn == this.plus1) {
            this.addQty(1);
        } else if (btn == this.plus10) {
            this.addQty(10);
        } else if (btn == this.plus100) {
            this.addQty(100);
        } else if (btn == this.plus1000) {
            this.addQty(1000);
        } else if (btn == this.minus1) {
            this.addQty(-1);
        } else if (btn == this.minus10) {
            this.addQty(-10);
        } else if (btn == this.minus100) {
            this.addQty(-100);
        } else if (btn == this.minus1000) {
            this.addQty(-1000);
        }
    }

    /**
     * Adds the specified amount to the current stack size value
     */
    private void addQty(final int amount) {
        try {
            String text = this.stackSizeBox.getText();

            // Remove leading zeros
            boolean fixed = false;
            while (text.startsWith("0") && text.length() > 1) {
                text = text.substring(1);
                fixed = true;
            }

            if (fixed) {
                this.stackSizeBox.setText(text);
            }

            if (text.isEmpty()) {
                text = "1"; // Minimum value is 1
            }

            int currentValue = Integer.parseInt(text);
            int newValue = currentValue + amount;

            // Clamp to valid range (minimum 1, maximum Integer.MAX_VALUE)
            if (newValue < 1) {
                newValue = 1;
            }
            // Handle overflow
            if (amount > 0 && newValue < currentValue) {
                newValue = Integer.MAX_VALUE;
            }

            this.stackSizeBox.setText(String.valueOf(newValue));
            this.sendStackSizeToServer(newValue);
        } catch (final NumberFormatException e) {
            // If parsing fails, reset to 1
            this.stackSizeBox.setText("1");
            this.sendStackSizeToServer(1);
        }
    }

    /**
     * Sends the new stack size value to the server
     */
    private void sendStackSizeToServer(int stackSize) {
        ModularMachinery.NET_CHANNEL.sendToServer(
                new PktMEOutputBusStackSizeChange(this.outputBus.getPos(), stackSize)
        );
    }

    @Override
    protected void keyTyped(final char character, final int key) throws IOException {
        if (!this.checkHotbarKeys(key)) {
            // Allow typing numbers and navigation keys
            if ((key == 211 || key == 205 || key == 203 || key == 14 || Character.isDigit(character))
                    && this.stackSizeBox.textboxKeyTyped(character, key)) {
                try {
                    String text = this.stackSizeBox.getText();

                    // Remove leading zeros
                    boolean fixed = false;
                    while (text.startsWith("0") && text.length() > 1) {
                        text = text.substring(1);
                        fixed = true;
                    }

                    if (fixed) {
                        this.stackSizeBox.setText(text);
                    }

                    if (text.isEmpty()) {
                        text = "1";
                        this.stackSizeBox.setText(text);
                    }

                    int value = Integer.parseInt(text);
                    value = Math.max(1, value); // Minimum 1

                    this.sendStackSizeToServer(value);
                } catch (final NumberFormatException e) {
                    this.stackSizeBox.setText("1");
                    this.sendStackSizeToServer(1);
                }
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
            int boxWidth = 59;
            int boxHeight = this.fontRenderer.FONT_HEIGHT;

            if (x >= boxX && x <= boxX + boxWidth && y >= boxY && y <= boxY + boxHeight) {
                this.onMouseWheelEvent(wheel);
            }
        }
    }

    /**
     * Handles mouse wheel scrolling for the stack size value.
     * Matches the behavior of GuiMEItemInputBus:
     * - SHIFT+CTRL + scroll up = double
     * - SHIFT+CTRL + scroll down = halve
     * - SHIFT + scroll up = +1
     * - SHIFT + scroll down = -1
     */
    private void onMouseWheelEvent(final int wheel) {
        boolean shiftHeld = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        boolean ctrlHeld = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);

        // Only process if SHIFT is held (to match Input Bus behavior)
        if (!shiftHeld) {
            return;
        }

        try {
            int currentValue = Integer.parseInt(this.stackSizeBox.getText());
            int newValue;
            boolean isScrollingUp = wheel > 0;

            if (ctrlHeld) {
                // SHIFT+CTRL: Double or halve
                if (isScrollingUp) {
                    // Double (with overflow protection)
                    if (currentValue <= Integer.MAX_VALUE / 2) {
                        newValue = currentValue * 2;
                    } else {
                        newValue = Integer.MAX_VALUE;
                    }
                } else {
                    // Halve
                    newValue = Math.max(1, currentValue / 2);
                }
            } else {
                // SHIFT only: +1 or -1
                if (isScrollingUp) {
                    // +1 (with overflow protection)
                    if (currentValue < Integer.MAX_VALUE) {
                        newValue = currentValue + 1;
                    } else {
                        newValue = Integer.MAX_VALUE;
                    }
                } else {
                    // -1
                    newValue = Math.max(1, currentValue - 1);
                }
            }

            this.stackSizeBox.setText(String.valueOf(newValue));
            this.sendStackSizeToServer(newValue);
        } catch (NumberFormatException e) {
            this.stackSizeBox.setText("1");
            this.sendStackSizeToServer(1);
        }
    }

    protected String getBackground() {
        return "guis/priority.png";
    }
}