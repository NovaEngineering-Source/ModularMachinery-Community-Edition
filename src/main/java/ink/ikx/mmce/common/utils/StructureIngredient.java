package ink.ikx.mmce.common.utils;

import com.github.bsideup.jabel.Desugar;
import hellfirepvp.modularmachinery.common.util.BlockArray;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Desugar
public record StructureIngredient(List<ItemIngredient> itemIngredient, List<FluidIngredient> fluidIngredient) {

    public static StructureIngredient of(World world, BlockPos ctrlPos, BlockArray blockArray) {
        List<ItemIngredient> itemIngredients = blockArray.getBlockStateIngredientList(world, ctrlPos);
        List<FluidIngredient> fluidIngredients = new ArrayList<>();

        // Search fluid block from ingredients.
        Iterator<ItemIngredient> iterator = itemIngredients.iterator();
        while (iterator.hasNext()) {
            final ItemIngredient itemIngredient = iterator.next();
            final BlockPos pos = itemIngredient.pos();
            final List<Tuple<FluidStack, IBlockState>> fluidIngredient = new ArrayList<>();

            for (final Tuple<ItemStack, IBlockState> tuple : itemIngredient.ingredientList()) {
                IBlockState state = tuple.getSecond();
                FluidStack fluidStack = FluidUtils.getFluidStackFromBlockState(state);
                if (fluidStack == null) {
                    continue;
                }
                fluidIngredient.add(new Tuple<>(fluidStack, state));
            }

            if (!fluidIngredient.isEmpty()) {
                fluidIngredients.add(new FluidIngredient(pos, fluidIngredient));
                iterator.remove();
            }
        }

        return new StructureIngredient(itemIngredients, fluidIngredients);
    }

    public StructureIngredient copy() {
        return new StructureIngredient(new ArrayList<>(itemIngredient), new ArrayList<>(fluidIngredient));
    }

    @Desugar
    public record ItemIngredient(BlockPos pos, List<Tuple<ItemStack, IBlockState>> ingredientList, NBTTagCompound nbt) {
    }

    @Desugar
    public record FluidIngredient(BlockPos pos, List<Tuple<FluidStack, IBlockState>> ingredientList) {
    }

}
