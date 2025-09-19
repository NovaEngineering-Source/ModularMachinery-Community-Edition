package github.kasuminova.mmce.client.util;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraft.client.renderer.BufferBuilder;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;

public class BufferBuilderPool {

    private static final List<WeakReference<BufferBuilder>>          BUFFERS = ObjectLists.synchronize(new ObjectArrayList<>());
    private static final NavigableMap<Integer, Queue<BufferBuilder>> POOL    = new ConcurrentSkipListMap<>();

    public static synchronized BufferBuilder borrowBuffer(int initSize) {
        Map.Entry<Integer, Queue<BufferBuilder>> entry = POOL.ceilingEntry(initSize);
        Queue<BufferBuilder> ceiling = entry == null ? null : entry.getValue();

        if (ceiling != null) {
            BufferBuilder buffer = ceiling.poll();
            if (ceiling.isEmpty()) {
                POOL.remove(entry.getKey());
            }
            if (buffer != null) {
                return buffer;
            }
        }

        BufferBuilder buffer = new BufferBuilder(initSize / 4);
        BUFFERS.add(new WeakReference<>(buffer));
        return buffer;
    }

    public static synchronized void returnBuffer(final BufferBuilder buffer) {
        buffer.getByteBuffer().clear();
        buffer.reset();

        Queue<BufferBuilder> builder = POOL.get(buffer.getByteBuffer().capacity());
        if (builder == null) {
            builder = POOL.computeIfAbsent(buffer.getByteBuffer().capacity(), k -> new ConcurrentLinkedQueue<>());
            builder.offer(buffer);
            return;
        }

        builder.offer(buffer);
    }

    public static int getPoolSize() {
        return POOL.values().stream().mapToInt(Queue::size).sum();
    }

    public static int getCreatedBuffers() {
        return BUFFERS.size();
    }

    public static long getBufferMemUsage() {
        long sum = 0;
        for (Iterator<WeakReference<BufferBuilder>> iterator = BUFFERS.iterator(); iterator.hasNext(); ) {
            final WeakReference<BufferBuilder> ref = iterator.next();
            BufferBuilder buffer = ref.get();
            if (buffer == null) {
                iterator.remove();
                continue;
            }
            sum += buffer.getByteBuffer().capacity();
        }
        return sum;
    }

}
