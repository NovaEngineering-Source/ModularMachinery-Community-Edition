package kport.gugu_utils.common.tile;

import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.crafting.ComponentType;
import hellfirepvp.modularmachinery.common.machine.MachineComponent;
import hellfirepvp.modularmachinery.common.tiles.base.MachineComponentTile;
import kport.gugu_utils.GuGuCompoments;
import kport.gugu_utils.common.components.GenericMachineCompoment;
import kport.gugu_utils.common.requirements.RequirementMana;
import kport.gugu_utils.common.requirements.basic.IGeneratable;
import kport.gugu_utils.network.PacketCollectorPostion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import vazkii.botania.api.mana.IManaCollector;
import vazkii.botania.api.wand.IWandBindable;

import javax.annotation.Nullable;

public class TileSparkManaOutputHatch extends TileSparkManaHatch
        implements MachineComponentTile, IGeneratable<RequirementMana.RT>, ITickable, IWandBindable {


    private static final String TAG_COLLECTOR_X = "collectorX";
    private static final String TAG_COLLECTOR_Y = "collectorY";
    private static final String TAG_COLLECTOR_Z = "collectorZ";
    private TileEntity linkedCollector;
    private BlockPos cachedCollectorCoordinates = null;
    private int tickExisted = 0;

    @Override
    public boolean canRecieveManaFromBursts() {
        return false;
    }

    @Override
    public int getAvailableSpaceForMana() {
        return 0;
    }


    @Override
    public boolean areIncomingTranfersDone() {
        return true;
    }

    @Nullable
    @Override
    public MachineComponent provideComponent() {
        return new GenericMachineCompoment<>(this, (ComponentType) GuGuCompoments.COMPONENT_MANA);
    }

    @Override
    public boolean generate(RequirementMana.RT outputToken, boolean doOperation) {
        int manaAdded = Math.min(outputToken.getMana(), MAX_MANA - this.mana);
        outputToken.setMana(outputToken.getMana() - manaAdded);

        if (doOperation)
            this.mana += manaAdded;
        return manaAdded > 0;
    }

    @Override
    public void update() {
        //绑定魔力发射器



        tickExisted++;
        if (getLinkedCollector() == null) {
            if (cachedCollectorCoordinates != null && world.isBlockLoaded(cachedCollectorCoordinates)) {
                TileEntity tileAt = world.getTileEntity(cachedCollectorCoordinates);
                if (tileAt instanceof IManaCollector && !tileAt.isInvalid()) {
                    linkedCollector = tileAt;
                }
                cachedCollectorCoordinates = null;
            }
        } else {
            TileEntity tileAt = world.getTileEntity(getLinkedCollector().getPos());
            if (getLinkedCollector().isInvalid() || tileAt == null)
                linkedCollector = null;
            else if (tileAt instanceof IManaCollector)
                linkedCollector = tileAt;
        }

        if (world.isRemote) {
            return;
        }
        //搜索附近的魔力发射器
        if (tickExisted % 20 == 0)
            if (!isValidBinding() && cachedCollectorCoordinates == null) {
                for (EnumFacing dir : EnumFacing.VALUES) {
                    TileEntity tileAt = world.getTileEntity(pos.offset(dir));
                    if (tileAt instanceof IManaCollector) {
                        setLinkedCollector(tileAt);
                    }
                }
            }

        if (getLinkedCollector() != null && world.isBlockLoaded(getLinkedCollector().getPos())) {
            IManaCollector collector = (IManaCollector) getLinkedCollector();
            if (!collector.isFull() && mana > 0) {
                int manaval = Math.min(mana, collector.getMaxMana() - collector.getCurrentMana());
                mana -= manaval;
                collector.recieveMana(manaval);
            }
        }


    }

    public void sync() {
//        VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
        ModularMachinery.NET_CHANNEL.sendToAllAround(
                new PacketCollectorPostion(this.getPos(), this.isValidBinding() ? this.getLinkedCollector().getPos() : null),
                new NetworkRegistry.TargetPoint(world.provider.getDimension(),
                        getPos().getX(), getPos().getY(), getPos().getZ(),
                        32));
    }


    public void sync(BlockPos destCollector) {
        cachedCollectorCoordinates = destCollector;
    }


    @Override
    public boolean canSelect(EntityPlayer player, ItemStack wand, BlockPos pos, EnumFacing side) {
        return true;
    }

    @Override
    public boolean bindTo(EntityPlayer player, ItemStack wand, BlockPos pos, EnumFacing side) {
        int range = 6;
        range *= range;

        double dist = pos.distanceSq(getPos());
        if (range >= dist) {
            TileEntity tile = player.world.getTileEntity(pos);
            if (tile instanceof IManaCollector) {
                setLinkedCollector(tile);
                return true;
            }
        }

        return false;
    }

    @Override
    public BlockPos getBinding() {
        if (getLinkedCollector() == null)
            return null;
        return getLinkedCollector().getPos();
    }

    @Override
    public void readNBT(NBTTagCompound compound) {
        super.readNBT(compound);
        int x = compound.getInteger(TAG_COLLECTOR_X);
        int y = compound.getInteger(TAG_COLLECTOR_Y);
        int z = compound.getInteger(TAG_COLLECTOR_Z);
        cachedCollectorCoordinates = y < 0 ? null : new BlockPos(x, y, z);
    }


    @Override
    public void writeNBT(NBTTagCompound compound) {
        super.writeNBT(compound);
        int x = getLinkedCollector() == null ? 0 : getLinkedCollector().getPos().getX();
        int y = getLinkedCollector() == null ? -1 : getLinkedCollector().getPos().getY();
        int z = getLinkedCollector() == null ? 0 : getLinkedCollector().getPos().getZ();

        compound.setInteger(TAG_COLLECTOR_X, x);
        compound.setInteger(TAG_COLLECTOR_Y, y);
        compound.setInteger(TAG_COLLECTOR_Z, z);

    }

    public boolean isValidBinding() {
        return getLinkedCollector() != null && !getLinkedCollector().isInvalid() && world.getTileEntity(getLinkedCollector().getPos()) == getLinkedCollector();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderHUD(Minecraft mc, ScaledResolution res) {

        super.renderHUD(mc, res);
        if (isValidBinding()) {
            ItemStack recieverStack = new ItemStack(world.getBlockState(getLinkedCollector().getPos()).getBlock(), 1, getLinkedCollector().getBlockMetadata());

            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            if (!recieverStack.isEmpty()) {
                String stackName = recieverStack.getDisplayName();
                int width = 16 + mc.fontRenderer.getStringWidth(stackName) / 2;
                int x = res.getScaledWidth() / 2 - width;
                int y = res.getScaledHeight() / 2 + 30;

                mc.fontRenderer.drawStringWithShadow(stackName, x + 20, y + 5, hudColor);
                RenderHelper.enableGUIStandardItemLighting();
                mc.getRenderItem().renderItemAndEffectIntoGUI(recieverStack, x, y);
                RenderHelper.disableStandardItemLighting();


                GlStateManager.disableLighting();
                GlStateManager.disableBlend();

            }

        }
    }


    @Override
    public void onWanded(EntityPlayer player, ItemStack wand) {
        if (player == null)
            return;

        if (!player.world.isRemote)
            sync();

        super.onWanded(player, wand);
    }

    public TileEntity getLinkedCollector() {
        return linkedCollector;
    }

    public void setLinkedCollector(TileEntity linkedCollector) {
        if (this.linkedCollector != linkedCollector) {
            this.linkedCollector = linkedCollector;
            if (!world.isRemote)
                sync();
        }
    }
}
