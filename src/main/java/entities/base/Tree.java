package entities.base;

import java.util.List;
import java.util.Random;

import entities.Food;

public abstract class Tree extends Food {
    protected int age;
    protected double defaultSeedCooldown;
    protected double growthTime;
    protected double currentSeedCooldown;
    protected static Random random = new Random();

    public Tree(String name, int x, int y){
        super(name, x, y, 100, 1);
        setRestoreAmount(12);
    }

    public Tree(){}

    public abstract void checkCD(Entity[][] animalCoordinates, List<Entity> allEntities);

}