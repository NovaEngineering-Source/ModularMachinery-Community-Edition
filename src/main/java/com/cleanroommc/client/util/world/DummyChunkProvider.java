package com.cleanroommc.client.util.world;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DummyChunkProvider implements IChunkProvider {

    private final World                 world;
    private final Long2ObjectMap<Chunk> loadedChunks = new Long2ObjectOpenHashMap<>();

    public DummyChunkProvider(World world) {
        this.world = world;
    }

    @Nullable
    @Override
    public Chunk getLoadedChunk(int x, int z) {
        return loadedChunks.get(ChunkPos.asLong(x, z));
    }

    @Nonnull
    @Override
    public Chunk provideChunk(int x, int z) {
        long chunkKey = ChunkPos.asLong(x, z);
        if (loadedChunks.containsKey(chunkKey)) {
            return loadedChunks.get(chunkKey);
        }
        Chunk chunk = new Chunk(world, x, z);
        loadedChunks.put(chunkKey, chunk);
        return chunk;
    }

    @Override
    public boolean tick() {
        for (Chunk chunk : loadedChunks.values()) {
            chunk.onTick(false);
        }
        return !loadedChunks.isEmpty();
    }

    @Nonnull
    @Override
    public String makeString() {
        return "Dummy";
    }

    @Override
    public boolean isChunkGeneratedAt(int x, int z) {
        return true;
    }
}
