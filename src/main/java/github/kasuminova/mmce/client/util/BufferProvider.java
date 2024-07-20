package github.kasuminova.mmce.client.util;

import net.minecraft.client.renderer.BufferBuilder;

public interface BufferProvider {

    BufferBuilder getBuffer();

    BufferBuilder getBuffer(final boolean bloom, final boolean transparent, final boolean isStatic);

    void begin(final boolean isStatic);

    void finishDrawing(final boolean isStatic);

}
