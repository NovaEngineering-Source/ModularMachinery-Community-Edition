package github.kasuminova.mmce.common.util.concurrent;

import javax.annotation.Nonnull;
import java.util.concurrent.locks.ReadWriteLock;

public interface ReadWriteLockProvider {

    @Nonnull
    ReadWriteLock getRWLock();

}
