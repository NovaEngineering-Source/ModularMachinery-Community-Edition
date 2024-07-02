/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.util;

import com.github.bsideup.jabel.Desugar;
import com.google.gson.JsonParseException;
import crafttweaker.annotations.ZenRegister;
import github.kasuminova.mmce.common.helper.AdvancedBlockChecker;
import github.kasuminova.mmce.common.machine.pattern.SpecialItemBlockProxy;
import github.kasuminova.mmce.common.machine.pattern.SpecialItemBlockProxyRegistry;
import hellfirepvp.modularmachinery.client.ClientScheduler;
import hellfirepvp.modularmachinery.common.block.BlockStatedMachineComponent;
import hellfirepvp.modularmachinery.common.util.nbt.NBTJsonSerializer;
import hellfirepvp.modularmachinery.common.util.nbt.NBTMatchingHelper;
import ink.ikx.mmce.common.utils.StackUtils;
import ink.ikx.mmce.common.utils.StructureIngredient;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import stanhebben.zenscript.annotations.ZenClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockArray
 * Created by HellFirePvP
 * Date: 27.06.2017 / 10:50
 */
@ZenRegister
@ZenClass("mods.modularmachinery.BlockArray")
public class BlockArray {
    private static final ResourceLocation IC_2_TILE_BLOCK = new ResourceLocation("ic2", "te");

    public final long traitNum;

    protected Map<BlockPos, BlockInformation> pattern = new HashMap<>();
    protected Map<BlockPos, BlockInformation> tileBlocksArray = new HashMap<>();
    private BlockPos min = new BlockPos(0, 0, 0), max = new BlockPos(0, 0, 0), size = new BlockPos(0, 0, 0);

    public BlockArray() {
        this.traitNum = BlockArrayCache.nextTraitNum();
    }

    public BlockArray(long traitNum) {
        this.traitNum = traitNum;
    }

    public BlockArray(BlockArray other) {
        this.pattern = new HashMap<>(other.pattern);
        this.min = new BlockPos(other.min.getX(), other.min.getY(), other.min.getZ());
        this.max = new BlockPos(other.max.getX(), other.max.getY(), other.max.getZ());
        this.size = new BlockPos(other.size.getX(), other.size.getY(), other.size.getZ());

        this.traitNum = other.traitNum;
    }

    public BlockArray(BlockArray other, BlockPos offset) {
        for (Map.Entry<BlockPos, BlockInformation> otherEntry : other.pattern.entrySet()) {
            this.pattern.put(otherEntry.getKey().add(offset), otherEntry.getValue());
        }
        this.min = new BlockPos(offset.getX() + other.min.getX(), offset.getY() + other.min.getY(), offset.getZ() + other.min.getZ());
        this.max = new BlockPos(offset.getX() + other.max.getX(), offset.getY() + other.max.getY(), offset.getZ() + other.max.getZ());
        this.size = new BlockPos(other.size.getX(), other.size.getY(), other.size.getZ());

        this.traitNum = other.traitNum;
    }

    public StructureBoundingBox getPatternBoundingBox(final BlockPos ctrlPos) {
        BlockPos min = ctrlPos.add(this.min);
        BlockPos max = ctrlPos.add(this.max);
        return new StructureBoundingBox(min, max);
    }

    public void flushTileBlocksCache() {
        tileBlocksArray.clear();
        pattern.forEach((pos, info) -> {
            if (info.hasTileEntity()) {
                tileBlocksArray.put(pos, info);
            }
        });
    }

    public void overwrite(BlockArray other) {
        this.pattern = new HashMap<>(other.pattern);
        this.min = new BlockPos(other.min.getX(), other.min.getY(), other.min.getZ());
        this.max = new BlockPos(other.max.getX(), other.max.getY(), other.max.getZ());
        this.size = new BlockPos(other.size.getX(), other.size.getY(), other.size.getZ());
    }

    public void addBlock(int x, int y, int z, @Nonnull BlockInformation info) {
        addBlock(new BlockPos(x, y, z), info);
    }

    public void addBlock(BlockPos offset, @Nonnull BlockInformation info) {
        pattern.put(offset, info);
        updateSize(offset);
    }

    public boolean hasBlockAt(BlockPos pos) {
        return pattern.containsKey(pos);
    }

    public boolean isEmpty() {
        return pattern.isEmpty();
    }

    public BlockPos getMax() {
        return max;
    }

    public BlockPos getMin() {
        return min;
    }

    public BlockPos getSize() {
        return size;
    }

    private void updateSize(BlockPos addedPos) {
        if (addedPos.getX() < min.getX()) {
            min = new BlockPos(addedPos.getX(), min.getY(), min.getZ());
        }
        if (addedPos.getX() > max.getX()) {
            max = new BlockPos(addedPos.getX(), max.getY(), max.getZ());
        }
        if (addedPos.getY() < min.getY()) {
            min = new BlockPos(min.getX(), addedPos.getY(), min.getZ());
        }
        if (addedPos.getY() > max.getY()) {
            max = new BlockPos(max.getX(), addedPos.getY(), max.getZ());
        }
        if (addedPos.getZ() < min.getZ()) {
            min = new BlockPos(min.getX(), min.getY(), addedPos.getZ());
        }
        if (addedPos.getZ() > max.getZ()) {
            max = new BlockPos(max.getX(), max.getY(), addedPos.getZ());
        }
        size = new BlockPos(max.getX() - min.getX() + 1, max.getY() - min.getY() + 1, max.getZ() - min.getZ() + 1);
    }

    public Map<BlockPos, BlockInformation> getPattern() {
        return pattern;
    }

    public Map<BlockPos, BlockInformation> getTileBlocksArray() {
        return tileBlocksArray;
    }

    public Map<BlockPos, BlockInformation> getPatternSlice(int slice) {
        Map<BlockPos, BlockInformation> copy = new HashMap<>();
        for (BlockPos pos : pattern.keySet()) {
            if (pos.getY() == slice) {
                copy.put(pos, pattern.get(pos));
            }
        }
        return copy;
    }

    @SideOnly(Side.CLIENT)
    public List<ItemStack> getAsDescriptiveStacks() {
        return getAsDescriptiveStacks(-1);
    }

    @SideOnly(Side.CLIENT)
    public List<ItemStack> getAsDescriptiveStacks(long snapSample) {
        List<ItemStack> out = new LinkedList<>();
        pattern.forEach((key, bi) -> {
            ItemStack s = bi.getDescriptiveStack(snapSample);
            if (s.isEmpty()) {
                return;
            }

            boolean found = false;
            for (ItemStack stack : out) {
                if (stack.getItem().getRegistryName().equals(s.getItem().getRegistryName()) && stack.getItemDamage() == s.getItemDamage()) {
                    stack.setCount(stack.getCount() + 1);
                    found = true;
                    break;
                }
            }
            if (!found) {
                out.add(s);
            }
        });
        return out;
    }

    public List<List<ItemStack>> getIngredientList() {
        List<List<ItemStack>> ingredient = new LinkedList<>();

        pattern.forEach((pos, info) -> {
            List<ItemStack> infoIngList = info.getIngredientList();
            if (infoIngList.isEmpty()) {
                return;
            }

            if (infoIngList.size() == 1) {
                ItemStack input = infoIngList.get(0);

                for (final List<ItemStack> itemStackList : ingredient) {
                    ItemStack anotherInput = itemStackList.get(0);
                    if (ItemUtils.matchStacks(input, anotherInput)) {
                        anotherInput.grow(1);
                        return;
                    }
                }
            }

            ingredient.add(infoIngList);
        });

        return ingredient;
    }

    public List<StructureIngredient.ItemIngredient> getBlockStateIngredientList(World world, BlockPos ctrlPos) {
        List<StructureIngredient.ItemIngredient> ingredientList = new ArrayList<>();
        pattern.forEach((pos, info) -> {
            BlockPos realPos = ctrlPos.add(pos);
            if (!info.matches(world, realPos, false)) {
                ingredientList.add(new StructureIngredient.ItemIngredient(pos, info.getBlockStateIngredientList(), info.getMatchingTag()));
            }
        });
        return ingredientList;
    }

    public List<ItemStack> getDescriptiveStackList(long snapTick) {
        List<ItemStack> stackList = new ArrayList<>();

        pattern.values().forEach((info) -> {
            ItemStack descriptiveStack = info.getDescriptiveStack(snapTick);
            if (descriptiveStack.isEmpty()) {
                return;
            }

            for (final ItemStack stack : stackList) {
                if (ItemUtils.matchStacks(descriptiveStack, stack)) {
                    stack.grow(1);
                    return;
                }
            }

            stackList.add(descriptiveStack);
        });

        return stackList;
    }

    public List<ItemStack> getDescriptiveStackList(long snapTick, World world, BlockPos offset) {
        List<ItemStack> stackList = new ArrayList<>();

        pattern.forEach((pos, info) -> {
            BlockPos realPos = pos.add(offset);
            ItemStack descriptiveStack = info.getDescriptiveStack(snapTick, realPos, world);
            SpecialItemBlockProxy specialItemBlockProxy = SpecialItemBlockProxyRegistry.INSTANCE.getValidProxy(descriptiveStack);
            if (specialItemBlockProxy != null) {
                descriptiveStack = specialItemBlockProxy.getTrueStack(world.getBlockState(realPos), world.getTileEntity(realPos));
            }
            if (descriptiveStack.isEmpty()) {
                return;
            }

            for (final ItemStack stack : stackList) {
                if (ItemUtils.matchStacks(descriptiveStack, stack)) {
                    stack.grow(1);
                    return;
                }
            }

            stackList.add(descriptiveStack);
        });

        return stackList;
    }

    public boolean matches(World world, BlockPos center, boolean oldState, @Nullable Map<BlockPos, List<BlockInformation>> modifierReplacementPattern) {
//        if (pattern.size() >= 1500) {
//            return matchesParallel(world, center, oldState, modifierReplacementPattern);
//        }

        patternCheck:
        for (Map.Entry<BlockPos, BlockInformation> entry : pattern.entrySet()) {
            BlockPos at = center.add(entry.getKey());
            // Block is matched, continue.
            if (entry.getValue().matches(world, at, oldState)) {
                continue;
            }

            // Block is not match, and there are no replaceable blocks in the configuration, end check.
            if (modifierReplacementPattern == null || !modifierReplacementPattern.containsKey(entry.getKey())) {
                return false;
            }

            // Check if the replacement block match.
            for (BlockInformation info : modifierReplacementPattern.get(entry.getKey())) {
                if (info.matches(world, at, oldState)) {
                    continue patternCheck;
                }
            }

            // All the above conditions are not valid, and the check fails.
            return false;
        }
        return true;
    }

    public boolean matchesParallel(World world, BlockPos center, boolean oldState, @Nullable Map<BlockPos, List<BlockInformation>> modifierReplacementPattern) {
        return pattern.entrySet().parallelStream().allMatch(entry -> {
            BlockPos at = center.add(entry.getKey());
            // Block is matched, continue.
            if (entry.getValue().matches(world, at, oldState)) {
                return true;
            }

            // Block is not match, and there are no replaceable blocks in the configuration, end check.
            if (modifierReplacementPattern == null || !modifierReplacementPattern.containsKey(entry.getKey())) {
                return false;
            }

            // Check if the replacement block match.
            for (BlockInformation info : modifierReplacementPattern.get(entry.getKey())) {
                if (info.matches(world, at, oldState)) {
                    return true;
                }
            }

            // All the above conditions are not valid, and the check fails.
            return false;
        });
    }

    public BlockPos getRelativeMismatchPosition(World world, BlockPos center, @Nullable Map<BlockPos, List<BlockInformation>> modifierReplacementPattern) {
        pattern:
        for (Map.Entry<BlockPos, BlockInformation> entry : pattern.entrySet()) {
            BlockPos at = center.add(entry.getKey());
            if (entry.getValue().matches(world, at, false)) {
                continue;
            }

            if (modifierReplacementPattern == null || !modifierReplacementPattern.containsKey(entry.getKey())) {
                return entry.getKey();
            }

            for (BlockInformation info : modifierReplacementPattern.get(entry.getKey())) {
                if (info.matches(world, at, false)) {
                    continue pattern;
                }
            }

            return entry.getKey();
        }
        return null;
    }

    public BlockArray rotateYCCW() {
        BlockArray out = new BlockArray(traitNum);
        Map<BlockPos, BlockInformation> outPattern = out.pattern;

        for (BlockPos pos : pattern.keySet()) {
            BlockInformation info = pattern.get(pos);
            out.addBlock(MiscUtils.rotateYCCW(pos), info.copyRotateYCCW());
        }

        return out;
    }

    public BlockArray rotateUp() {
        BlockArray out = new BlockArray(traitNum);
        Map<BlockPos, BlockInformation> outPattern = out.pattern;

        for (BlockPos pos : pattern.keySet()) {
            BlockInformation info = pattern.get(pos);
            outPattern.put(MiscUtils.rotateUp(pos), info.copy());
        }

        return out;
    }

    public BlockArray rotateDown() {
        BlockArray out = new BlockArray(traitNum);
        Map<BlockPos, BlockInformation> outPattern = out.pattern;

        for (BlockPos pos : pattern.keySet()) {
            BlockInformation info = pattern.get(pos);
            outPattern.put(MiscUtils.rotateDown(pos), info.copy());
        }

        return out;
    }

    public String serializeAsMachineJson() {
        String newline = System.getProperty("line.separator");
        String move = "    ";

        StringBuilder sb = new StringBuilder();
        sb.append("{").append(newline);
        sb.append(move).append("\"parts\": [").append(newline);

        Iterator<BlockPos> iterator = this.pattern.keySet().iterator();
        while (iterator.hasNext()) {
            BlockPos pos = iterator.next();
            sb.append(move).append(move).append("{").append(newline);

            sb.append(move).append(move).append(move).append("\"x\": ").append(pos.getX()).append(",").append(newline);
            sb.append(move).append(move).append(move).append("\"y\": ").append(pos.getY()).append(",").append(newline);
            sb.append(move).append(move).append(move).append("\"z\": ").append(pos.getZ()).append(",").append(newline);

            BlockInformation bi = this.pattern.get(pos);
            if (bi.getMatchingTag() != null) {
                String strTag = NBTJsonSerializer.serializeNBT(bi.getMatchingTag());
                sb.append(move).append(move).append(move).append("\"nbt\": ").append(strTag).append(",").append(newline);
            }

            sb.append(move).append(move).append(move).append("\"elements\": [").append(newline);
            for (Iterator<IBlockState> iterator1 = bi.samples.iterator(); iterator1.hasNext(); ) {
                IBlockState descriptor = iterator1.next();

                int meta = descriptor.getBlock().getMetaFromState(descriptor);
                String str = descriptor.getBlock().getRegistryName().toString() + "@" + meta;
                sb.append(move).append(move).append(move).append(move).append("\"").append(str).append("\"");

                if (iterator1.hasNext()) {
                    sb.append(",");
                }
                sb.append(newline);
            }

            sb.append(move).append(move).append(move).append("]").append(newline);
            sb.append(move).append(move).append("}");
            if (iterator.hasNext()) {
                sb.append(",");
            }
            sb.append(newline);
        }

        sb.append(move).append("]");
        sb.append("}");
        return sb.toString();
    }

    public static class BlockInformation {

        public static final int CYCLE_TICK_SPEED = 30;
        public List<IBlockStateDescriptor> matchingStates = new ArrayList<>();

        private List<IBlockState> samples = new ArrayList<>();

        private boolean hasTileEntity;
        protected NBTTagCompound matchingTag = null;
        protected NBTTagCompound previewTag = null;
        public AdvancedBlockChecker nbtChecker = null;

        public BlockInformation(List<IBlockStateDescriptor> matching) {
            this.matchingStates.addAll(matching);
            for (IBlockStateDescriptor desc : matchingStates) {
                samples.addAll(desc.applicable);
                if (!hasTileEntity) {
                    hasTileEntity = hasTileEntity(desc.applicable);
                }
            }
        }

        public void addMatchingStates(List<IBlockStateDescriptor> matching) {
            for (IBlockStateDescriptor desc : matching) {
                if (!matchingStates.contains(desc)) {
                    matchingStates.add(desc);
                }
                for (IBlockState state : desc.applicable) {
                    if (!samples.contains(state)) {
                        samples.add(state);
                    }
                }
                if (!hasTileEntity) {
                    hasTileEntity = hasTileEntity(desc.applicable);
                }
            }
        }

        public boolean hasTileEntity() {
            return hasTileEntity;
        }

        public boolean hasStatedMachineComponent() {
            for (final IBlockStateDescriptor matchingState : matchingStates) {
                for (final IBlockState state : matchingState.applicable) {
                    if (state.getBlock() instanceof BlockStatedMachineComponent) {
                        return true;
                    }
                }
            }
            return false;
        }

        private static boolean hasTileEntity(List<IBlockState> matching) {
            for (IBlockState state : matching) {
                if (state.getBlock().hasTileEntity(state)) {
                    return true;
                }
            }
            return false;
        }

        public static IBlockStateDescriptor getDescriptor(String strElement) throws JsonParseException {
            int meta = -1;
            int indexMeta = strElement.indexOf('@');
            if (indexMeta != -1 && indexMeta != strElement.length() - 1) {
                try {
                    meta = Integer.parseInt(strElement.substring(indexMeta + 1));
                } catch (NumberFormatException exc) {
                    throw new JsonParseException("Expected a metadata number, got " + strElement.substring(indexMeta + 1), exc);
                }
                strElement = strElement.substring(0, indexMeta);
            }
            ResourceLocation res = new ResourceLocation(strElement);
            Block block = ForgeRegistries.BLOCKS.getValue(res);
            if (block == null) {
                throw new JsonParseException("Couldn't find block with registryName '" + res + "' !");
            }
            if (meta == -1) {
                return new IBlockStateDescriptor(block);
            } else {
                return new IBlockStateDescriptor(block.getStateFromMeta(meta));
            }
        }

        public NBTTagCompound getMatchingTag() {
            return matchingTag;
        }

        public void setMatchingTag(@Nullable NBTTagCompound matchingTag) {
            this.matchingTag = matchingTag;
        }

        public NBTTagCompound getPreviewTag() {
            return previewTag;
        }

        public void setPreviewTag(NBTTagCompound previewTag) {
            this.previewTag = previewTag;
        }

        public IBlockState getSampleState() {
            return getSampleState(-1);
        }

        public IBlockState getSampleState(long snapTick) {
            int tickSpeed = CYCLE_TICK_SPEED;
//            if (samples.size() > 10) {
//                tickSpeed *= 0.6;
//            }
            int p = (int) ((snapTick == -1 ? ClientScheduler.getClientTick() : snapTick) / tickSpeed);
            int part = p % samples.size();
            return samples.get(part);
        }

        public ItemStack getDescriptiveStack(long snapTick) {
            return StackUtils.getStackFromBlockState(getSampleState(snapTick));
        }

        public ItemStack getDescriptiveStack(long snapTick, final BlockPos pos, final World world) {
            return StackUtils.getStackFromBlockState(getSampleState(snapTick), pos, world);
        }

        public List<ItemStack> getIngredientList() {
            List<ItemStack> list = new ArrayList<>();
            samples.stream()
                    .map(StackUtils::getStackFromBlockState)
                    .filter(stackFromBlockState -> ItemUtils.stackNotInList(list, stackFromBlockState))
                    .forEach(list::add);
            return list;
        }

        public List<ItemStack> getIngredientList(BlockPos pos, World world) {
            List<ItemStack> list = new ArrayList<>();
            samples.stream()
                    .map(state -> StackUtils.getStackFromBlockState(state, pos, world))
                    .filter(stackFromBlockState -> ItemUtils.stackNotInList(list, stackFromBlockState))
                    .forEach(list::add);
            return list;
        }

        public List<Tuple<ItemStack, IBlockState>> getBlockStateIngredientList() {
            return samples.stream()
                    .map(BlockInformation::getTupleIngredientFromBlockState)
                    .collect(Collectors.toList());
        }

        public static Tuple<ItemStack, IBlockState> getTupleIngredientFromBlockState(IBlockState state) {
            return new Tuple<>(StackUtils.getStackFromBlockState(state), state);
        }

        public BlockInformation copyRotateYCCW() {
            List<IBlockStateDescriptor> newDescriptors = new ArrayList<>();

            boolean noBlockCanRotated = true;
            for (IBlockStateDescriptor desc : this.matchingStates) {
                IBlockStateDescriptor copy = new IBlockStateDescriptor();
                for (IBlockState applicableState : desc.applicable) {
                    IBlockState rotated = applicableState.withRotation(Rotation.COUNTERCLOCKWISE_90);
                    if (rotated != applicableState) {
                        noBlockCanRotated = false;
                    }

                    copy.applicable.add(rotated);
                }
                newDescriptors.add(copy);
            }

            BlockInformation bi;
            if (noBlockCanRotated) {
                bi = new BlockInformation(Collections.emptyList());
                bi.matchingStates = this.matchingStates;
                bi.samples = this.samples;
                bi.hasTileEntity = this.hasTileEntity;
            } else {
                bi = new BlockInformation(newDescriptors);
            }

            if (this.getMatchingTag() != null) {
                bi.setMatchingTag(this.getMatchingTag());
            }
            return bi;
        }

        public BlockInformation copy() {
            List<IBlockStateDescriptor> descr = new ArrayList<>(this.matchingStates.size());
            for (IBlockStateDescriptor desc : this.matchingStates) {
                IBlockStateDescriptor copy = new IBlockStateDescriptor();
                copy.applicable.addAll(desc.applicable);
                descr.add(copy);
            }
            BlockInformation bi = new BlockInformation(descr);
            if (this.getMatchingTag() != null) {
                bi.setMatchingTag(this.getMatchingTag());
            }
            return bi;
        }

        public boolean matchesState(World world, BlockPos at, IBlockState state) {
            Block atBlock = state.getBlock();
            int atMeta = atBlock.getMetaFromState(state);

            for (IBlockStateDescriptor descriptor : matchingStates) {
                for (IBlockState applicable : descriptor.applicable) {
                    Block type = applicable.getBlock();
                    int meta = type.getMetaFromState(applicable);
                    if (!type.equals(atBlock) || meta != atMeta) {
                        continue;
                    }

                    if (!isNBTCheckerMatch(world, at, applicable)) return false;

                    if (getMatchingTag() != null) {
                        TileEntity te = world.getTileEntity(at);
                        if (te != null && getMatchingTag().getSize() > 0) {
                            NBTTagCompound cmp = new NBTTagCompound();
                            te.writeToNBT(cmp);
                            return NBTMatchingHelper.matchNBTCompound(getMatchingTag(), cmp); //No match at this position.
                        }
                    }
                    return true;
                }
            }
            return false;
        }

        public boolean matches(World world, BlockPos at, boolean default_) {
            if (!world.isBlockLoaded(at)) {
                return default_;
            }

            IBlockState state = world.getBlockState(at);
            return matchesState(world, at, state);
        }

        private boolean isNBTCheckerMatch(World world, BlockPos at, IBlockState applicable) {
            if (nbtChecker == null) {
                return true;
            }

            TileEntity te = world.getTileEntity(at);
            if (te == null) {
                return false;
            }

            NBTTagCompound cmp = new NBTTagCompound();
            te.writeToNBT(cmp);
            return nbtChecker.isMatch(world, at, applicable, cmp);
        }
    }

    @Desugar
    public record TileInstantiateContext(World world, BlockPos pos) {
        public void apply(TileEntity te) {
            if (te != null) {
                te.setWorld(world);
                te.setPos(pos);
            }
        }
    }
}
