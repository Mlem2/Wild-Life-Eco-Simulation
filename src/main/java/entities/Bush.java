package entities;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import allEnum.Direction;
import entities.base.Entity;
import entities.base.EntityFactory;
import entities.base.Tree;

public class Bush extends Tree {
    public Bush(int x, int y){
        super(x, y, 20, 20);
        this.defaultSeedCooldown = (random.nextInt(2) + 5) * 10800;
        this.currentSeedCooldown = defaultSeedCooldown;
        this.growthTime = (random.nextInt(3) + 2) * 10800;
        this.age = (random.nextInt(8) + 10) * 21600;
    }

    public void checkCD(Entity[][] animalCoordinates, List<Entity> allEntities) {
        if (age <= 0) {
            this.isAlive = false;
            return;
        }

        age--;
    }
}