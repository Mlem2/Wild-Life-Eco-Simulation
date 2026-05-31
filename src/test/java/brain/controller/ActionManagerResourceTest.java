package brain.controller;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;

import core.enviroment.Chunk;
import core.enviroment.WorldMap;
import entities.Food;
import entities.Rabbit;
import entities.Water;
import entities.Wolf;
import entities.base.Animals;

public class ActionManagerResourceTest {

    @Test
    void eatingAndDrinkingShouldRestoreAnimalStats() throws Exception {
        Rabbit rabbit = new Rabbit(0, 0);
        setAnimalStat(rabbit, "hunger", 50.0);
        setAnimalStat(rabbit, "thirst", 40.0);

        ActionManager actionManager = new ActionManager(rabbit, new MapSystem());
        Food food = new Food(0, 0, 100, 1);
        Water water = new Water(0, 0, 100, 1);

        actionManager.eat(food);
        assertEquals(60.0, rabbit.getHungerPercentage(), 0.0001,
                "Eating should restore hunger using the consumed food's base recovery.");
        assertEquals(50.0, rabbit.getThirstPercentage(), 0.0001,
                "Eating should also restore thirst using the consumed food's base recovery.");

        actionManager.drink(water);
        assertEquals(70.0, rabbit.getThirstPercentage(), 0.0001,
                "Drinking should restore thirst using the rabbit's recovery value.");
    }

    @Test
    void eatingPreyShouldInstantlyRemoveItAndRestorePredatorHungerAndThirst() throws Exception {
        WorldMap worldMap = new WorldMap(123, 500);
        MapSystem mapSystem = new MapSystem(worldMap);

        Wolf wolf = new Wolf(0, 0);
        setAnimalStat(wolf, "hunger", 40.0);
        setAnimalStat(wolf, "thirst", 40.0);

        Rabbit prey = new Rabbit(0, 0);

        Chunk chunk = getChunk(worldMap, wolf);
        chunk.addEntity(wolf);
        chunk.addEntity(prey);

        ActionManager actionManager = new ActionManager(wolf, mapSystem);
        actionManager.eat(prey);

        assertEquals(90.0, wolf.getHungerPercentage(), 0.0001, "Eating should restore the predator's hunger from the prey's base recovery.");
        assertEquals(60.0, wolf.getThirstPercentage(), 0.0001, "Eating should restore the predator's thirst from the prey's base recovery.");
        assertFalse(chunk.contains(prey), "The prey should be removed from the chunk immediately when eaten.");
    }

    private static void setAnimalStat(Animals animal, String fieldName, double value) throws Exception {
        Field field = animal.getClass().getSuperclass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.setDouble(animal, value);
    }

    private static void setAnimalStat(Animals animal, String fieldName, int value) throws Exception {
        Field field = animal.getClass().getSuperclass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.setInt(animal, value);
    }

    private static Chunk getChunk(WorldMap worldMap, entities.base.Entity entity) throws Exception {
        Field field = WorldMap.class.getDeclaredField("chunkMap");
        field.setAccessible(true);
        Chunk[][] chunkMap = (Chunk[][]) field.get(worldMap);
        int cx = entity.getX() / 50;
        int cy = entity.getY() / 50;
        return chunkMap[cy][cx];
    }
}
