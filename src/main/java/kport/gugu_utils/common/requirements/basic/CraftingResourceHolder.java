package kport.gugu_utils.common.requirements.basic;

public class CraftingResourceHolder<T extends IResourceToken> implements ICraftingResourceHolder<T> {

    public final IConsumable<T> consumable;
    public final IGeneratable<T> generatable;
    private ICraftNotifier<T> notifier;
    private boolean fulfilled = false;

    public CraftingResourceHolder(IConsumable<T> consumable, IGeneratable<T> generatable) {
        this.consumable = consumable;
        this.generatable = generatable;
    }

    public CraftingResourceHolder(IConsumable<T> consumable) {
        this.consumable = consumable;
        this.generatable = null;
    }

    public CraftingResourceHolder(IGeneratable<T> generatable) {
        this.consumable = null;
        this.generatable = generatable;
    }

    public CraftingResourceHolder<T> setNotify(ICraftNotifier<T> notifier) {
        this.notifier = notifier;
        return this;
    }

    @Override
    public boolean consume(T outputToken, boolean doOperation) {
        if (this.consumable != null) {
            return this.consumable.consume(outputToken, doOperation);
        }
        return false;
    }

    @Override
    public boolean canConsume() {
        return this.consumable != null;
    }

    @Override
    public boolean generate(T outputToken, boolean doOperation) {
        if (this.generatable != null) {
            return this.generatable.generate(outputToken, doOperation);
        }
        return false;
    }

    @Override
    public boolean canGenerate() {
        return this.generatable != null;
    }

    @Override
    public String getInputProblem(T checkToken) {
        if (checkToken.getError() != null) {
            return checkToken.getError();
        }
        return "craftcheck.failure." + checkToken.getKey() + ".input";
    }

    @Override
    public String getOutputProblem(T checkToken) {
        if (checkToken.getError() != null) {
            return checkToken.getError();
        }
        return "craftcheck.failure." + checkToken.getKey() + ".output.space";
    }

    @Override
    public void startCrafting(T outputToken) {
        if (this.notifier != null) {
            notifier.startCrafting(outputToken);
        }
    }

    @Override
    public void finishCrafting(T outputToken) {
        if (this.notifier != null) {
            notifier.finishCrafting(outputToken);
        }
    }

    @Override
    public boolean isFulfilled() {
        return fulfilled;
    }

    @Override
    public void setFulfilled(boolean value) {
        fulfilled = value;
    }
}
