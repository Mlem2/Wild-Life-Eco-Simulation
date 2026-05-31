package entities;

import entities.base.ResourceEntity;

public class Water extends ResourceEntity {
    public Water() {
        this("Water", 0, 0, 100, 1);
    }

    public Water(String name, int x, int y) {
        this(name, x, y, 100, 1);
    }

    public Water(String name, int x, int y, int maxAmount, int regenPerTick) {
        super(name, x, y, maxAmount, regenPerTick);
        setInfinite(true);
    }

    public int consume(int amount) {
        return consumeResource(amount);
    }

    public int getVolume() {
        return getCurrentAmount();
    }
}
