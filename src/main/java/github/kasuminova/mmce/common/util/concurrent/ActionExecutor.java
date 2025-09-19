package github.kasuminova.mmce.common.util.concurrent;

import hellfirepvp.modularmachinery.ModularMachinery;
import io.netty.util.internal.ThrowableUtil;

public class ActionExecutor implements Runnable, Comparable<ActionExecutor> {
    public final    Action  action;
    public final    int     priority;
    public volatile boolean isCompleted = false;
    public volatile int     usedTime    = 0;

    public ActionExecutor(Action action) {
        this(action, 0);
    }

    public ActionExecutor(Action action, int priority) {
        this.action = action;
        this.priority = priority;
    }

    public void run() {
        long start = System.nanoTime() / 1000;

        try {
            action.doAction();
        } catch (Throwable e) {
            ModularMachinery.log.warn("An error occurred during asynchronous task execution!");
            ModularMachinery.log.warn(ThrowableUtil.stackTraceToString(e));
        }

        usedTime = (int) (System.nanoTime() / 1000 - start);
        isCompleted = true;
    }

    @Override
    public int compareTo(ActionExecutor o) {
        return o.priority - priority;
    }
}