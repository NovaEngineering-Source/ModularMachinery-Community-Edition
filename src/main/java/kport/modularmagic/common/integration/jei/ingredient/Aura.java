package kport.modularmagic.common.integration.jei.ingredient;

import de.ellpeck.naturesaura.api.aura.type.IAuraType;

public class Aura {

    private int       amount;
    private IAuraType type;

    public Aura(int amount, IAuraType type) {
        this.amount = amount;
        this.type = type;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public IAuraType getType() {
        return type;
    }

    public void setType(IAuraType type) {
        this.type = type;
    }
}
