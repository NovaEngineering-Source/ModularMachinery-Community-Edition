package github.kasuminova.mmce.common.integration.gregtech.patternproxy;

import github.kasuminova.mmce.common.machine.pattern.SpecialItemBlockProxy;
import gregtech.api.block.machines.MachineItemBlock;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.util.GTUtility;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class GTBlockMachineProxy implements SpecialItemBlockProxy {
    public static final GTBlockMachineProxy INSTANCE = new GTBlockMachineProxy();

    private GTBlockMachineProxy() {
    }

    @Override
    public boolean isValid(final ItemStack stack) {
        return stack.getItem() instanceof MachineItemBlock;
    }

    @Override
    public TileEntity transformState(final ItemStack stack, final World world) {
        MetaTileEntity metaTileEntity = GTUtility.getMetaTileEntity(stack);
        MetaTileEntityHolder holder = new MetaTileEntityHolder();
        holder.setMetaTileEntity(metaTileEntity);
        holder.getMetaTileEntity().onPlacement();
        return holder;
    }

    @Override
    public ItemStack getTrueStack(final IBlockState state, final TileEntity te) {
        if (!(te instanceof MetaTileEntityHolder holder)) {
            return ItemStack.EMPTY;
        }
        MetaTileEntity mte = holder.getMetaTileEntity();
        return mte != null ? mte.getStackForm() : ItemStack.EMPTY;
    }

}
