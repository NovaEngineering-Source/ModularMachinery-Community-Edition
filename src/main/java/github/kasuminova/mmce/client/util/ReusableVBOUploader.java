package github.kasuminova.mmce.client.util;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;

import java.nio.ByteBuffer;
import java.util.List;

public class ReusableVBOUploader extends WorldVertexBufferUploader {

    protected ByteBuffer buffer = null;

    @Override
    public void draw(final BufferBuilder builder) {
        if (builder.getVertexCount() <= 0) {
            return;
        }

        VertexFormat format = builder.getVertexFormat();
        int vertexSize = format.getSize();
        ByteBuffer buffer = builder.getByteBuffer();
        List<VertexFormatElement> elements = format.getElements();
        int elementCount = elements.size();

        // Pre-draw setup
        for (int index = 0; index < elementCount; ++index) {
            VertexFormatElement element = elements.get(index);
            buffer.position(format.getOffset(index));
            element.getUsage().preDraw(format, index, vertexSize, buffer);
        }

        // Drawing
        GlStateManager.glDrawArrays(builder.getDrawMode(), 0, builder.getVertexCount());

        // Post-draw cleanup
        for (int index = 0; index < elementCount; ++index) {
            VertexFormatElement element = elements.get(index);
            element.getUsage().postDraw(format, index, vertexSize, buffer);
        }
    }

    /**
     * TODO: Use glMultiDrawArrays().
     */
    public void drawMultiple(List<BufferBuilder> builders) {
        if (builders.isEmpty()) {
            return;
        }

        // Merge buffers
        int totalSize = 0;
        int totalVertexCount = 0;
        for (BufferBuilder builder : builders) {
            totalSize += builder.getByteBuffer().limit();
            totalVertexCount += builder.getVertexCount();
        }
        checkBufferSize(totalSize);

        for (BufferBuilder builder : builders) {
            ByteBuffer buffer = builder.getByteBuffer();
            buffer.rewind();
            this.buffer.put(buffer);
        }

        BufferBuilder example = builders.get(0);
        VertexFormat format = example.getVertexFormat();
        int vertexSize = format.getSize();
        List<VertexFormatElement> elements = format.getElements();
        int elementCount = elements.size();

        // Pre-draw setup
        for (int index = 0; index < elementCount; ++index) {
            VertexFormatElement element = elements.get(index);
            buffer.position(format.getOffset(index));
            element.getUsage().preDraw(format, index, vertexSize, buffer);
        }

        // Drawing
        GlStateManager.glDrawArrays(example.getDrawMode(), 0, totalVertexCount);

        // Post-draw cleanup
        for (int index = 0; index < elementCount; ++index) {
            VertexFormatElement element = elements.get(index);
            element.getUsage().postDraw(format, index, vertexSize, buffer);
        }

        this.buffer.clear();
    }

    public void checkBufferSize(int minSize) {
        if (buffer == null || buffer.capacity() < minSize) {
            buffer = GLAllocation.createDirectByteBuffer(minSize);
        }
        buffer.limit(minSize);
    }

}
