package kport.gugu_utils.compat.jei.renders;

import kport.gugu_utils.common.envtypes.*;
import kport.gugu_utils.compat.jei.ingedients.IngredientEnvironment;
import static kport.gugu_utils.tools.RenderUtils.*;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

public class RendererEnvironment implements IIngredientRenderer<IngredientEnvironment> {
    public static final RendererEnvironment INSTANCE = new RendererEnvironment();
    private final ItemStack itemClock = new ItemStack(Items.CLOCK);
    ResourceLocation portal = new ResourceLocation("minecraft", "textures/blocks/portal.png");

    private RendererEnvironment() {

    }

    @Override
    public void render(Minecraft minecraft, int x, int y, @Nullable IngredientEnvironment ingredient) {
        if (ingredient == null) {
            return;
        }
        GlStateManager.enableAlpha();
        minecraft.renderEngine.bindTexture(RECIPES_UI);
        GlStateManager.color(1F, 1F, 1F, 0.8f);
        EnvironmentType type = ingredient.getType();
        if (type instanceof EnvMoonPhase) {
            int[] phases = ((EnvMoonPhase) type).getPhases();
            int phase = phases.length > 0 ? phases[0] : 0;
            if (minecraft.world != null) {
                int index = (int) (minecraft.world.getWorldTime() / 30 % (phases.length));
                phase = phases[index];
            }
            int textureX = 106 + phase * 16;
            drawTexturedModalRect(x, y, 0, textureX, 0, 16, 16);
        } else if (type instanceof EnvWeather) {
            switch (((EnvWeather) type).getWeather()) {
                case SUNNY:
                    drawTexturedModalRect(x, y, 0, 106, 16, 16, 16);
                    break;
                case RAINING:
                    drawTexturedModalRect(x, y, 0, 106 + 16, 16, 16, 16);
                    break;
                case SNOWING:
                    drawTexturedModalRect(x, y, 0, 106 + 32, 16, 16, 16);
                    break;
                case THUNDERING:
                    drawTexturedModalRect(x, y, 0, 106 + 48, 16, 16, 16);
                    break;
            }
        } else if (type instanceof EnvTime) {
            renderItem(minecraft, itemClock, x, y, null);
        } else if (type instanceof EnvAltitude) {
            drawTexturedModalRect(x, y, 0, 170, 16, 16, 16);
        } else if (type instanceof EnvDimension) {
            int offset = (int) (minecraft.world.getWorldTime() % 32);
            minecraft.renderEngine.bindTexture(portal);
            drawModalRectWithCustomSizedTexture(x, y, 0, offset * 16, 16, 16, 16, 512);
        } else if (type instanceof EnvBoime) {
            drawTexturedModalRect(x, y, 0, 186, 16, 16, 16);
        }
        GlStateManager.disableAlpha();


    }

    @Override
    public List<String> getTooltip(Minecraft minecraft, IngredientEnvironment ingredient, ITooltipFlag tooltipFlag) {
        return ingredient.getType().getTooltip();
    }
}
