package entities;

import entities.base.ResourceEntity;

public class Water extends ResourceEntity {
    public Water() {
        this(0, 0, 100, 1);
    }

    public Water(int x, int y) {
        this(x, y, 100, 1);
    }

    public Water(int x, int y, int maxAmount, int regenPerTick) {
        super(x, y, maxAmount, regenPerTick);
        setInfinite(true);
    }

    public int consume(int amount) {
        return consumeResource(amount);
    }

    public int getVolume() {
        return getCurrentAmount();
    }
}
