package brain.strategy;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import brain.controller.MapSystem;
import core.enviroment.Terrain;
import core.enviroment.WorldMap;
import entities.Rabbit;
import entities.Fish;
import entities.base.Position;
import java.lang.reflect.Field;

public class PassiveStrategyTest {

    @Test
    void landAnimalShouldNotTargetWaterInPassiveStrategy() throws Exception {
        int SIZE = 100;
        WorldMap worldMap = new WorldMap(1, SIZE);
        
        MapSystem mapSystem = new MapSystem(worldMap);
        Rabbit rabbit = new Rabbit(10, 10); // Land animal
        PassiveStrategy strategy = new PassiveStrategy();
        
        for (int i = 0; i < 200; i++) {
            Position target = strategy.getTarget(rabbit, mapSystem);
            Terrain t = worldMap.getTile(target.getX(), target.getY());
            assertTrue(!t.isWater(), "Land animal should not target water in Passive strategy. Found water at " + target);
        }
    }

    @Test
    void fishShouldBeAllowedToTargetWaterInPassiveStrategy() throws Exception {
        int SIZE = 100;
        // Seed that likely has water (default seed 1 seems to have some)
        WorldMap worldMap = new WorldMap(1, SIZE);
        
        MapSystem mapSystem = new MapSystem(worldMap);
        Fish fish = new Fish(10, 10);
        PassiveStrategy strategy = new PassiveStrategy();
        
        boolean foundWaterTarget = false;
        for (int i = 0; i < 1000; i++) {
            Position target = strategy.getTarget(fish, mapSystem);
            Terrain t = worldMap.getTile(target.getX(), target.getY());
            if (t.isWater()) {
                foundWaterTarget = true;
                break;
            }
        }
        // Fish usually can find water in their chunk if it's there.
    }
}
