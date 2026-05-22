package core.enviroment;

import allEnum.Direction;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class WorldMap {
    protected static Terrain[][] worldMap;
    protected Chunk[][] chunkMap;
    protected static final int SIZE = 500;
    protected static final int CHUNK_ARRAY_SIZE = SIZE / 50;
    protected static final int CHUNK_SIZE = SIZE / CHUNK_ARRAY_SIZE;
    private static int[][] waterHeatMap = new int[CHUNK_ARRAY_SIZE][CHUNK_ARRAY_SIZE];
    private static float[][] heightNoiseMap;
    private static float[][] moistureNoiseMap;

    //Constructor
    public WorldMap(int seed) {
        worldMap = new Terrain[SIZE][SIZE];
        chunkMap = new Chunk[CHUNK_ARRAY_SIZE][CHUNK_ARRAY_SIZE];

        heightNoiseMap = generateNoiseArray(seed);
        moistureNoiseMap = generateNoiseArray(seed + 392);

        generateWorldMap();
        generateChunkMap();
    }

    public Terrain getTile(int x, int y) {
        return worldMap[y][x];
    }

    //Gen Perlin noise map using FastNoiseLite
    public float[][] generateNoiseArray(int seed) {
        float[][] noiseMap = new float[SIZE][SIZE];

        FastNoiseLite noise = new FastNoiseLite();
        noise.SetSeed(seed);
        noise.SetNoiseType(FastNoiseLite.NoiseType.Perlin);

        //Make more noise if increased
        noise.SetFrequency(0.005f);

        //Decreasing octaves makes maps smoother
        noise.SetFractalType(FastNoiseLite.FractalType.FBm);
        noise.SetFractalOctaves(10);

        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                noiseMap[x][y] = noise.GetNoise((float)x, (float)y);
            }
        }

        return noiseMap;
    }

    public void generateWorldMap() {
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                float heightVal = heightNoiseMap[y][x];
                float moistureVal = moistureNoiseMap[y][x];

                //Tile gen conditions based on height and moisture
                if (heightVal < -0.2f) {
                    worldMap[y][x] = Terrain.WATER;
                } else if (heightVal < 0.2f) {
                    if (moistureVal > 0.3f) {
                        worldMap[y][x] = Terrain.MUD;
                    } else if (moistureVal > 0.0f) {
                        worldMap[y][x] = Terrain.FOREST;
                    } else {
                        worldMap[y][x] = Terrain.GRASSLAND;
                    }
                } else {
                    worldMap[y][x] = Terrain.MOUNTAIN;
                }
            }
        }
    }

    public void generateChunkMap() {
        for (int y = 0; y < CHUNK_ARRAY_SIZE; y++) {
            for (int x = 0; x < CHUNK_ARRAY_SIZE; x++) {
                chunkMap[y][x] = new Chunk(waterHeatMap[y][x]);
            }
        }
    }

    public void generateWaterHeatMap() {
        for (int[] row : waterHeatMap) Arrays.fill(row, -1);

        Queue<int[]> queue = new LinkedList<>();

        //Get chunks where there are water
        for (int y = 0; y < CHUNK_ARRAY_SIZE; y++) {
            for (int x = 0; x < CHUNK_ARRAY_SIZE; x++) {

                chunkCheck:
                for (int i = 0; i < CHUNK_SIZE; i++) {
                    for (int j = 0; j < CHUNK_SIZE; j++) {
                        if (worldMap[CHUNK_SIZE * y + i][CHUNK_SIZE * x + j] == Terrain.WATER) {
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
}
