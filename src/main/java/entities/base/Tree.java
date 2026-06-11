package entities.base;

import java.util.List;
import java.util.Random;

public abstract class Tree extends Entity{
    protected double defaultSeedCooldown;
    protected double growthTime;
    protected double currentSeedCooldown;
    protected static Random random = new Random();
    protected int hungerRecoveryAmount;
    protected int thirstRecoveryAmount;

    public Tree(int x, int y, int hungerRecoveryAmount, int thirstRecoveryAmount){
        super(x, y);
        this.hungerRecoveryAmount = hungerRecoveryAmount;
        this.thirstRecoveryAmount = thirstRecoveryAmount;
    }

    public Tree(){}

    public abstract void checkCD(Entity[][] animalCoordinates, List<Entity> allEntities);

}