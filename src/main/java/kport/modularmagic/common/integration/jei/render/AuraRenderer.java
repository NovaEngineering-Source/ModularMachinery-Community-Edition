package kport.modularmagic.common.integration.jei.render;

import de.ellpeck.naturesaura.api.aura.type.IAuraType;
import de.ellpeck.naturesaura.items.ModItems;
import kport.modularmagic.common.integration.jei.ingredient.Aura;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AuraRenderer implements IIngredientRenderer<Aura> {

    private static ItemStack aura     = null;
    private static IAuraType auraType = null;

    @Override
    public void render(Minecraft minecraft, int xPosition, int yPosition, @Nullable Aura ingredient) {
        if (ingredient == null) {
            return;
        }

        if (aura == null || auraType == null || auraType != ingredient.getType()) {
            auraType = ingredient.getType();
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("stored_type", auraType.getName().toString());
            aura = new ItemStack(ModItems.AURA_BOTTLE, ingredient.getAmount());
            aura.setTagCompound(nbt);
        }
        minecraft.getRenderItem().renderItemIntoGUI(aura, xPosition, yPosition);
        GlStateManager.translate(0D, 0D, 101D);
        FontRenderer fr = getFontRenderer(minecraft, ingredient);
        String s = String.valueOf(ingredient.getAmount());
        fr.drawStringWithShadow(s, (float) (xPosition + 19 - 2 - fr.getStringWidth(s)), (float) (yPosition + 6 + 3), 16777215);
    }

    @Override
    public List<String> getTooltip(Minecraft minecraft, Aura ingredient, ITooltipFlag tooltipFlag) {
        List<String> tooltip = new ArrayList<>();
        String name = ingredient.getType().getName().getPath();
        tooltip.add(name.substring(0, 1).toUpperCase() + name.substring(1) + " Aura");
        return tooltip;
    }

    @Override
    public FontRenderer getFontRenderer(Minecraft minecraft, Aura ingredient) {
        return minecraft.fontRenderer;
    }
}
