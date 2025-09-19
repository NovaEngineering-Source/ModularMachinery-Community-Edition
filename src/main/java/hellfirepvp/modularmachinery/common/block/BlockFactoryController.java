package hellfirepvp.modularmachinery.common.block;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.CommonProxy;
import hellfirepvp.modularmachinery.common.machine.DynamicMachine;
import hellfirepvp.modularmachinery.common.tiles.TileFactoryController;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class BlockFactoryController extends BlockController {
    public static final Map<DynamicMachine, BlockFactoryController> FACTORY_CONTROLLERS = new HashMap<>();

    public BlockFactoryController() {
    }

    public BlockFactoryController(DynamicMachine parentMachine) {
        this();
        this.parentMachine = parentMachine;
        setRegistryName(new ResourceLocation(
            ModularMachinery.MODID, parentMachine.getRegistryName().getPath() + "_factory_controller")
        );
    }

    public static BlockFactoryController getControllerWithMachine(DynamicMachine machine) {
        return FACTORY_CONTROLLERS.get(machine);
    }

    @Override
    public boolean onBlockActivated(World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer playerIn, @Nonnull EnumHand hand, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof TileFactoryController) {
                playerIn.openGui(ModularMachinery.MODID, CommonProxy.GuiType.FACTORY.ordinal(), worldIn, pos.getX(), pos.getY(), pos.getZ());
            }
        }
        return true;
    }

    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    public String getLocalizedName() {
        if (parentMachine != null) {
            return I18n.format("tile.modularmachinery.machinefactorycontroller.name", parentMachine.getLocalizedName());
        }
        return I18n.format("tile.modularmachinery.blockfactorycontroller.name");
    }

    @Nonnull
    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileFactoryController(state);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileFactoryController(getStateFromMeta(meta));
    }

}
