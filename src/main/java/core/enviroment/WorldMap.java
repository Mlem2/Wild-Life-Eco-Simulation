package core.enviroment;

import allEnum.Direction;
import entities.*;
import entities.base.Entity;
import entities.base.EntityFactory;

import java.util.*;

public class WorldMap {
    public static final int CHUNK_SIZE = 25;
    protected static Terrain[][] worldMap;
    protected static String[][] decorationMap;
    public final Chunk[][] chunkMap;
    public static int SIZE;
    private static float[][] heightNoiseMap;
    private static float[][] moistureNoiseMap;
    private int[][] waterHeatMap;
    private int CHUNK_ARRAY_SIZE;
    private final Random random = new Random();

    private final Map<String, Integer> waterSpawns = Map.of(
            "fish", 25
    );

    private final Map<String, Integer> forestSpawns = Map.of(
            "wolf", 3,
            "rabbit", 5,
            "tree", 200,
            "bush", 30
    );

    private final Map<String, Integer> grasslandSpawns = Map.of(
            "rabbit", 25,
            "wolf", 0,
            "elephant", 1,
            "tree", 10,
            "bush", 10
    );


    //Constructor
    public WorldMap(int seed, int SIZE) {
        WorldMap.SIZE = SIZE;
        worldMap = new Terrain[SIZE][SIZE];
        decorationMap = new String[SIZE][SIZE];
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

        populateDecorationMap();
    }

    public void initializeChunks() {
        CHUNK_ARRAY_SIZE = SIZE / CHUNK_SIZE;
        waterHeatMap = new int[CHUNK_ARRAY_SIZE][CHUNK_ARRAY_SIZE];

        generateWaterHeatMap();
        generateChunkDistances();
        spawnEntities(this, SIZE);
    }

    private void generateWaterHeatMap() {
        for (int[] row : waterHeatMap) Arrays.fill(row, -1);

        Queue<int[]> queue = new LinkedList<>();

        for (int y = 0; y < CHUNK_ARRAY_SIZE; y++) {
            for (int x = 0; x < CHUNK_ARRAY_SIZE; x++) {
                if (!chunkMap[y][x].getWaterPositions().isEmpty()) {
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

    private void spawnEntities(WorldMap worldMap, int SIZE) {
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                // 1. Get the current tile type
                var tileType = worldMap.getTile(x, y);

                // 2. Select the appropriate spawn table based on the tile
                Map<String, Integer> currentSpawnTable = switch (tileType) {
                    case WATER -> waterSpawns;
                    case FOREST -> forestSpawns;
                    case GRASSLAND -> grasslandSpawns;
                    default -> null;
                };

                // 3. Roll the dice and spawn the entity if the tile has spawns defined
                if (currentSpawnTable != null) {
                    String chosenEntityType = getWeightedRandom(currentSpawnTable);

                    if (chosenEntityType != null) {
                        // 4. Match the string type to its specific recipe and spawn it
                        spawnByTypeName(chosenEntityType, x, y);
                    }
                }
            }
        }
    }

    private void spawnByTypeName(String type, int x, int y) {
        switch (type) {
            case "rabbit" -> AddEntity(Rabbit::new, x, y);
            case "tiger" -> AddEntity(Tiger::new, x, y);
            case "wolf" -> AddEntity(Wolf::new, x, y);
            case "fish" -> AddEntity(Fish::new, x, y);
            case "elephant" -> AddEntity(Elephant::new, x, y);
            case "tree" -> AddEntity(Trees::new, x, y);
            case "bush" -> AddEntity(Bush::new, x, y);
            default -> System.out.println("Unknown entity type: " + type);
        }
    }

    // Helper method to handle the weighted random math
    private String getWeightedRandom(Map<String, Integer> spawnTable) {
        // Keep your maximum pool scale at 5000
        int totalPool = 5000;

        int roll = random.nextInt(totalPool);
        int collectiveWeight = 0;

        for (Map.Entry<String, Integer> entry : spawnTable.entrySet()) {
            collectiveWeight += entry.getValue();
            if (roll < collectiveWeight) {
                return entry.getKey();
            }
        }

        // If the roll lands anywhere between collectiveWeight and 4999,
        // it returns null (leaving the tile empty).
        return null;
    }

    private <T extends Entity> void AddEntity(EntityFactory.FakeConstructor<T, Integer, Integer> recipe, int x, int y) {
        if (x < 0 || x >= worldMap.length || y < 0 || y >= worldMap[0].length) {
            return;
        }

        Entity tmp = EntityFactory.CreateEntity(recipe, x, y);
        chunkMap[y / CHUNK_SIZE][x / CHUNK_SIZE].addEntity(tmp);
    }

    public String getDecoration(int x, int y) {
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) return null;
        return decorationMap[y][x];
    }


    private void populateDecorationMap() {
        // Template decoration generator: cậu có thể chỉnh tỉ lệ, loại decoration cho từng terrain.
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                Terrain terrain = worldMap[y][x];
                if (terrain == null) continue;

                float chance = (float) Math.random();
                String decorKey = null;

                switch (terrain) {
                    case GRASSLAND -> {
                        if (chance < 0.08f) {
                            decorKey = chooseRandomDecoration(new String[]{"decor_grass_clump", "decor_flower_white"});
                        }
                    }
                    case FOREST -> {
                        if (chance < 0.07f) {
                            decorKey = chooseRandomDecoration(new String[]{"decor_leaf_pile", "decor_mushroom"});
                        }
                    }
                    case MOUNTAIN -> {
                        if (chance < 0.03f) {
                            decorKey = "decor_leaf_pile";
                        }
                    }
                    case MUD -> {
                        if (chance < 0.03f) {
                            decorKey = "decor_mushroom";
                        }
                    }
                    default -> {
                        // default không có overlay
                    }
                }

                decorationMap[y][x] = decorKey;
            }
        }
    }


    private String chooseRandomDecoration(String[] options) {
        if (options == null || options.length == 0) return null;
        int index = (int) (Math.random() * options.length);
        return options[index];
    }

    public Chunk[][] getChunkMap() {
        return chunkMap;
    }

}
