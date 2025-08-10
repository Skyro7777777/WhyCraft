package com.why.craft;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class World {
    private static final int CHUNK_SIZE = 16;
    private final int width, height, depth;
    private final List<Chunk> chunks = new ArrayList<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    public World(int width, int height, int depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        initChunks();
    }

    private void initChunks() {
        int chunkCountX = (int) Math.ceil((float) width / CHUNK_SIZE);
        int chunkCountZ = (int) Math.ceil((float) depth / CHUNK_SIZE);

        for (int cx = 0; cx < chunkCountX; cx++) {
            for (int cz = 0; cz < chunkCountZ; cz++) {
                Chunk chunk = new Chunk(CHUNK_SIZE, height, CHUNK_SIZE, cx * CHUNK_SIZE, cz * CHUNK_SIZE);
                chunks.add(chunk);
                executor.submit(chunk::generateTerrain);
            }
        }
    }

    // World.java
	
	public int getBlock(int x, int y, int z) {
		if (x < 0 || x >= width || z < 0 || z >= depth || y < 0 || y >= height)
		return Block.AIR;
		
		if (y < 0 || y >= height) return Block.AIR; // World bounds check
		
		for (Chunk chunk : chunks) {
			if (chunk.contains(x, y, z)) {
				return chunk.getBlock(x - chunk.offsetX, y, z - chunk.offsetZ);
			}
		}
		return Block.AIR; // Treat unloaded chunks as air
	}

    public void setBlock(int x, int y, int z, int type) {
		
        for (Chunk chunk : chunks) {
            if (chunk.contains(x, y, z)) {
                chunk.setBlock(x - chunk.offsetX, y, z - chunk.offsetZ, type);
                chunk.markDirty();
                return;
            }
        }
    }

    /**
     * Returns true if the player's bounding box at (x,y,z) is in air blocks.
     */
    public boolean isPositionValid(float x, float y, float z) {
        int ix = (int) x;
        int iy = (int) y;
        int iz = (int) z;

        // Check player head and feet
        return getBlock(ix, iy, iz) == Block.AIR &&
               getBlock(ix, iy + 1, iz) == Block.AIR;
    }

    public void render(float[] vpMatrix, ChunkRenderer renderer) {
    for (Chunk chunk : chunks) {
        if (chunk.isReady() && !chunk.isEmpty()) {
            renderer.renderChunk(vpMatrix, chunk, this);  // Pass world reference
        }
    }
	}

    public void cleanup() {
        executor.shutdown();
    }

    public int getHeight() {
        return height;
    }
	public int getWidth() { 
		return width; 
		}
    public int getDepth() { 
		return depth; 
		}
}