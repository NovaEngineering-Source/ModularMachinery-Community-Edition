package github.kasuminova.mmce.client.util;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraft.client.renderer.BufferBuilder;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BufferBuilderPool {

    private static final List<WeakReference<BufferBuilder>> BUFFERS = ObjectLists.synchronize(new ObjectArrayList<>());
    private static final Queue<BufferBuilder> POOL = new ConcurrentLinkedQueue<>();

    public static BufferBuilder borrowBuffer(int initSize) {
        BufferBuilder buffer;
        if ((buffer = POOL.poll()) != null) {
            return buffer;
        }
        BufferBuilder newBuffer = new BufferBuilder(initSize);
        BUFFERS.add(new WeakReference<>(newBuffer));
        return newBuffer;
    }

    public static void returnBuffer(final BufferBuilder buffer) {
        buffer.getByteBuffer().clear();
        buffer.reset();
        POOL.offer(buffer);
    }

    public static int getPoolSize() {
        return POOL.size();
    }

    public static int getCreatedBuffers() {
        return BUFFERS.size();
    }

    public static int getBufferMemUsage() {
        int sum = 0;
        for (Iterator<WeakReference<BufferBuilder>> iterator = BUFFERS.iterator(); iterator.hasNext();) {
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
