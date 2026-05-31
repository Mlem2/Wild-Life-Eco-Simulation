package core.enviroment;

import allEnum.Direction;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class WorldMap {
    protected static Terrain[][] worldMap;
    protected Chunk[][] chunkMap;
    protected static int SIZE;
    private static float[][] heightNoiseMap;
    private static float[][] moistureNoiseMap;

    //Constructor
    public WorldMap(int seed, int SIZE) {
        WorldMap.SIZE = SIZE;
        worldMap = new Terrain[SIZE][SIZE];
        this.chunkMap = new Chunk[SIZE / 50][SIZE / 50];
        for (int y = 0; y < SIZE / 50; y++) {
            for (int x = 0; x < SIZE / 50; x++) {
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

    public Chunk[][] getChunkMap() {
        return chunkMap;
    }

}
