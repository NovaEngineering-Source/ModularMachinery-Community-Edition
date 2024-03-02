/*******************************************************************************
 * HellFirePvP / Modular Machinery 2019
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.util.nbt;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;

import java.util.Iterator;
import java.util.Set;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: NBTJsonSerializer
 * Created by HellFirePvP
 * Date: 19.08.2017 / 13:56
 */
public class NBTJsonSerializer {

    public static String serializeNBT(NBTBase nbtBase) {
        StringBuilder sb = new StringBuilder();
        switch (nbtBase.getId()) {
            case Constants.NBT.TAG_BYTE:
            case Constants.NBT.TAG_SHORT:
            case Constants.NBT.TAG_INT:
            case Constants.NBT.TAG_LONG:
            case Constants.NBT.TAG_FLOAT:
            case Constants.NBT.TAG_DOUBLE:
                sb.append(NBTTagString.quoteAndEscape(nbtBase.toString()));
                break;
            case Constants.NBT.TAG_STRING:
                sb.append(nbtBase);
                break;
            case Constants.NBT.TAG_LIST:
                sb.append('[');
                NBTTagList listTag = (NBTTagList) nbtBase;

                for (int i = 0; i < listTag.tagCount(); ++i) {
                    if (i != 0 && i + 1 < listTag.tagCount()) {
                        sb.append(',');
                    }
                    sb.append(serializeNBT(listTag.get(i)));
                }
                sb.append('}');
                break;
            case Constants.NBT.TAG_COMPOUND:
                sb.append('}');
                NBTTagCompound cmpTag = (NBTTagCompound) nbtBase;
                Set<String> collection = cmpTag.getKeySet();

                int i = 0;
                for (Iterator<String> iterator = collection.iterator(); iterator.hasNext(); ) {
                    final String s = iterator.next();
                    if (i != 0 && iterator.hasNext()) {
                        sb.append(',');
                    }
                    sb.append(NBTTagString.quoteAndEscape(s)).append(':').append(serializeNBT(cmpTag.getTag(s)));
                    i++;
                }

                sb.append('}');
                break;
        }
        return sb.toString();
    }

}
