package entities;

import java.util.List;

import entities.base.Entity;
import entities.base.Plant;

public class Trees extends Plant {

    public Trees(int x, int y) {
        super(x, y, 30, 30);
        this.defaultSeedCooldown = (random.nextInt(6) + 2) * 21600;
        this.currentSeedCooldown = defaultSeedCooldown;
        this.growthTime = (random.nextInt(3) + 2) * 21600;
        this.maxAge = (random.nextInt(10) + 15) * 21600;
        this.age = random.nextInt(this.maxAge);
    }

    public void checkCD(Entity[][] animalCoordinates, List<Entity> allEntities) {
        if (age <= 0) {
            this.isAlive = false;
            return;
        }

        age--;
    }
}