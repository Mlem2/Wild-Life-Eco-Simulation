package entities;

import java.util.List;

import entities.base.Entity;
import entities.base.Plant;

public class Bush extends Plant {
    public Bush(int x, int y){
        super(x, y, 20, 20);
        this.defaultSeedCooldown = (random.nextInt(2) + 5) * 10800;
        this.currentSeedCooldown = defaultSeedCooldown;
        this.growthTime = (random.nextInt(3) + 2) * 10800;
        this.maxAge = (random.nextInt(8) + 10) * 21600;
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