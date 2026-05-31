package core.enviroment;

import entities.*;
import core.enviroment.Chunk;
import java.util.Random;
import java.util.function.BiFunction;

public class EntitySpawner {
    private final WorldMap worldMap;
    private final int gridSize;
    private final Random rand = new Random();

    public EntitySpawner(WorldMap worldMap, int gridSize) {
        this.worldMap = worldMap;
        this.gridSize = gridSize;
    }

    public void spawnInitialEntities() {
        Chunk[][] chunkMap = worldMap.getChunkMap();
        if (chunkMap == null) return;

        // Configuration for initial population
        int spawnRabbits = 150;
        int spawnTigers = 20;
        int spawnWolves = 35;
        int spawnElephants = 15;
        int spawnFishes = 80;
        int spawnBushes = 400;
        int spawnTrees = 300;

        BiFunction<Integer, Integer, String> getTileType = (x, y) -> {
            try { return worldMap.getTile(x, y).getName().toLowerCase(); } catch (Exception e) { return ""; }
        };

        // Spawn Rabbits
        for (int countR = 0; countR < spawnRabbits; ) {
            int rx = rand.nextInt(gridSize), ry = rand.nextInt(gridSize);
            String name = getTileType.apply(rx, ry);
            if (!name.contains("water") && !name.contains("nuoc") && !name.contains("stone") && !name.contains("da")) {
                chunkMap[ry / 50][rx / 50].addEntity(new Rabbit(rx, ry));
                countR++;
            }
        }
        // Spawn Tigers
        for (int countT = 0; countT < spawnTigers; ) {
            int rx = rand.nextInt(gridSize), ry = rand.nextInt(gridSize);
            String name = getTileType.apply(rx, ry);
            if (!name.contains("water") && !name.contains("nuoc") && !name.contains("stone") && !name.contains("da")) {
                chunkMap[ry / 50][rx / 50].addEntity(new Tiger(rx, ry));
                countT++;
            }
        }
        // Spawn Wolves
        for (int countW = 0; countW < spawnWolves; ) {
            int rx = rand.nextInt(gridSize), ry = rand.nextInt(gridSize);
            String name = getTileType.apply(rx, ry);
            if (!name.contains("water") && !name.contains("nuoc") && !name.contains("stone") && !name.contains("da")) {
                chunkMap[ry / 50][rx / 50].addEntity(new Wolf(rx, ry));
                countW++;
            }
        }
        // Spawn Elephants
        for (int countE = 0; countE < spawnElephants; ) {
            int rx = rand.nextInt(gridSize), ry = rand.nextInt(gridSize);
            String name = getTileType.apply(rx, ry);
            if (!name.contains("water") && !name.contains("nuoc") && !name.contains("stone") && !name.contains("da")) {
                chunkMap[ry / 50][rx / 50].addEntity(new Elephant(rx, ry));
                countE++;
            }
        }
        // Spawn Fish (Water only)
        for (int countF = 0; countF < spawnFishes; ) {
            int rx = rand.nextInt(gridSize), ry = rand.nextInt(gridSize);
            String name = getTileType.apply(rx, ry);
            if (name.contains("water") || name.contains("nuoc")) {
                chunkMap[ry / 50][rx / 50].addEntity(new Fish(rx, ry));
                countF++;
            }
        }
        // Spawn Bushes
        for (int countB = 0; countB < spawnBushes; ) {
            int rx = rand.nextInt(gridSize), ry = rand.nextInt(gridSize);
            String name = getTileType.apply(rx, ry);
            if (!name.contains("water") && !name.contains("nuoc") && !name.contains("stone") && !name.contains("da") && !name.contains("mud") && !name.contains("bun")) {
                chunkMap[ry / 50][rx / 50].addEntity(new Bush(rx, ry));
                countB++;
            }
        }
        // Spawn Trees
        for (int countTree = 0; countTree < spawnTrees; ) {
            int rx = rand.nextInt(gridSize), ry = rand.nextInt(gridSize);
            String name = getTileType.apply(rx, ry);
            if (!name.contains("water") && !name.contains("nuoc") && !name.contains("stone") && !name.contains("da") && !name.contains("mud") && !name.contains("bun")) {
                chunkMap[ry / 50][rx / 50].addEntity(new Trees(rx, ry));
                countTree++;
            }
        }
        System.out.println("🟢 Initial full ecosystem random setup successfully!");
    }
}
