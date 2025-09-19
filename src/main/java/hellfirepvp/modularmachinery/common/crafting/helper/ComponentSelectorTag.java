/*******************************************************************************
 * HellFirePvP / Modular Machinery 2018
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/ModularMachinery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.modularmachinery.common.crafting.helper;

import com.github.bsideup.jabel.Desugar;

import javax.annotation.Nonnull;

/**
 * This class is part of the Modular Machinery Mod
 * The complete source code for this mod can be found on github.
 * Class: ComponentSelectorTag
 * Created by HellFirePvP
 * Date: 04.03.2019 / 21:31
 */
// Basically a super fancy wrapped string.
@Desugar
public record ComponentSelectorTag(String tag) {

    public ComponentSelectorTag {
        if (tag == null || tag.isEmpty()) {
            throw new IllegalArgumentException("Tried to create tag object will null or empty tag!");
        }
    }

    @Nonnull
    public String getTag() {
        return tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ComponentSelectorTag another = (ComponentSelectorTag) o;
        return tag.equals(another.tag);
    }

    @Override
    public int hashCode() {
        return tag.hashCode();
    }
}
