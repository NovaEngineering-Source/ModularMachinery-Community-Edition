package github.kasuminova.mmce.common.tile.base;

import hellfirepvp.modularmachinery.common.machine.MachineComponent;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

public interface MachineCombinationComponent {

    AtomicLong GROUP_ACQUIRER = new AtomicLong(Integer.MAX_VALUE);

    /**
     * 每个MachineComponent的getGroupID方法应该返回不同的值，否则不应该实现此接口
     *
     */
    @Nonnull
    Collection<MachineComponent<?>> provideComponents();

    /**
     * 应该缓存结果而不是反复调用
     * Should cache the results instead of repeatedly calling
     */
    default long getUniqueGroupID() {
        return GROUP_ACQUIRER.incrementAndGet();
    }

}
