package entities.base;

import java.util.List;
import java.util.Random;

import entities.Food;

public abstract class Tree extends Food {
    protected double defaultSeedCooldown;
    protected double growthTime;
    protected double currentSeedCooldown;
    protected int maxAge;
    protected static final double MIN_RENDER_SCALE = 0.65;
    protected static final double MAX_RENDER_SCALE = 1.15;
    protected static Random random = new Random();

    public Tree(int x, int y){
        super(x, y, 100, 1);
        setRestoreAmount(12);
    }

    public double getGrowthPercent() {
        if (maxAge <= 0) return 1.0;
        double percent = 1.0 - ((double) age / maxAge);
        return Math.min(1.0, Math.max(0.0, percent));
    }

    public double getRenderScale() {
        double percent = getGrowthPercent();
        return MIN_RENDER_SCALE + (MAX_RENDER_SCALE - MIN_RENDER_SCALE) * percent;
    }

    public int getGrowthStage() {
        double percent = getGrowthPercent();
        if (percent < 0.35) return 0;
        if (percent < 0.75) return 1;
        return 2;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public Tree(){}

    public abstract void checkCD(Entity[][] animalCoordinates, List<Entity> allEntities);

}