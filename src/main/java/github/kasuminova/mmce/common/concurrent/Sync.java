package github.kasuminova.mmce.common.concurrent;

import crafttweaker.annotations.ZenRegister;
import github.kasuminova.mmce.common.util.concurrent.Action;
import github.kasuminova.mmce.common.util.concurrent.ReadWriteLockProvider;
import hellfirepvp.modularmachinery.ModularMachinery;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nonnull;
import java.util.concurrent.locks.ReadWriteLock;

@ZenRegister
@ZenClass("mods.modularmachinery.Sync")
public class Sync {
    /**
     * <p>全局同步方法，用来进行方块更新、同步等非线程安全操作。</p>
     * <p>Global synchronization method.</p>
     * <p>For non-thread-safe operations such as block update and synchronization.</p>
     */
    public static synchronized void doSyncAction(Action action) {
        action.doAction();
    }

    @ZenMethod
    public static void addSyncTask(Action action) {
        ModularMachinery.EXECUTE_MANAGER.addSyncTask(action);
    }

    public static void executeSyncIfPresent(@Nonnull final Object mutex, @Nonnull final Runnable operation) {
        if (mutex instanceof ReadWriteLockProvider lockProvider) {
            ReadWriteLock rwLock = lockProvider.getRWLock();
            rwLock.writeLock().lock();
            try {
                operation.run();
            } finally {
                rwLock.writeLock().unlock();
            }
        } else {
            synchronized (mutex) {
                operation.run();
            }
        }
    }

}
