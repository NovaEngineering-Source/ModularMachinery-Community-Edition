package kport.gugu_utils.common.item;

import static kport.gugu_utils.common.Constants.NAME_MANA;
import kport.gugu_utils.common.block.BlockSparkManaHatch;
import kport.gugu_utils.common.IOHatchVariant;
import kport.gugu_utils.tools.ItemNBTUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.Optional;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.api.mana.IManaTooltipDisplay;
import vazkii.botania.common.core.helper.ItemNBTHelper;

@Optional.Interface(iface = "vazkii.botania.api.mana.IManaItem",modid = "botania")
@Optional.Interface(iface = "vazkii.botania.api.mana.IManaTooltipDisplay",modid = "botania")
public class ItemManaBlock extends VariantItem<IOHatchVariant> implements IManaItem, IManaTooltipDisplay {
    private final int maxMana;

    public ItemManaBlock(BlockSparkManaHatch block, int maxMana) {
        super(block);
        this.maxMana = maxMana;
    }

    private static void setMana(ItemStack stack, int mana) {
        ItemNBTHelper.setInt(stack, NAME_MANA, mana);
    }



    @Override
    public int getMana(ItemStack stack) {
        return ItemNBTUtils.getInt(stack, NAME_MANA, 0);
    }

    @Override
    public int getMaxMana(ItemStack stack) {
        return maxMana;
    }

    @Override
    public void addMana(ItemStack stack, int mana) {
        setMana(stack, Math.min(getMana(stack) + mana, maxMana));
    }


    @Override
    public boolean canReceiveManaFromPool(ItemStack stack, TileEntity pool) {
        return this.getMetadata(stack) == IOHatchVariant.INPUT.ordinal();
    }

    @Override
    public boolean canReceiveManaFromItem(ItemStack stack, ItemStack otherStack) {
        return false;
    }

    @Override
    public boolean canExportManaToPool(ItemStack stack, TileEntity pool) {
        return this.getMetadata(stack) == IOHatchVariant.OUTPUT.ordinal();
    }

    @Override
    public boolean canExportManaToItem(ItemStack stack, ItemStack otherStack) {
        return false;
    }

    @Override
    public boolean isNoExport(ItemStack stack) {
        return true;
    }

    @Override
    public float getManaFractionForDisplay(ItemStack stack) {
        return 1.0F * this.getMana(stack) / this.maxMana;
    }

}
