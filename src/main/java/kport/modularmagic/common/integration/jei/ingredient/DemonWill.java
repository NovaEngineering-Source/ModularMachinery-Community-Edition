package kport.modularmagic.common.integration.jei.ingredient;

import WayofTime.bloodmagic.soul.EnumDemonWillType;

public class DemonWill {

    private EnumDemonWillType willType;
    private double            willAmount;

    public DemonWill(EnumDemonWillType willType, double willAmount) {
        this.willType = willType;
        this.willAmount = willAmount;
    }

    public EnumDemonWillType getWillType() {
        return willType;
    }

    public void setWillType(EnumDemonWillType willType) {
        this.willType = willType;
    }

    public double getWillAmount() {
        return willAmount;
    }

    public void setWillAmount(double willAmount) {
        this.willAmount = willAmount;
    }
}
