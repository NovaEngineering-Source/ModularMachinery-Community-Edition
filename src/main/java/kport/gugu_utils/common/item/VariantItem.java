package kport.gugu_utils.common.item;

import kport.gugu_utils.common.VariantBlock;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VariantItem<T extends Enum<T> & IStringSerializable> extends ItemBlock {

    public final PropertyEnum<T> VARIANT;
    public final Class<T> variantType;
    public VariantItem(VariantBlock<T> block) {
        super(block);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
        this.VARIANT = block.getVariant();
        this.variantType = block.variantType;

        this.addPropertyOverride(new ResourceLocation("meta"), new IItemPropertyGetter() {
            @Override
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
                if(stack.getItemDamage() == 0)
                    return 0F;
                if (stack.getItemDamage() == 1)
                    return 1.0F;
                if (stack.getItemDamage() == 2)
                    return 2.0F;
                return 0.0F;
            }
        });
    }


    @Override
    public int getMetadata(int meta) {
        return meta;
    }

    @Override
    @Nonnull
    public String getTranslationKey(ItemStack stack) {
        int meta = stack.getMetadata() < variantType.getEnumConstants().length ? stack.getMetadata() : 0;
        return super.getTranslationKey(stack) + "." + variantType.getEnumConstants()[meta].getName();
    }


}
