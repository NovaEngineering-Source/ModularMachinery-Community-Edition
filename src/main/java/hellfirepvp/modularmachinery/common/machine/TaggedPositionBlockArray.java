/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.machine;

import crafttweaker.annotations.ZenRegister;
import github.kasuminova.mmce.common.util.BlockPos2ValueMap;
import hellfirepvp.modularmachinery.common.crafting.helper.ComponentSelectorTag;
import hellfirepvp.modularmachinery.common.util.BlockArray;
import hellfirepvp.modularmachinery.common.util.MiscUtils;
import net.minecraft.util.math.BlockPos;
import stanhebben.zenscript.annotations.ZenClass;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: TaggedPositionBlockArray
 * Created by HellFirePvP
 * Date: 04.03.2019 / 21:33
 */
@ZenRegister
@ZenClass("mods.modularmachinery.TaggedPositionBlockArray")
public class TaggedPositionBlockArray extends BlockArray {
    private Map<BlockPos, ComponentSelectorTag> taggedPositions = new BlockPos2ValueMap<>();

    public TaggedPositionBlockArray() {
    }

    public TaggedPositionBlockArray(long uid) {
        super(uid);
    }

    public TaggedPositionBlockArray(TaggedPositionBlockArray another) {
        super(another);
        taggedPositions.putAll(another.taggedPositions);
    }

    public void setTag(BlockPos pos, ComponentSelectorTag tag) {
        this.taggedPositions.put(pos, tag);
    }

    @Nullable
    public ComponentSelectorTag getTag(BlockPos pos) {
        return this.taggedPositions.get(pos);
    }

    public Map<BlockPos, ComponentSelectorTag> getTaggedPositions() {
        return taggedPositions;
    }

    @Override
    public void overwrite(final BlockArray other) {
        super.overwrite(other);
        if (other instanceof TaggedPositionBlockArray) {
            this.taggedPositions = new BlockPos2ValueMap<>(((TaggedPositionBlockArray) other).taggedPositions);
        }
    }

    @Override
    public TaggedPositionBlockArray rotateYCCW() {
        TaggedPositionBlockArray out = new TaggedPositionBlockArray(uid);

        for (Map.Entry<BlockPos, BlockInformation> entry : pattern.entrySet()) {
            out.addBlock(MiscUtils.rotateYCCW(entry.getKey()), entry.getValue().copyRotateYCCW());
        }

        for (final Map.Entry<BlockPos, ComponentSelectorTag> entry : taggedPositions.entrySet()) {
            out.taggedPositions.put(MiscUtils.rotateYCCW(entry.getKey()), entry.getValue());
        }

        return out;
    }

}
