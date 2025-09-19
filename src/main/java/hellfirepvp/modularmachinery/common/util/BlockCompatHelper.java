/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.util;

import com.google.common.collect.Iterables;
import hellfirepvp.modularmachinery.common.base.Mods;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockCompatHelper
 * Created by HellFirePvP
 * Date: 27.11.2017 / 20:49
 */
public class BlockCompatHelper {

    private static final ResourceLocation ic2TileBlock = new ResourceLocation("ic2", "te");

    private static final Method getITeBlockIc2, getTeClassIc2, getTeBlockState, getITEgetSupportedFacings, getTEBlockId, getITeBlockIc2Class;
    private static final IProperty<EnumFacing> facingPropertyField;
    private static final Field                 teBlockItemField;

    static {
        Method m1 = null, m2 = null, m3 = null, m4 = null, m5 = null, m6 = null;
        IProperty<EnumFacing> f = null;
        Field f1 = null;
        if (Mods.IC2.isPresent()) {
            try {
                Class<?> clazz = Class.forName("ic2.core.block.TeBlockRegistry");
                m1 = clazz.getDeclaredMethod("get", String.class);
                m6 = clazz.getDeclaredMethod("get", Class.class);
                clazz = Class.forName("ic2.core.block.ITeBlock");
                m2 = clazz.getDeclaredMethod("getTeClass");
                m4 = clazz.getDeclaredMethod("getSupportedFacings");
                clazz = Class.forName("ic2.core.block.state.IIdProvider");
                m5 = clazz.getDeclaredMethod("getId");
                m5.setAccessible(true);
                clazz = Class.forName("ic2.core.block.TileEntityBlock");
                m3 = clazz.getDeclaredMethod("getBlockState");
                clazz = Class.forName("ic2.core.block.BlockTileEntity");
                f1 = clazz.getDeclaredField("item");
                f1.setAccessible(true);
                f = (IProperty<EnumFacing>) clazz.getDeclaredField("facingProperty").get(null);
            } catch (Throwable ignored) {
            }
        }
        getITeBlockIc2 = m1;
        getITeBlockIc2Class = m6;
        getTeClassIc2 = m2;
        getTeBlockState = m3;
        facingPropertyField = f;
        getITEgetSupportedFacings = m4;
        getTEBlockId = m5;
        teBlockItemField = f1;
    }

    @Nonnull
    @net.minecraftforge.fml.common.Optional.Method(modid = "ic2")
    public static ItemStack tryGetIC2MachineStack(IBlockState state, Object tile) {
        try {
            Object tileITBlock = getITeBlockIc2Class.invoke(null, tile.getClass());
            int id = (int) getTEBlockId.invoke(tileITBlock);
            if (id != -1) {
                Item item = (Item) teBlockItemField.get(state.getBlock());
                return new ItemStack(item, 1, id);
            }
        } catch (Throwable ignored) {
        }
        return ItemStack.EMPTY;
    }

    @Nonnull
    public static Tuple<IBlockState, TileEntity> transformState(IBlockState state, @Nullable NBTTagCompound matchTag, BlockArray.TileInstantiateContext context) {
        ResourceLocation blockRes = state.getBlock().getRegistryName();
        if (ic2TileBlock.equals(blockRes) && matchTag != null) {
            Tuple<IBlockState, TileEntity> ret = tryRecoverTileState(state, matchTag, context);
            if (ret != null) {
                return ret;
            }
        }
        TileEntity te = state.getBlock().hasTileEntity(state) ? state.getBlock().createTileEntity(context.world(), state) : null;
        if (te != null) {
            context.apply(te);
        }
        return new Tuple<>(state, te);
    }

    @net.minecraftforge.fml.common.Optional.Method(modid = "ic2")
    private static Tuple<IBlockState, TileEntity> tryRecoverTileState(IBlockState state, @Nonnull NBTTagCompound matchTag, BlockArray.TileInstantiateContext context) {
        if (getTeClassIc2 == null || getITeBlockIc2 == null || getTeBlockState == null
            || getITEgetSupportedFacings == null || facingPropertyField == null) {
            return null;
        }

        ResourceLocation ic2TileBlock = new ResourceLocation("ic2", "te");
        if (ic2TileBlock.equals(state.getBlock().getRegistryName())) {
            if (matchTag.hasKey("id")) {
                ResourceLocation key = new ResourceLocation(matchTag.getString("id"));
                if (key.getNamespace().equalsIgnoreCase("ic2")) {
                    String name = key.getPath();
                    try {
                        Object o = getITeBlockIc2.invoke(null, name);
                        Object oClazz = getTeClassIc2.invoke(o);
                        if (oClazz instanceof Class) {
                            TileEntity te = (TileEntity) ((Class<?>) oClazz).newInstance();
                            context.apply(te);
                            te.readFromNBT(matchTag);

                            IBlockState st = (IBlockState) getTeBlockState.invoke(te);
                            EnumFacing applicable = Iterables.getFirst((Iterable<EnumFacing>) getITEgetSupportedFacings.invoke(o), EnumFacing.NORTH);
                            st = st.withProperty(facingPropertyField, applicable);
                            return new Tuple<>(st, te);
                        }
                    } catch (Throwable tr) {
                        tr.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

}
