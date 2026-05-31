package entities;

import entities.base.ResourceEntity;

public class Food extends ResourceEntity {
    public Food() {
        this("Food", 0, 0, 100, 1);
    }

    public Food(String name, int x, int y) {
        this(name, x, y, 100, 1);
    }

    public Food(String name, int x, int y, int maxAmount, int regenPerTick) {
        super(name, x, y, maxAmount, regenPerTick);
    }

    public int consume(int amount) {
        return consumeResource(amount);
    }

    public int getNutrient() {
        return getCurrentAmount();
    }
}
