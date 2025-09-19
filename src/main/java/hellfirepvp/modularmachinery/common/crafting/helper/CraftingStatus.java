package hellfirepvp.modularmachinery.common.crafting.helper;

import hellfirepvp.modularmachinery.common.tiles.TileMachineController;
import hellfirepvp.modularmachinery.common.tiles.base.TileMultiblockMachineController;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Objects;

public class CraftingStatus {

    public static final CraftingStatus             SUCCESS           = new CraftingStatus(TileMultiblockMachineController.Type.CRAFTING, "");
    public static final CraftingStatus             MISSING_STRUCTURE = new CraftingStatus(TileMultiblockMachineController.Type.MISSING_STRUCTURE, "");
    public static final CraftingStatus             CHUNK_UNLOADED    = new CraftingStatus(TileMultiblockMachineController.Type.CHUNK_UNLOADED, "");
    public static final CraftingStatus             IDLE              = new CraftingStatus(TileMultiblockMachineController.Type.IDLE, "");
    private final       TileMachineController.Type status;
    private             String                     unlocalizedMessage;

    public CraftingStatus(TileMachineController.Type status, String unlocalizedMessage) {
        this.status = status;
        this.unlocalizedMessage = unlocalizedMessage;
    }

    public static CraftingStatus working() {
        return SUCCESS;
    }

    public static CraftingStatus working(String unlocMessage) {
        return new CraftingStatus(TileMultiblockMachineController.Type.CRAFTING, unlocMessage);
    }

    public static CraftingStatus failure(String unlocMessage) {
        return new CraftingStatus(TileMultiblockMachineController.Type.NO_RECIPE, unlocMessage);
    }

    public static CraftingStatus deserialize(NBTTagCompound tag) {
        TileMachineController.Type type = TileMultiblockMachineController.Type.values()[tag.getByte("type")];
        String unlocMessage = tag.getString("message");
        return new CraftingStatus(type, unlocMessage);
    }

    public TileMachineController.Type getStatus() {
        return status;
    }

    public String getUnlocMessage() {
        return !unlocalizedMessage.isEmpty() ? unlocalizedMessage : this.status.getUnlocalizedDescription();
    }

    public void overrideStatusMessage(String unlocalizedMessage) {
        this.unlocalizedMessage = unlocalizedMessage;
    }

    public boolean isCrafting() {
        return this.status == TileMultiblockMachineController.Type.CRAFTING;
    }

    public NBTTagCompound serialize() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setByte("type", (byte) this.status.ordinal());
        tag.setString("message", this.unlocalizedMessage);
        return tag;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof final CraftingStatus another)) {
            return false;
        }
        if (status != another.status) {
            return false;
        }
        return unlocalizedMessage.equals(another.unlocalizedMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, unlocalizedMessage);
    }
}
