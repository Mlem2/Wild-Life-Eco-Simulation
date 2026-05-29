package entities;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import allEnum.Direction;
import entities.base.Entity;
import entities.base.EntityFactory;
import entities.base.Tree;

public class Bush extends Tree {
    public Bush(String name, int x, int y){
        super(name,x,y);
        setRestoreAmount(10);
        setHungerRecoveryAmount(20);
        setThirstRecoveryAmount(20);
        this.defaultSeedCooldown = (random.nextInt(2) + 5) * 10800;
        this.currentSeedCooldown = defaultSeedCooldown;
        this.growthTime = (random.nextInt(3) + 2) * 10800;
        this.age = (random.nextInt(5) + 3) * 21600;
    }

    public void checkCD(Entity[][] animalCoordinates, List<Entity> allEntities) {
        updateResourceState();

        if (age <= 0) {
            this.isAlive = false;
            return;
        }

        if (growthTime <= 0) {
            currentSeedCooldown--;
            if (currentSeedCooldown <= 0) {
                giveBirth(animalCoordinates, allEntities);
            }
        } else {
            growthTime--;
        }
        age--;
    }

    public void giveBirth(Entity[][] animalCoordinates, List<Entity> allEntities) {
        int treeCount = 0;
        int i2 = Math.min(x + 3, 499);
        int j2 = Math.min(y + 3, 499);

        // Đếm số lượng cây trong phạm vi bán kính 3 ô
        for (int i1 = Math.max(x - 3, 0); i1 <= i2; i1++) {
            for (int j1 = Math.max(y - 3, 0); j1 <= j2; j1++) {
                if (animalCoordinates[i1][j1] != null && animalCoordinates[i1][j1] instanceof Tree) {
                    treeCount++;
                }
            }
        }

        // Nếu số lượng cây xung quanh ít hơn 4, tiến hành sinh sản
        if (treeCount < 4) {
            Integer[] directions = {0, 1, 2, 3, 4, 5, 6, 7};
            Collections.shuffle(Arrays.asList(directions));

            for (int direction : directions) {
                int tmp1 = Direction.values()[direction].x;
                int tmp2 = Direction.values()[direction].y;

                int nextX = x + tmp1;
                int nextY = y + tmp2;

                // Kiểm tra điều kiện biên và ô trống
                if (nextX >= 0 && nextX <= 499 && nextY >= 0 && nextY <= 499) {
                    if (animalCoordinates[nextX][nextY] == null) {
                        Bush tmp = EntityFactory.CreateEntity(Bush::new, name, nextX, nextY);
                        animalCoordinates[nextX][nextY] = tmp;
                        allEntities.add(tmp);

                        currentSeedCooldown = defaultSeedCooldown;
                        break; // Sinh sản thành công một cây thì dừng vòng lặp
                    }
                }
            }
        }
    }
}