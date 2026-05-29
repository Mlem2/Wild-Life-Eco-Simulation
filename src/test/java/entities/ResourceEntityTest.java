package entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class ResourceEntityTest {

    @Test
    void treeFoodShouldConsumeAndRegenerateAfterConsumption() {
        Trees tree = new Trees("tree", 0, 0);

        assertTrue(tree instanceof Food, "Trees should inherit the food resource behavior.");
        assertEquals(false, tree.isInfinite(), "Tree food sources should be finite and deplete after consumption.");

        int consumed = tree.consume(40);
        assertEquals(40, consumed, "Consuming should report the amount taken from the surface.");
        assertEquals(60, tree.getCurrentAmount(), "Finite food should lose stock after consumption.");

        tree.updateResourceState();
        assertEquals(61, tree.getCurrentAmount(), "Finite food should regenerate by its restore rate after a tick.");
    }

    @Test
    void genericFoodShouldExposeBaseRecoveryAmounts() {
        Food food = new Food("grass", 0, 0, 100, 1);

        assertEquals(10, food.getHungerRecoveryAmount(), "Generic food should provide a base hunger recovery amount.");
        assertEquals(10, food.getThirstRecoveryAmount(), "Generic food should provide a base thirst recovery amount.");
    }

    @Test
    void bushAndTreeFoodShouldExposeTheirBaseRecoveryAmounts() {
        Bush bush = new Bush("bush", 0, 0);
        Trees tree = new Trees("tree", 0, 0);

        assertEquals(20, bush.getHungerRecoveryAmount(), "Bushes should provide a higher base hunger recovery than generic grass.");
        assertEquals(20, bush.getThirstRecoveryAmount(), "Bushes should provide a higher base thirst recovery than generic grass.");
        assertEquals(30, tree.getHungerRecoveryAmount(), "Trees should provide the strongest base hunger recovery.");
        assertEquals(30, tree.getThirstRecoveryAmount(), "Trees should provide the strongest base thirst recovery.");
    }

    @Test
    void waterShouldRemainInfiniteForDrinkConsumption() {
        Water water = new Water("water", 0, 0);

        assertTrue(water.isInfinite(), "Water sources should be infinite.");

        int consumed = water.consume(40);
        assertEquals(40, consumed, "Drinking should report the requested amount.");
        assertEquals(water.getMaxAmount(), water.getCurrentAmount(), "Infinite water should not be depleted by drinking.");
    }
}
