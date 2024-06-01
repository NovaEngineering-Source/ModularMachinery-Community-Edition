package github.kasuminova.mmce.common.util.concurrent;

import hellfirepvp.modularmachinery.ModularMachinery;
import io.netty.util.internal.ThrowableUtil;

import java.util.concurrent.RecursiveAction;

public abstract class TimeRecordingAction extends RecursiveAction {
    public volatile int usedTime = 0;

    @Override
    protected final void compute() {
        long start = System.nanoTime() / 1000;

        try {
            computeAction();
        } catch (Throwable e) {
            ModularMachinery.log.warn("An error occurred during fork join task execution!");
            ModularMachinery.log.warn(ThrowableUtil.stackTraceToString(e));
        }

        usedTime = (int) (System.nanoTime() / 1000 - start);
    }

    protected abstract void computeAction();
}
