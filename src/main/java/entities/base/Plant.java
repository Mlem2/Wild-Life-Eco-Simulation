package entities.base;

import java.util.List;
import java.util.Random;

public abstract class Plant extends Entity{
    protected double defaultSeedCooldown;
    protected double growthTime;
    protected double currentSeedCooldown;
    protected static Random random = new Random();
    protected int hungerRecoveryAmount;
    protected int thirstRecoveryAmount;

    public Plant(int x, int y, int hungerRecoveryAmount, int thirstRecoveryAmount){
        super(x, y);
        this.hungerRecoveryAmount = hungerRecoveryAmount;
        this.thirstRecoveryAmount = thirstRecoveryAmount;
    }

    public Plant(){}

    public abstract void checkCD(Entity[][] animalCoordinates, List<Entity> allEntities);

}