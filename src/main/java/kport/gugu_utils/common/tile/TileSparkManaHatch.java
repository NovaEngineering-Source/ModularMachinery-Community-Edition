package kport.gugu_utils.common.tile;

import com.google.common.base.Predicates;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.lib.BlocksMM;
import kport.gugu_utils.common.block.BlockSparkManaHatch;
import static kport.gugu_utils.common.Constants.NAME_MANA;
import kport.gugu_utils.common.IRestorableTileEntity;
import kport.gugu_utils.CommonMMTile;
import kport.gugu_utils.network.PacketMana;
import kport.gugu_utils.tools.CIELab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.api.mana.spark.ISparkEntity;
import vazkii.botania.client.core.handler.HUDHandler;
import vazkii.botania.common.core.handler.ModSounds;

import java.awt.*;
import java.util.List;

public abstract class TileSparkManaHatch extends CommonMMTile implements IRestorableTileEntity, ISparkAttachable {

    public static final int MAX_MANA = 1000000;
    protected int mana;
    protected int knownMana = -1;
    protected int hudColor = -1;//= 0x4444FF;

    public static int calculateComparatorLevel(int mana, int max) {
        int val = (int) ((double) mana / (double) max * 15.0);
        if (mana > 0)
            val = Math.max(val, 1);
        return val;
    }

    public static double CIE_1976(float[] a, float[] b) {
        return Math.sqrt(Math.pow(Math.abs(b[0] - a[0]), 2) + Math.pow(Math.abs(b[1] - a[1]), 2) + Math.pow(Math.abs(b[2] - a[2]), 2)) / 173.2;
    }


    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        if (oldState.getBlock() != newState.getBlock())
            return true;
        if (oldState.getBlock() != BlocksMM.blockManaProvider || newState.getBlock() != BlocksMM.blockManaProvider)
            return true;
        return oldState.getValue(BlockSparkManaHatch.VARIANT) != newState.getValue(BlockSparkManaHatch.VARIANT);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound compound) {
        if (compound.hasKey(NAME_MANA))
            mana = compound.getInteger(NAME_MANA);
    }

    @Override
    public NBTTagCompound writeRestorableToNBT(NBTTagCompound compound) {
        compound.setInteger(NAME_MANA, mana);
        return compound;
    }

    @Override
    public boolean canAttachSpark(ItemStack stack) {
        return true;
    }

    @Override
    public void attachSpark(ISparkEntity entity) {

    }

    @Override
    public int getAvailableSpaceForMana() {
        int space = Math.max(0, MAX_MANA - getCurrentMana());
        if (space > 0)
            return space;
        else
            return 0;
    }

    @Override
    public ISparkEntity getAttachedSpark() {
        List<Entity> sparks = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos.up(), pos.up().add(1, 1, 1)), Predicates.instanceOf(ISparkEntity.class));
        if (sparks.size() == 1) {
            Entity e = sparks.get(0);
            return (ISparkEntity) e;
        }
        return null;
    }

    @Override
    public boolean areIncomingTranfersDone() {
        return false;
    }

    @Override
    public boolean isFull() {
        return this.mana >= MAX_MANA;
    }

    @Override
    public void recieveMana(int mana) {
        int old = this.mana;
        this.mana = Math.max(0, Math.min(getCurrentMana() + mana, MAX_MANA));
        if (old != this.mana) {
            world.updateComparatorOutputLevel(pos, world.getBlockState(pos).getBlock());
        }
    }


    @Override
    public int getCurrentMana() {
        return mana;
    }

    //法杖右键，将魔力数据发送给客户端
    public void onWanded(EntityPlayer player, ItemStack wand) {
        if (player == null)
            return;
        if (!world.isRemote) {

            if (player instanceof EntityPlayerMP)
                syncMana((EntityPlayerMP) player);
        }

        world.playSound(null, player.posX, player.posY, player.posZ, ModSounds.ding, SoundCategory.PLAYERS, 0.11F, 1F);
    }


    //显示魔力
    @SideOnly(Side.CLIENT)
    public void renderHUD(Minecraft mc, ScaledResolution res) {
        ItemStack hatch = new ItemStack(BlocksMM.blockManaProvider, 1, world.getBlockState(getPos()).getValue(BlockSparkManaHatch.VARIANT).ordinal());

        if (hudColor < 0) {
            float[] xyzColorMachine = new float[3];
            float[] bgColor = {80f, 0f, 0f};
            Color color = new Color(machineColor);


            color.getColorComponents(CIELab.getInstance(), xyzColorMachine);


            double diff = CIE_1976(xyzColorMachine, bgColor);

            if (diff >= 0.2) {
                this.hudColor = machineColor;
            } else {
                this.hudColor = 0x4444FF;
            }
        }
        HUDHandler.drawSimpleManaHUD(hudColor, knownMana, MAX_MANA, hatch.getDisplayName(), res);

    }

    public void sync(int mana) {
        this.knownMana = mana;
    }

    public void syncMana(EntityPlayerMP player) {
        ModularMachinery.NET_CHANNEL.sendTo(new PacketMana(getPos(), mana), player);
    }
}
