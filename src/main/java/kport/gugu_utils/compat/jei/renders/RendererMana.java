package kport.gugu_utils.compat.jei.renders;

import com.google.common.collect.Lists;
import kport.gugu_utils.compat.jei.ingedients.IngredientMana;
import static kport.gugu_utils.tools.RenderUtils.renderItem;
import static kport.gugu_utils.tools.RenderUtils.renderManaBarVertical;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.block.tile.mana.TilePool;
import vazkii.botania.common.core.helper.ItemNBTHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class RendererMana implements IIngredientRenderer<IngredientMana> {
    public static final RendererMana INSTANCE = new RendererMana();
    public static final String KEY_MANA_AMOUNT = "tooltips.gugu-utils.mana_amount";
    public static final String KEY_MANA_AMOUNT_TICK = "tooltips.gugu-utils.mana_amount.tick";
    public static final String KEY_MANA_AMOUNT_TOTAL = "tooltips.gugu-utils.mana_amount.total";

    private final ItemStack itemPool = new ItemStack(ModBlocks.pool);
    private final ItemStack itemPoolDiluted = new ItemStack(ModBlocks.pool, 1, 2);
    private final ItemStack itemPoolFabulous = new ItemStack(ModBlocks.pool, 1, 3);

    private final int offsetX;
    private final int offsetY;

    private RendererMana() {
        ItemNBTHelper.setBoolean(itemPool, "RenderFull", true);
        ItemNBTHelper.setBoolean(itemPoolDiluted, "RenderFull", true);
        ItemNBTHelper.setBoolean(itemPoolFabulous, "RenderFull", true);
        offsetX = 0;
        offsetY = 0;
    }

    @Override
    @Nonnull
    public List<String> getTooltip(Minecraft minecraft, IngredientMana ingredient, ITooltipFlag tooltipFlag) {
        List<String> result = Lists.newArrayList();
        if (!(ingredient.getValue() instanceof Number)) {
            result.add("Mana");
            return result;
        }

        int mana = (int) ingredient.getValue();


        if (ingredient.getTicks() > 0) {
            result.add(I18n.format(KEY_MANA_AMOUNT_TICK, ingredient.getValue()));
            result.add(I18n.format(KEY_MANA_AMOUNT_TOTAL, mana * ingredient.getTicks()));
        } else {
            result.add(I18n.format(KEY_MANA_AMOUNT, ingredient.getValue()));
        }

        return result;
    }

    @Override
    public void render(Minecraft minecraft, int xPosition, int yPosition, @Nullable IngredientMana ingredient) {
        xPosition += offsetX;
        yPosition += offsetY;

        if (ingredient == null || !(ingredient.getValue() instanceof Number)) {
            return;
        }
        int mana = (int) ingredient.getValue();

        if (ingredient.getTicks() > 0){
            mana *= ingredient.getTicks();
        }
        GlStateManager.enableAlpha();
        if (mana <= TilePool.MAX_MANA / 10) {
            renderManaBarVertical(xPosition, yPosition, 0x109EFF, 0.75F, mana, TilePool.MAX_MANA / 10);
            renderItem(minecraft, itemPoolDiluted, xPosition - 5, yPosition + 102, null);
        } else if (mana <= TilePool.MAX_MANA) {
            renderManaBarVertical(xPosition, yPosition, 0x0000FF, 0.75F, mana, TilePool.MAX_MANA);
            renderItem(minecraft, itemPool, xPosition - 5, yPosition + 102, null);
        } else {
            int times = 2;
            while (mana >= TilePool.MAX_MANA * times) {
                times *= 1.5;
            }
            renderManaBarVertical(xPosition, yPosition, 0x00FF00, 0.75F, mana, TilePool.MAX_MANA * times);
            renderItem(minecraft, itemPoolFabulous, xPosition - 5, yPosition + 102, "*" + times);
        }
        GlStateManager.disableAlpha();


    }


}
