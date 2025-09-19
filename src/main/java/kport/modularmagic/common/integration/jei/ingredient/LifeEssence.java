package kport.modularmagic.common.integration.jei.ingredient;

public class LifeEssence {

    private int     essenceAmount;
    private boolean perTick;

    public LifeEssence(int essenceAmount, boolean perTick) {
        this.essenceAmount = essenceAmount;
        this.perTick = perTick;
    }

    public int getEssenceAmount() {
        return essenceAmount;
    }

    public boolean isPerTick() {
        return perTick;
    }
}
