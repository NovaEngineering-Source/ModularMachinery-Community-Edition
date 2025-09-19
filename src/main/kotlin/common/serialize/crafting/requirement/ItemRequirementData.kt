package github.kasuminova.mmce.common.serialize.crafting.requirement

import github.kasuminova.mmce.common.serialize.DataStructure
import github.kasuminova.mmce.common.serialize.getValue
import hellfirepvp.modularmachinery.common.util.nbt.NBTJsonDeserializer
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.registry.ForgeRegistries

class ItemRequirementData : DataStructure() {

    val item by string("item")
        .map { str ->
            val split = str.split(":")
            when (split.size) {
                1 -> Either(oreDict = split[0])
                2 -> {
                    if (split[0] == "ore") {
                        Either(oreDict = split[1])
                    } else {
                        val resLoc = ResourceLocation(split[0], split[1])
                        val item = ForgeRegistries.ITEMS.getValue(resLoc) ?: error("Item $resLoc not found")
                        val meta = split[1].split("@").getOrNull(1)?.toIntOrNull()
                        if (meta != null) {
                            Either(ItemStack(item, 1, meta))
                        } else {
                            Either(ItemStack(item))
                        }
                    }
                }

                else -> error("Invalid item string: $str")
            }
        }

    val amount by integer("amount")
        .def { 1 }

    val chance by float("chance")
        .def { 1.0f }

    val fuelTime by integer("time")
        .def { -1 }

    val nbt by string("nbt")
        .map { nbtStr -> if (nbtStr.isBlank()) null else NBTJsonDeserializer.deserialize(nbtStr) }

    val nbtDisplay by string("nbt-display")
        .map { nbtStr -> if (nbtStr.isBlank()) null else NBTJsonDeserializer.deserialize(nbtStr) }

    data class Either(val stack: ItemStack? = null, val oreDict: String? = null, val fuelTime: Int? = null)

}