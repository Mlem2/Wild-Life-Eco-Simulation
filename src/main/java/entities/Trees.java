package entities;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import allEnum.Direction;
import entities.base.Entity;
import entities.base.EntityFactory;
import entities.base.Tree;

public class Trees extends Tree {

    public Trees(int x, int y) {
        super(x, y, 30, 30);
        this.defaultSeedCooldown = (random.nextInt(6) + 2) * 21600;
        this.currentSeedCooldown = defaultSeedCooldown;
        this.growthTime = (random.nextInt(3) + 2) * 21600;
        this.age = (random.nextInt(10) + 15) * 21600;
    }

    public void checkCD(Entity[][] animalCoordinates, List<Entity> allEntities) {
        if (age <= 0) {
            this.isAlive = false;
            return;
        }

        age--;
    }
}