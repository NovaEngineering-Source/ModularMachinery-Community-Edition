package github.kasuminova.mmce.common.block.appeng;

import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import github.kasuminova.mmce.common.tile.MEPatternMirrorImage;
import hellfirepvp.modularmachinery.common.CommonProxy;
import hellfirepvp.modularmachinery.common.block.BlockMachineComponent;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockMEPatternMirrorImage extends BlockMachineComponent {

    final String mep = "tile.modularmachinery.blockmepatternprovider";

    public BlockMEPatternMirrorImage() {
        super(Material.IRON);
        setHardness(2F);
        setResistance(10F);
        setSoundType(SoundType.METAL);
        setHarvestLevel("pickaxe", 1);
        setCreativeTab(CommonProxy.creativeTabModularMachinery);
    }

    @Override
    public boolean onBlockActivated(
        @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state,
        @Nonnull EntityPlayer player, @Nonnull EnumHand hand,
        @Nonnull EnumFacing facing,
        float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote || hand != EnumHand.MAIN_HAND) {
            return false;
        }
        final MEPatternMirrorImage tileEntity = (MEPatternMirrorImage) worldIn.getTileEntity(pos);
        if (!player.getHeldItem(hand).isEmpty()) {
            var heldItem = player.getHeldItem(hand);
            if (!player.isSneaking()) {
                if (heldItem.getItem() instanceof IMemoryCard memoryCard) {
                    final String savedName = memoryCard.getSettingsName(heldItem);
                    final NBTTagCompound data = memoryCard.getData(heldItem);

                    if (mep.equals(savedName)) {
                        tileEntity.providerPos = BlockPos.fromLong(data.getLong("Pos"));
                        player.sendMessage(new TextComponentTranslation("message.blockmepatternprovider.load"));
                    } else {
                        memoryCard.notifyUser(player, MemoryCardMessages.INVALID_MACHINE);
                    }

                    return true;
                }
            }
        }
        var pPos = tileEntity.providerPos;
        if (tileEntity.providerPos == null) {
            player.sendMessage(new TextComponentTranslation("message.blockmepatternprovider.tip0"));
        } else {
            player.sendMessage(new TextComponentTranslation("message.blockmepatternprovider.tip1", pPos.getX(), pPos.getY(), pPos.getZ()));
        }
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new MEPatternMirrorImage();
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new MEPatternMirrorImage();
    }

    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Nonnull
    @Override
    public EnumBlockRenderType getRenderType(@Nonnull IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }
}
