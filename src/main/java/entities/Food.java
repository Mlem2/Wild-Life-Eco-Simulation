package entities;

import entities.base.ResourceEntity;

public class Food extends ResourceEntity {
    public Food() {
        this(0, 0, 100, 1);
    }

    public Food(int x, int y) {
        this(x, y, 100, 1);
    }

    public Food(int x, int y, int maxAmount, int regenPerTick) {
        super(x, y, maxAmount, regenPerTick);
    }

    public int consume(int amount) {
        return consumeResource(amount);
    }

    public int getNutrient() {
        return getCurrentAmount();
    }
}
