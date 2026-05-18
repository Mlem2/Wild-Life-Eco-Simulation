package core.enviroment;

import java.awt.*;

public class WorldMap {
    protected static Terrain[][] worldMap;
    protected static final int SIZE = 500;
    private static float[][] heightNoiseMap;
    private static float[][] moistureNoiseMap;

    //Constructor
    public WorldMap(int seed) {
        worldMap = new Terrain[SIZE][SIZE];

        heightNoiseMap = generateNoiseArray(seed);
        moistureNoiseMap = generateNoiseArray(seed + 392);

        generateTileMap();
    }

    public Terrain getTile(int x, int y) {
        return  worldMap[y][x];
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

    public void generateTileMap() {
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                float heightVal = heightNoiseMap[x][y];
                float moistureVal = moistureNoiseMap[x][y];

                //Tile gen conditions based on height and moisture
                if (heightVal < -0.2f) {
                    worldMap[x][y] = Terrain.WATER;
                } else if (heightVal < 0.2f) {
                    if (moistureVal > 0.3f) {
                        worldMap[x][y] = Terrain.MUD;
                    } else if (moistureVal > 0.0f) {
                        worldMap[x][y] = Terrain.FOREST;
                    } else {
                        worldMap[x][y] = Terrain.GRASSLAND;
                    }
                } else {
                    worldMap[x][y] = Terrain.MOUNTAIN;
                }
            }
        }
    }
}
