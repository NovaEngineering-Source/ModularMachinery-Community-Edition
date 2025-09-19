package kport.modularmagic.common.crafting.helper;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.tiles.essentia.TileJarFillable;

public class AspectProviderCopy {
    private final TileJarFillable original;

    private Aspect aspect;
    private int    amount;

    public AspectProviderCopy(TileJarFillable jar) {
        this.original = jar;
        this.aspect = jar.aspect;
        this.amount = jar.amount;
    }

    public boolean takeFromContainer(Aspect tt, int am) {
        if (this.amount >= am && tt == this.aspect) {
            this.amount -= am;
            if (this.amount <= 0) {
                this.aspect = null;
                this.amount = 0;
            }
            return true;
        } else {
            return false;
        }
    }

    public int addToContainer(Aspect tt, int am) {
        if (am != 0) {
            if (this.amount < 250 && tt == this.aspect || this.amount == 0) {
                this.aspect = tt;
                int added = Math.min(am, 250 - this.amount);
                this.amount += added;
                am -= added;
            }
        }
        return am;
    }

    public TileJarFillable getOriginal() {
        return original;
    }

    public Aspect getAspect() {
        return aspect;
    }

    public AspectProviderCopy setAspect(Aspect aspect) {
        this.aspect = aspect;
        return this;
    }

    public int getAmount() {
        return amount;
    }

    public AspectProviderCopy setAmount(int amount) {
        this.amount = amount;
        return this;
    }
}
