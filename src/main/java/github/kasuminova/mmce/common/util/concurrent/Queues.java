package github.kasuminova.mmce.common.util.concurrent;

import io.netty.util.internal.shaded.org.jctools.queues.atomic.MpscLinkedAtomicQueue;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Queues {

    public static <E> Queue<E> createConcurrentQueue() {
        try {
            // May be incompatible with cleanroom.
            return new MpscLinkedAtomicQueue<>();
        } catch (Throwable e) {
            return new ConcurrentLinkedQueue<>();
        }
    }

}
