package github.kasuminova.mmce.client.util;

import net.minecraft.client.renderer.BufferBuilder;

public interface BufferProvider {

    BufferBuilder getBuffer();

    BufferBuilder getBuffer(final boolean bloom, final boolean transparent);

    void begin();

    void finishDrawing();

}
