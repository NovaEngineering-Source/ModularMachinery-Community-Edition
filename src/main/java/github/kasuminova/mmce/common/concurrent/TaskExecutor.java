package github.kasuminova.mmce.common.concurrent;

import github.kasuminova.mmce.common.util.concurrent.*;
import hellfirepvp.modularmachinery.ModularMachinery;
import hellfirepvp.modularmachinery.common.tiles.base.TileEntitySynchronized;
import io.netty.util.internal.ThrowableUtil;
import io.netty.util.internal.shaded.org.jctools.queues.atomic.MpscLinkedAtomicQueue;
import it.unimi.dsi.fastutil.longs.*;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

public class TaskExecutor {
    public static final int THREAD_COUNT = Math.min(Math.max(Runtime.getRuntime().availableProcessors() / 4, 4), 8);

    public static final ThreadPoolExecutor THREAD_POOL = new ThreadPoolExecutor(THREAD_COUNT, THREAD_COUNT,
            5000, TimeUnit.MILLISECONDS,
            new PriorityBlockingQueue<>(),
            new CustomThreadFactory("MMCE-TaskExecutor-%s"));

    public static final ForkJoinPool FORK_JOIN_POOL = new ForkJoinPool(THREAD_COUNT,
            new CustomForkJoinWorkerThreadFactory("MMCE-ForkJoinPool-worker-%s"),
            null, true);

    public static long totalExecuted = 0;
    public static long taskUsedTime = 0;
    public static long totalUsedTime = 0;
    public static long executedCount = 0;

    public static long tickExisted = 0;

    private final Queue<ActionExecutor> submitted = new MpscLinkedAtomicQueue<>();

    private final Queue<ActionExecutor> executors = new MpscLinkedAtomicQueue<>();
    private final Long2ObjectMap<ExecuteGroup> executeGroups = new Long2ObjectOpenHashMap<>();

    private final Queue<ForkJoinTask<?>> forkJoinTasks = new MpscLinkedAtomicQueue<>();

    private final Queue<Action> mainThreadActions = new MpscLinkedAtomicQueue<>();
    private final Queue<TileEntitySynchronized> requireUpdateTEQueue = new MpscLinkedAtomicQueue<>();
    private final Queue<TileEntitySynchronized> requireMarkNoUpdateTEQueue = new MpscLinkedAtomicQueue<>();

    private final TaskSubmitter submitter = new TaskSubmitter();

    private volatile boolean inTick = false;
    private volatile boolean shouldUseForkJoinPool = false;

    public void init() {
        THREAD_POOL.prestartAllCoreThreads();
        submitter.start();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onServerTick(final TickEvent.ServerTickEvent event) {
        if (event.side == Side.CLIENT) {
            return;
        }
        switch (event.phase) {
            case START -> {
                inTick = true;
                submitter.unpark();
            }
            default -> {
                inTick = false;
                tickExisted++;
            }
        }

        int executed = executeActions();
        if (executed > 0) {
            totalExecuted += executed;
            executedCount++;
        }

        executeGroups.clear();
        checkShouldUseForkJoinPool();
    }

    /**
     * <p>大量任务的前提下，使用阻塞队列会导致 CPU 资源占用的激增，此时我们不再关注优先级，而是考虑尽可能快地提交任务。</p>
     *
     * <p>With a large number of tasks, the use of a blocking queue can lead to a spike in CPU resource usage, at which
     * point we stop focusing on prioritization and think about submitting tasks as fast as possible.</p>
     */
    private void checkShouldUseForkJoinPool() {
        if (tickExisted % 20 != 0) {
            return;
        }

        if (shouldUseForkJoinPool) {
            if (!shouldUseForkJoinPool()) {
                ModularMachinery.log.warn("The thread pool has been re-switched to ThreadPoolExecutor (below the limit of 1500).");
                shouldUseForkJoinPool = false;
            }
            return;
        }
        if (shouldUseForkJoinPool()) {
            ModularMachinery.log.warn("The thread pool has now been replaced with a ForkJoinPool due to too many tasks in a single commit (Limit 1500).");
            shouldUseForkJoinPool = true;
        }
    }

    private boolean shouldUseForkJoinPool() {
        long executedAvgPerExecution = executedCount == 0 ? 0 : totalExecuted / executedCount;
        return executedAvgPerExecution >= 1500;
    }

    /**
     * 正式执行队列内的所有操作。
     *
     * @return 已执行的数量
     */
    public int executeActions() {
        int executed = 0;
        long time = System.nanoTime() / 1000;

        submitTask();
        executed += spinAwaitActionExecutor();
        executed += executeMainThreadActions();

        updateTileEntity();

        // Empty Check
        if (!submitted.isEmpty()) {
            executed += executeActions();
        }

        totalUsedTime += System.nanoTime() / 1000 - time;
        return executed;
    }

    private int executeMainThreadActions() {
        int executed = 0;
        if (mainThreadActions.isEmpty()) {
            return executed;
        }

        Action action;
        while ((action = mainThreadActions.poll()) != null) {
            try {
                action.doAction();
            } catch (Exception e) {
                ModularMachinery.log.warn("An error occurred during synchronous task execution!");
                ModularMachinery.log.warn(ThrowableUtil.stackTraceToString(e));
            }
            executed++;
        }
        return executed;
    }

    private int spinAwaitActionExecutor() {
        int executed = 0;

        ActionExecutor executor;
        while ((executor = submitted.poll()) != null) {
            // Spin up while completing the operation in the queue.
            while (!executor.isCompleted) {
                executed += executeMainThreadActions();
                updateTileEntity();
                if (!executor.isCompleted) {
                    loopWait(100_000L);
                }
            }

            taskUsedTime += executor.usedTime;
            executed++;
        }
        return executed;
    }

    @SuppressWarnings({"BusyWait", "SameParameterValue"})
    private static void loopWait(final long nanos) {
        long startTime = System.nanoTime();
        while (System.nanoTime() - startTime < nanos) {
            try {
                Thread.sleep(0);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void updateTileEntity() {
        if (requireUpdateTEQueue.isEmpty() && requireMarkNoUpdateTEQueue.isEmpty()) {
            return;
        }

        TileEntitySynchronized te;
        while ((te = requireUpdateTEQueue.poll()) != null) {
            te.markForUpdate();
        }

        while ((te = requireMarkNoUpdateTEQueue.poll()) != null) {
            te.markNoUpdate();
        }
    }

    /**
     * <p>添加一个异步操作引用，这个操作必定在本 Tick 结束前执行完毕。</p>
     *
     * @param action 要执行的异步任务
     */
    public ActionExecutor addTask(final Action action) {
        return addTask(action, 0);
    }

    /**
     * <p>添加一个异步操作引用，这个操作必定在本 Tick 结束前执行完毕。</p>
     *
     * @param action   要执行的异步任务
     * @param priority 优先级
     */
    public ActionExecutor addTask(final Action action, final int priority) {
        ActionExecutor actionExecutor = new ActionExecutor(action, priority);
        executors.offer(actionExecutor);

        return actionExecutor;
    }

    public ActionExecutor addExecuteGroupTask(final Action action, final long groupId) {
        ExecuteGroup group = executeGroups.get(groupId);
        if (group == null) {
            synchronized (executeGroups) {
                group = executeGroups.get(groupId);
                if (group == null) {
                    group = new ExecuteGroup(groupId);
                    executeGroups.put(groupId, group);
                }
            }
        }

        return group.offer(new ActionExecutor(action));
    }

    public <T> ForkJoinTask<T> submitForkJoinTask(final ForkJoinTask<T> task) {
        forkJoinTasks.offer(task);
        return task;
    }

    /**
     * <p>添加一个同步操作引用，这个操作必定会在异步操作完成后在<strong>主线程</strong>中顺序执行。</p>
     *
     * @param action 要执行的同步任务
     */
    public void addSyncTask(final Action action) {
        mainThreadActions.offer(action);
    }

    public void addTEUpdateTask(final TileEntitySynchronized te) {
        requireUpdateTEQueue.offer(te);
    }

    public void addTEMarkNoUpdateTask(final TileEntitySynchronized te) {
        requireMarkNoUpdateTEQueue.offer(te);
    }

    private void execute(final ActionExecutor executor) {
        if (shouldUseForkJoinPool) {
            FORK_JOIN_POOL.execute(executor);
        } else {
            THREAD_POOL.execute(executor);
        }
    }

    private synchronized void submitTask() {
        ActionExecutor executor;
        while ((executor = executors.poll()) != null) {
            execute(executor);
            submitted.offer(executor);
        }

        ForkJoinTask<?> forkJoinTask;
        while ((forkJoinTask = forkJoinTasks.poll()) != null) {
            FORK_JOIN_POOL.submit(forkJoinTask);
        }

        synchronized (executeGroups) {
            LongList toRemove = new LongArrayList();
            for (final ExecuteGroup group : executeGroups.values()) {
                if (group.isSubmitted()) {
                    continue;
                }
                if (group.isEmpty()) {
                    toRemove.add(group.getGroupId());
                    continue;
                }
                ActionExecutor groupExecutor = new ActionExecutor(() -> {
                    ActionExecutor actionExecutor;
                    while ((actionExecutor = group.poll()) != null) {
                        actionExecutor.run();
                    }
                    group.setSubmitted(false);
                });
                group.setSubmitted(true);
                execute(groupExecutor);
                submitted.offer(groupExecutor);
            }
            LongListIterator it = toRemove.iterator();
            while (it.hasNext()) {
                executeGroups.remove(it.nextLong());
            }
        }
    }

    public class TaskSubmitter implements Runnable {
        public Thread thread = null;

        public void start() {
            if (thread != null && thread.isAlive()) {
                thread.interrupt();
            }
            thread = new Thread(this);
            thread.setName("MMCE-TaskSubmitter");
            thread.start();
        }

        public void unpark() {
            if (thread != null) {
                LockSupport.unpark(thread);
            }
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                if (inTick) {
                    if (!executors.isEmpty() || !executeGroups.isEmpty() || !forkJoinTasks.isEmpty()) {
                        submitTask();
                    } else {
                        LockSupport.parkNanos(10_000L);
                    }
                } else {
                    LockSupport.park();
                }
            }
        }
    }
}
