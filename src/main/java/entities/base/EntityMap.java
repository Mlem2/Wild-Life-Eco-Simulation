package entities.base;

import core.enviroment.WorldMap;
import entities.*;

import java.util.Map;
import java.util.Random;

public class EntityMap {
    private final Entity[][] entityMap;
    private final Random random = new Random();

    // Define spawn tables for each tile type using relative weights
    private final Map<String, Integer> waterSpawns = Map.of(
            "fish", 25
    );

    private final Map<String, Integer> forestSpawns = Map.of(
            "wolf", 3,
            "rabbit", 1,
            "tree", 200,
            "bush", 60
    );

    private final Map<String, Integer> grasslandSpawns = Map.of(
            "rabbit", 5,
            "wolf", 1,
            "elephant", 1,
            "tree", 10,
            "bush", 20
    );

    public EntityMap(WorldMap worldMap, int SIZE) {
        entityMap = new Entity[SIZE][SIZE];
        spawnEntities(worldMap, SIZE);
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
        if (x < 0 || x >= entityMap.length || y < 0 || y >= entityMap[0].length) {
            return;
        }

        if (entityMap[x][y] != null) {
            System.out.println("toa do da co thuc the");
            return;
        }
        Entity tmp = EntityFactory.<T>CreateEntity(recipe, x, y);
        entityMap[x][y] = tmp;
    }

    public Entity getEntity(int x, int y) {
        return this.entityMap[x][y];
    }
}