package core.enviroment;

import allEnum.Direction;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class WorldMap {
    public static final int CHUNK_SIZE = 50;
    protected static Terrain[][] worldMap;
    public final Chunk[][] chunkMap;
    public static int SIZE;
    private static float[][] heightNoiseMap;
    private static float[][] moistureNoiseMap;
    private int[][] waterHeatMap;
    private int CHUNK_ARRAY_SIZE;

    //Constructor
    public WorldMap(int seed, int SIZE) {
        WorldMap.SIZE = SIZE;
        worldMap = new Terrain[SIZE][SIZE];
        this.chunkMap = new Chunk[SIZE / CHUNK_SIZE][SIZE / CHUNK_SIZE];
        for (int y = 0; y < SIZE / CHUNK_SIZE; y++) {
            for (int x = 0; x < SIZE / CHUNK_SIZE; x++) {
                chunkMap[y][x] = new Chunk(0);
            }
        }

        heightNoiseMap = generateNoiseArray(seed);
        moistureNoiseMap = generateNoiseArray(seed + 392);

        generateWorldMap();
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
                    int cx = x / CHUNK_SIZE;
                    int cy = y / CHUNK_SIZE;
                    chunkMap[cy][cx].addWaterPosition(entities.base.Position.of(x, y));
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

    public void initializeChunks(entities.base.EntityMap entityMap) {
        CHUNK_ARRAY_SIZE = SIZE / CHUNK_SIZE;
        waterHeatMap = new int[CHUNK_ARRAY_SIZE][CHUNK_ARRAY_SIZE];

        generateWaterHeatMap();
        generateChunkDistances();
        addEntitiesToChunks(entityMap);
    }

    private void generateWaterHeatMap() {
        for (int[] row : waterHeatMap) Arrays.fill(row, -1);

        Queue<int[]> queue = new LinkedList<>();

        for (int y = 0; y < CHUNK_ARRAY_SIZE; y++) {
            for (int x = 0; x < CHUNK_ARRAY_SIZE; x++) {
                if (chunkMap[y][x].getWaterPositions().size() > 0) {
                    waterHeatMap[y][x] = 0;
                    queue.add(new int[]{x, y});
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

            for (Direction dir : Direction.values()) {
                if (dir == Direction.CENTER) continue;

                int nx = cx + dir.x;
                int ny = cy + dir.y;

                if (nx >= 0 && nx < CHUNK_ARRAY_SIZE && ny >= 0 && ny < CHUNK_ARRAY_SIZE) {
                    if (waterHeatMap[ny][nx] == -1) {
                        waterHeatMap[ny][nx] = currentDistance + 1;
                        queue.add(new int[]{nx, ny});
                    }
                }
            }
        }
    }

    private void generateChunkDistances() {
        for (int y = 0; y < CHUNK_ARRAY_SIZE; y++) {
            for (int x = 0; x < CHUNK_ARRAY_SIZE; x++) {
                chunkMap[y][x].setDistanceToWater(waterHeatMap[y][x]);
            }
        }
    }

    private void addEntitiesToChunks(entities.base.EntityMap entityMap) {
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                entities.base.Entity entity = entityMap.getEntity(x, y);
                if (entity != null) {
                    int yChunk = y / CHUNK_SIZE;
                    int xChunk = x / CHUNK_SIZE;
                    chunkMap[yChunk][xChunk].addEntity(entity);
                }
            }
        }
    }

    public Chunk[][] getChunkMap() {
        return chunkMap;
    }

}
