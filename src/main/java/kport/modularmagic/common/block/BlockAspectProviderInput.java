package kport.modularmagic.common.block;

import hellfirepvp.modularmachinery.common.CommonProxy;
import kport.modularmagic.common.tile.TileAspectProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class BlockAspectProviderInput extends BlockAspectProvider {

    public BlockAspectProviderInput() {
        super(Material.IRON);
        setHardness(2F);
        setResistance(10F);
        setSoundType(SoundType.METAL);
        setHarvestLevel("pickaxe", 1);
        setCreativeTab(CommonProxy.creativeTabModularMachinery);
    }

    @Nullable
    @Override
    @Optional.Method(modid = "thaumcraft")
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileAspectProvider.Input();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@NotNull ItemStack stack, @Nullable World worldIn, @NotNull List<String> tooltip, @NotNull ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip.add(I18n.format("tooltip.groupinput.block"));
    }

}
