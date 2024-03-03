package kport.gugu_utils.common;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraftforge.client.model.ModelLoader;

public abstract class VariantBlock<T extends Enum<T> & IStringSerializable> extends GenericBlock {
    public final Class<T> variantType;

    public VariantBlock(Material materialIn, Class<T> variantType) {
        super(materialIn);
        this.variantType = variantType;
        setDefaultState(blockState.getBaseState().withProperty(getVariant(), variantType.getEnumConstants()[0]));
    }

    public static <T extends Enum<T> & IStringSerializable> T getVariant(ItemStack stack, Class<T> type) {
        if (stack.getMetadata() < 0 || stack.getMetadata() >= type.getEnumConstants().length) {
            return null;
        }
        return type.getEnumConstants()[stack.getMetadata()];
    }

    public abstract PropertyEnum<T> getVariant();

    @Override
    public int damageDropped(IBlockState state) {
        return getMetaFromState(state);
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        for (T variant : variantType.getEnumConstants()) {
            items.add(new ItemStack(this, 1, variant.ordinal()));
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, getVariant());
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        if (meta > variantType.getEnumConstants().length) {
            meta = 0;
        }
        return getDefaultState().withProperty(getVariant(), variantType.getEnumConstants()[meta]);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(getVariant()).ordinal();
    }

    @Override
    public void initModel() {
        for (T variant : variantType.getEnumConstants()) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this),
                    variant.ordinal(), new ModelResourceLocation(getRegistryName(), "variant=" + variant.getName()));
        }
    }

}
