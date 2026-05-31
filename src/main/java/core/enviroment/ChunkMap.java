package core.enviroment;

import allEnum.Direction;
import entities.base.Entity;
import entities.base.EntityMap;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class ChunkMap {
    protected static int SIZE;
    protected Chunk[][] chunkMap;
    protected static int CHUNK_ARRAY_SIZE;
    protected static int CHUNK_SIZE;
    private static int[][] waterHeatMap;

    public ChunkMap(WorldMap worldMap, EntityMap entityMap, int SIZE) {
        ChunkMap.SIZE = SIZE;
        CHUNK_ARRAY_SIZE = SIZE / 50;
        CHUNK_SIZE = SIZE / CHUNK_ARRAY_SIZE;
        waterHeatMap =  new int[CHUNK_ARRAY_SIZE][CHUNK_ARRAY_SIZE];
        generateWaterHeatMap(worldMap);
        generateChunkMap();
        addEntitiesToChunks(entityMap);
    }

    public Chunk getChunk(int x, int y) {
        return chunkMap[y / CHUNK_SIZE][x / CHUNK_SIZE];
    }

    private void generateChunkMap() {
        for (int y = 0; y < CHUNK_ARRAY_SIZE; y++) {
            for (int x = 0; x < CHUNK_ARRAY_SIZE; x++) {
                chunkMap[y][x] = new Chunk(waterHeatMap[y][x]);
            }
        }
    }

    private void generateWaterHeatMap(WorldMap worldMap) {
        for (int[] row : waterHeatMap) Arrays.fill(row, -1);

        Queue<int[]> queue = new LinkedList<>();

        //Get chunks where there are water
        for (int y = 0; y < CHUNK_ARRAY_SIZE; y++) {
            for (int x = 0; x < CHUNK_ARRAY_SIZE; x++) {

                chunkCheck:
                for (int i = 0; i < CHUNK_SIZE; i++) {
                    for (int j = 0; j < CHUNK_SIZE; j++) {
                        if (worldMap.getTile(CHUNK_SIZE * x + j, CHUNK_SIZE * y + i) == Terrain.WATER) {
                            waterHeatMap[y][x] = 0;

                            queue.add(new int[]{x, y});
                            break chunkCheck;
                        }
                    }
                }
            }
        }

        propagateWaterDistance(queue);
    }

    private void propagateWaterDistance(Queue<int[]> queue) {
        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int cx = current[0];
            int cy = current[1];
            int currentDistance = waterHeatMap[cy][cx];

            // Loop through all directions defined in your enum
            for (Direction dir : Direction.values()) {
                // Skip CENTER, as we only want to move outward to actual neighbors
                if (dir == Direction.CENTER) {
                    continue;
                }

                // Apply your enum's x and y offsets to get the neighbor coordinates
                int nx = cx + dir.x;
                int ny = cy + dir.y;

                // Make sure the neighbor is within map bounds
                if (nx >= 0 && nx < CHUNK_ARRAY_SIZE && ny >= 0 && ny < CHUNK_ARRAY_SIZE) {

                    // If the neighbor chunk hasn't been visited yet (-1)
                    if (waterHeatMap[ny][nx] == -1) {
                        waterHeatMap[ny][nx] = currentDistance + 1;
                        queue.add(new int[]{nx, ny});
                    }
                }
            }
        }
    }

    private void addEntitiesToChunks(EntityMap entityMap) {
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                Entity entity = entityMap.getEntity(x, y);
                if (entity != null) {
                    chunkMap[y / 10][x / 10].addEntity(entity);
                }
            }
        }
    }
}
