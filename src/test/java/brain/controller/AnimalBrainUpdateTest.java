package brain.controller;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import brain.pathfinder.Pathfinder;
import core.enviroment.Chunk;
import core.enviroment.WorldMap;
import entities.Food;
import entities.Rabbit;
import entities.Wolf;
import entities.base.Animals;
import entities.base.Position;

public class AnimalBrainUpdateTest {

    @Test
    void predatorShouldConsumeAdjacentPreyEvenWhenMovementCooldownIsActive() throws Exception {
        WorldMap worldMap = new WorldMap(123);
        MapSystem mapSystem = new MapSystem(worldMap);

        Wolf wolf = new Wolf("wolf", 10, 10);
        Rabbit rabbit = new Rabbit("rabbit", 11, 10);

        placeAnimal(worldMap, wolf);
        placeAnimal(worldMap, rabbit);

        setAnimalField(wolf, "hunger", 40.0);
        setAnimalField(wolf, "thirst", 50.0);
        wolf.setCurrentMoveCooldown(2);

        ChooseTarget targetSelector = new ChooseTarget(wolf, mapSystem);
        AnimalBrainUpdate brain = new AnimalBrainUpdate(wolf, targetSelector, new Pathfinder(worldMap), new ActionManager(wolf, mapSystem));

        assertEquals(rabbit, mapSystem.getEntityAt(new Position(11, 10)), "The nearby prey should be discoverable through map lookup.");

        brain.update();

        assertEquals(90.0, wolf.getHunger(), "A predator should recover hunger immediately from the prey's base recovery when it instantly consumes prey.");
        assertEquals(70.0, wolf.getThirst(), "A predator should recover thirst immediately from the prey's base recovery when it instantly consumes prey.");
        assertEquals(1, wolf.getCurrentMoveCooldown(), "The instant consume should consume the cooldown and immediately set the action timer.");
        assertEquals(null, mapSystem.getEntityAt(new Position(11, 10)), "The prey should be removed from the map immediately when consumed.");
        assertEquals(true, getBooleanField(rabbit, "isAlive"), "The prey object should remain, but it should be gone from the world after instant consumption.");
    }

    @Test
    void updateShouldRecomputePathWhenTargetChanges() throws Exception {
        WorldMap worldMap = new WorldMap(123);
        MapSystem mapSystem = new MapSystem(worldMap);

        Wolf wolf = new Wolf("wolf", 10, 10);
        Rabbit rabbit = new Rabbit("rabbit", 17, 17);

        placeAnimal(worldMap, wolf);
        placeAnimal(worldMap, rabbit);

        double hunger = 50;
        setAnimalField(wolf, "hunger", hunger);
        setAnimalField(wolf, "thirst", 50.0);
        wolf.setCurrentMoveCooldown(0);

        ChooseTarget targetSelector = new ChooseTarget(wolf, mapSystem);
        AnimalBrainUpdate brain = new AnimalBrainUpdate(wolf, targetSelector, new Pathfinder(worldMap), new ActionManager(wolf, mapSystem));

        brain.update();
        assertEquals(new Position(11, 11), wolf.getPosition(), "The predator should start chasing the nearby prey.");

        removeEntity(worldMap, rabbit);
        Food food = new Food("food", 17, 10);
        placeAnimal(worldMap, food);

        wolf.setCurrentMoveCooldown(0);
        brain.update();

        assertEquals(new Position(12, 11), wolf.getPosition(), "After changing targets, the animal should recompute its path instead of continuing the old chase.");
    }

    private static void placeAnimal(WorldMap worldMap, entities.base.Entity entity) throws Exception {
        Chunk chunk = getChunk(worldMap, entity);
        chunk.addEntity(entity);
    }

    private static void removeEntity(WorldMap worldMap, entities.base.Entity entity) throws Exception {
        Chunk chunk = getChunk(worldMap, entity);
        chunk.removeEntity(entity);
    }

    private static Chunk getChunk(WorldMap worldMap, entities.base.Entity entity) throws Exception {
        Field field = WorldMap.class.getDeclaredField("chunkMap");
        field.setAccessible(true);
        Chunk[][] chunkMap = (Chunk[][]) field.get(worldMap);
        int cx = entity.getX() / 50;
        int cy = entity.getY() / 50;
        return chunkMap[cy][cx];
    }

    private static void setAnimalField(Animals animal, String fieldName, double value) throws Exception {
        Field field = Animals.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.setDouble(animal, value);
    }

    private static boolean getBooleanField(Object target, String fieldName) throws Exception {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = field.get(target);
                return Boolean.TRUE.equals(value);
            } catch (NoSuchFieldException ignored) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    private static int getIntegerField(Object target, String fieldName) throws Exception {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.getInt(target);
            } catch (NoSuchFieldException ignored) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }
}
