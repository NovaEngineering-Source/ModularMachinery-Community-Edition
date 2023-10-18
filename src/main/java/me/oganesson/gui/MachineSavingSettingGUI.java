package me.oganesson.gui;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.widgets.textfield.TextEditorWidget;
import hellfirepvp.modularmachinery.ModularMachinery;
import net.minecraft.client.resources.I18n;

public class MachineSavingSettingGUI extends ModularScreen {

    public MachineSavingSettingGUI() {
        super(ModularMachinery.MODID);
    }

    @Override
    public ModularPanel buildUI(GuiContext context) {
        ModularPanel panel = ModularPanel.defaultPanel("saving_machine");
        panel.child(IKey.lang("gui.saving_machine.title").asWidget()
                .top(7).left(7));
        panel.child(new TextEditorWidget().top(20).left(6)
                .addTooltipLine(I18n.format("gui.saving_machine.name_editor.tooltip")));
        return panel;
    }

}
