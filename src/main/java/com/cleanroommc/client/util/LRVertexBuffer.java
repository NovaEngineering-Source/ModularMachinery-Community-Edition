package com.cleanroommc.client.util;

import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.util.BlockRenderLayer;

public class LRVertexBuffer {

    private VertexBuffer[] left  = null;
    private VertexBuffer[] right = null;

    private boolean useLeft = true;

    public LRVertexBuffer() {
    }

    public LRVertexBuffer(VertexBuffer[] left, VertexBuffer[] right) {
        this.left = left;
        this.right = right;
    }

    public VertexBuffer[] getBuffer() {
        return useLeft ? left : right;
    }

    public LRVertexBuffer setBuffer(final VertexBuffer[] buffers) {
        VertexBuffer[] buffer = getBuffer();
        if (buffer != null) {
            for (int i = 0; i < BlockRenderLayer.values().length; ++i) {
                if (buffer[i] != null) {
                    buffer[i].deleteGlBuffers();
                }
            }
        }
        return useLeft ? setLeft(buffers) : setRight(buffers);
    }

    public VertexBuffer[] getAnotherBuffer() {
        return useLeft ? right : left;
    }

    public LRVertexBuffer setAnotherBuffer(final VertexBuffer[] buffers) {
        VertexBuffer[] buffer = getAnotherBuffer();
        if (buffer != null) {
            for (int i = 0; i < BlockRenderLayer.values().length; ++i) {
                if (buffer[i] != null) {
                    buffer[i].deleteGlBuffers();
                }
            }
        }
        return useLeft ? setRight(buffers) : setLeft(buffers);
    }

    public boolean isUseLeft() {
        return useLeft;
    }

    public LRVertexBuffer setUseLeft(final boolean useLeft) {
        this.useLeft = useLeft;
        return this;
    }

    public boolean isUseRight() {
        return !useLeft;
    }

    public LRVertexBuffer setUseRight(final boolean useRight) {
        this.useLeft = !useRight;
        return this;
    }

    public VertexBuffer[] getLeft() {
        return left;
    }

    public LRVertexBuffer setLeft(final VertexBuffer[] left) {
        this.left = left;
        return this;
    }

    public VertexBuffer[] getRight() {
        return right;
    }

    public LRVertexBuffer setRight(final VertexBuffer[] right) {
        this.right = right;
        return this;
    }
}
