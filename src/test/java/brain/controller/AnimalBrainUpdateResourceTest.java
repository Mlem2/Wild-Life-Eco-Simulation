package brain.controller;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import brain.pathfinder.Pathfinder;
import core.enviroment.Chunk;
import core.enviroment.WorldMap;
import entities.Bush;
import entities.Food;
import entities.Rabbit;
import entities.Water;
import entities.Wolf;
import entities.base.Animals;

public class AnimalBrainUpdateResourceTest {

    @Test
    void rabbitShouldEatAdjacentFoodOnly() throws Exception {
        WorldMap worldMap = new WorldMap(123, 500);
        MapSystem mapSystem = new MapSystem(worldMap);

        Rabbit rabbit = new Rabbit(0, 0);
        rabbit.setCurrentMoveCooldown(0);
        setAnimalStat(rabbit, "hunger", 40.0);
        setAnimalStat(rabbit, "thirst", 40.0);
        placeEntity(worldMap, rabbit);

        Food food = new Food(1, 0, 100, 1);
        placeEntity(worldMap, food);

        ActionManager actionManager = new ActionManager(rabbit, mapSystem);
        ChooseTarget chooseTarget = new ChooseTarget(rabbit, mapSystem);
        AnimalBrainUpdate animalBrainUpdate = new AnimalBrainUpdate(rabbit, chooseTarget, new Pathfinder(worldMap), actionManager);

        animalBrainUpdate.update();

        assertEquals(50.0, rabbit.getHungerPercentage(), 0.0001,
                "A rabbit should eat food in the adjacent tile using the consumed food's base hunger recovery.");
        assertEquals(50.0, rabbit.getThirstPercentage(), 0.0001,
                "A rabbit should recover thirst from the food it consumes.");
    }

    @Test
    void rabbitShouldConsumeBushWhenReachingTarget() throws Exception {
        WorldMap worldMap = new WorldMap(123, 500);
        MapSystem mapSystem = new MapSystem(worldMap);

        Rabbit rabbit = new Rabbit(0, 0);
        rabbit.setCurrentMoveCooldown(0);
        setAnimalStat(rabbit, "hunger", 40.0);
        setAnimalStat(rabbit, "thirst", 40.0);
        placeEntity(worldMap, rabbit);

        Bush bush = new Bush(1, 0);
        placeEntity(worldMap, bush);

        ActionManager actionManager = new ActionManager(rabbit, mapSystem);
        ChooseTarget chooseTarget = new ChooseTarget(rabbit, mapSystem);
        AnimalBrainUpdate animalBrainUpdate = new AnimalBrainUpdate(rabbit, chooseTarget, new Pathfinder(worldMap), actionManager);

        animalBrainUpdate.update();
        rabbit.setCurrentMoveCooldown(0);
        animalBrainUpdate.update();

        assertEquals(60.0, rabbit.getHungerPercentage(), 0.0001,
                "A rabbit should eat the bush it reaches using the bush's base hunger recovery.");
        assertEquals(60.0, rabbit.getThirstPercentage(), 0.0001,
                "A rabbit should recover thirst from the bush it consumes.");
        assertFalse(getChunk(worldMap, bush).getEntityList().contains(bush),
                "Consumed bushes should disappear from the map.");
    }

    @Test
    void rabbitShouldDrinkAdjacentWaterOnly() throws Exception {
        WorldMap worldMap = new WorldMap(123, 500);
        MapSystem mapSystem = new MapSystem(worldMap);

        Rabbit rabbit = new Rabbit(0, 0);
        rabbit.setCurrentMoveCooldown(0);
        setAnimalStat(rabbit, "thirst", 40.0);
        placeEntity(worldMap, rabbit);

        Water water = new Water(1, 0);
        placeEntity(worldMap, water);

        ActionManager actionManager = new ActionManager(rabbit, mapSystem);
        ChooseTarget chooseTarget = new ChooseTarget(rabbit, mapSystem);
        AnimalBrainUpdate animalBrainUpdate = new AnimalBrainUpdate(rabbit, chooseTarget, new Pathfinder(worldMap), actionManager);

        animalBrainUpdate.update();

        assertEquals(60.0, rabbit.getThirstPercentage(), 0.0001,
                "A rabbit should drink water only from an adjacent tile.");
    }

    @Test
    void predatorShouldAttackAdjacentPreyOnly() throws Exception {
        WorldMap worldMap = new WorldMap(123, 500);
        MapSystem mapSystem = new MapSystem(worldMap);

        Wolf wolf = new Wolf(0, 0);
        wolf.setCurrentMoveCooldown(0);
        setAnimalStat(wolf, "hunger", 40.0);
        setAnimalStat(wolf, "thirst", 40.0);
        placeEntity(worldMap, wolf);

        Rabbit prey = new Rabbit(1, 0);
        placeEntity(worldMap, prey);

        ActionManager actionManager = new ActionManager(wolf, mapSystem);
        ChooseTarget chooseTarget = new ChooseTarget(wolf, mapSystem);
        AnimalBrainUpdate animalBrainUpdate = new AnimalBrainUpdate(wolf, chooseTarget, new Pathfinder(worldMap), actionManager);

        animalBrainUpdate.update();

        assertEquals(90.0, wolf.getHungerPercentage(), 0.0001, "A predator should recover hunger instantly from the prey's base hunger recovery.");
        assertEquals(60.0, wolf.getThirstPercentage(), 0.0001, "A predator should recover thirst from the prey's base thirst recovery.");
        assertTrue(prey.checkAlive(), "Prey should be instant-consumed without relying on HP damage.");
        assertFalse(getChunk(worldMap, prey).contains(prey), "Consumed prey should be removed from the map.");
    }

    @Test
    void predatorShouldAttackPreyOnItsOwnTileAndRecoverHungerAndThirst() throws Exception {
        WorldMap worldMap = new WorldMap(123, 500);
        MapSystem mapSystem = new MapSystem(worldMap);

        Wolf wolf = new Wolf(0, 0);
        wolf.setCurrentMoveCooldown(0);
        setAnimalStat(wolf, "hunger", 40.0);
        setAnimalStat(wolf, "thirst", 40.0);
        placeEntity(worldMap, wolf);

        Rabbit prey = new Rabbit(0, 0);
        placeEntity(worldMap, prey);

        ActionManager actionManager = new ActionManager(wolf, mapSystem);
        ChooseTarget chooseTarget = new ChooseTarget(wolf, mapSystem);
        AnimalBrainUpdate animalBrainUpdate = new AnimalBrainUpdate(wolf, chooseTarget, new Pathfinder(worldMap), actionManager);

        animalBrainUpdate.update();

        assertEquals(90.0, wolf.getHungerPercentage(), 0.0001, "A successful instant consumption should restore the predator's hunger from the prey's base recovery.");
        assertEquals(60.0, wolf.getThirstPercentage(), 0.0001, "A successful instant consumption should restore the predator's thirst from the prey's base recovery.");
        assertTrue(prey.checkAlive(), "Prey should be instant-consumed without HP damage.");
        assertFalse(getChunk(worldMap, prey).contains(prey), "Consumed prey should be removed from the map.");
    }

    @Test
    void rabbitShouldEatFoodOnItsOwnTile() throws Exception {
        WorldMap worldMap = new WorldMap(123, 500);
        MapSystem mapSystem = new MapSystem(worldMap);

        Rabbit rabbit = new Rabbit(0, 0);
        rabbit.setCurrentMoveCooldown(0);
        setAnimalStat(rabbit, "hunger", 40.0);
        setAnimalStat(rabbit, "thirst", 40.0);
        placeEntity(worldMap, rabbit);

        Food food = new Food(0, 0, 100, 1);
        placeEntity(worldMap, food);

        ActionManager actionManager = new ActionManager(rabbit, mapSystem);
        ChooseTarget chooseTarget = new ChooseTarget(rabbit, mapSystem);
        AnimalBrainUpdate animalBrainUpdate = new AnimalBrainUpdate(rabbit, chooseTarget, new Pathfinder(worldMap), actionManager);

        animalBrainUpdate.update();

        assertEquals(50.0, rabbit.getHungerPercentage(), 0.0001,
                "An animal should consume food placed on its own tile using the food's base hunger recovery.");
        assertEquals(50.0, rabbit.getThirstPercentage(), 0.0001,
                "An animal should recover thirst from food placed on its own tile.");
    }

    private static void placeEntity(WorldMap worldMap, entities.base.Entity entity) throws Exception {
        Chunk chunk = getChunk(worldMap, entity);
        chunk.addEntity(entity);
    }

    private static Chunk getChunk(WorldMap worldMap, entities.base.Entity entity) throws Exception {
        Field field = WorldMap.class.getDeclaredField("chunkMap");
        field.setAccessible(true);
        Chunk[][] chunkMap = (Chunk[][]) field.get(worldMap);
        int cx = entity.getX() / 50;
        int cy = entity.getY() / 50;
        return chunkMap[cy][cx];
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
}
