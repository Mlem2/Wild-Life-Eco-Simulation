package entities;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import brain.controller.MapSystem;
import brain.controller.ActionManager;
import core.enviroment.Terrain;
import core.enviroment.WorldMap;
import entities.Fish;
import entities.Rabbit;
import entities.base.Position;

public class FishMovementTest {

    @Test
    void fishShouldNotMoveToLand() {
        int SIZE = 500; // Increased size to increase chance of finding both
        // Manual world creation to ensure we have land and water
        WorldMap worldMap = new WorldMap(9403312, SIZE); // Use the seed from MapTest/PathfindingTest
        MapSystem mapSystem = new MapSystem(worldMap);
        
        // Find a water tile and a land tile
        Position waterPos = null;
        Position landPos = null;
        
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                Terrain t = worldMap.getTile(x, y);
                if (t.isWater() && waterPos == null) {
                    waterPos = Position.of(x, y);
                } else if (!t.isWater() && t.isPassable() && landPos == null) {
                    landPos = Position.of(x, y);
                }
                if (waterPos != null && landPos != null) break;
            }
            if (waterPos != null && landPos != null) break;
        }
        
        assertTrue(waterPos != null, "Should find water tile");
        assertTrue(landPos != null, "Should find land tile");
        
        Fish fish = new Fish(waterPos.getX(), waterPos.getY());
        ActionManager actionManager = new ActionManager(fish, mapSystem);
        
        // Try to move fish to land
        actionManager.move(landPos);
        
        // Fish should stay in waterPos
        assertTrue(fish.getPosition().equals(waterPos), "Fish should not move to land. Current position: " + fish.getPosition());
    }

    @Test
    void rabbitShouldBeAbleToMoveToLand() {
        int SIZE = 50;
        WorldMap worldMap = new WorldMap(1, SIZE);
        MapSystem mapSystem = new MapSystem(worldMap);
        
        Position landPos1 = null;
        Position landPos2 = null;
        
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                Terrain t = worldMap.getTile(x, y);
                if (!t.isWater() && t.isPassable()) {
                    if (landPos1 == null) {
                        landPos1 = Position.of(x, y);
                    } else if (landPos2 == null) {
                        landPos2 = Position.of(x, y);
                        break;
                    }
                }
            }
            if (landPos2 != null) break;
        }
        
        Rabbit rabbit = new Rabbit(landPos1.getX(), landPos1.getY());
        ActionManager actionManager = new ActionManager(rabbit, mapSystem);
        
        actionManager.move(landPos2);
        
        assertTrue(rabbit.getPosition().equals(landPos2), "Rabbit should be able to move to land");
    }
}
