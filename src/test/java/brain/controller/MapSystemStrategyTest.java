package brain.controller;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import core.enviroment.Chunk;
import core.enviroment.WorldMap;
import entities.Elephant;
import entities.Rabbit;
import entities.Tiger;
import entities.Wolf;

public class MapSystemStrategyTest {

    @Test
    void tigerAloneShouldNotBeScared() throws Exception {
        WorldMap worldMap = new WorldMap(123, 500);
        MapSystem mapSystem = new MapSystem(worldMap);

        Tiger tiger = new Tiger(10, 10);
        placeAnimal(worldMap, tiger);

        assertFalse(mapSystem.hasEnemyAround(tiger), "A predator alone should not flee just because it is an apex animal.");
        assertEquals(0, mapSystem.getEnemiesInChunk(getChunk(worldMap, tiger), tiger).size(),
                "Prey should not count as enemies for a predator.");
    }

    @Test
    void rabbitShouldFleeFromPredator() throws Exception {
        WorldMap worldMap = new WorldMap(123, 500);
        MapSystem mapSystem = new MapSystem(worldMap);

        Rabbit rabbit = new Rabbit(10, 10);
        Wolf wolf = new Wolf(12, 12);
        placeAnimal(worldMap, rabbit);
        placeAnimal(worldMap, wolf);

        assertTrue(mapSystem.hasEnemyAround(rabbit), "Herbivores should react to nearby predators.");
        assertEquals(1, mapSystem.getEnemiesInChunk(getChunk(worldMap, rabbit), rabbit).size(),
                "Only threatening predators should be reported as enemies.");
    }

    @Test
    void tigerShouldNotFleeFromElephantApex() throws Exception {
        WorldMap worldMap = new WorldMap(123, 500);
        MapSystem mapSystem = new MapSystem(worldMap);

        Tiger tiger = new Tiger(10, 10);
        Elephant elephant = new Elephant(12, 12);
        placeAnimal(worldMap, tiger);
        placeAnimal(worldMap, elephant);

        assertFalse(mapSystem.hasEnemyAround(tiger), "Apex herbivores should not be treated as threats to predators.");
        assertEquals(0, mapSystem.getEnemiesInChunk(getChunk(worldMap, tiger), tiger).size(),
                "Predators should not flee from non-predatory apex animals.");
    }

    private static void placeAnimal(WorldMap worldMap, entities.base.Animals animal) throws Exception {
        Chunk chunk = getChunk(worldMap, animal);
        chunk.addEntity(animal);
    }

    private static Chunk getChunk(WorldMap worldMap, entities.base.Animals animal) throws Exception {
        Field field = WorldMap.class.getDeclaredField("chunkMap");
        field.setAccessible(true);
        Chunk[][] chunkMap = (Chunk[][]) field.get(worldMap);
        int cx = animal.getX() / 50;
        int cy = animal.getY() / 50;
        return chunkMap[cy][cx];
    }
}
