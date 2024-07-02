package github.kasuminova.mmce.common.util.concurrent;

import hellfirepvp.modularmachinery.ModularMachinery;
import io.netty.util.internal.ThrowableUtil;

import java.util.concurrent.RecursiveTask;

public abstract class TimeRecordingTask<V> extends RecursiveTask<V> {
    public volatile int usedTime = 0;

    @Override
    protected final V compute() {
        long start = System.nanoTime() / 1000;

        V result = null;
        try {
            result = computeTask();
        } catch (Throwable e) {
            ModularMachinery.log.warn("An error occurred during fork join task execution!");
            ModularMachinery.log.warn(ThrowableUtil.stackTraceToString(e));
        }

        usedTime = (int) (System.nanoTime() / 1000 - start);
        return result;
    }

    protected abstract V computeTask();
}
