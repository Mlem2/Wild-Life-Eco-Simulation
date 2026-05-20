package entities;

import AllEnum.Direction;
import entities.Base.Entity;
import entities.Base.EntityFactory;
import entities.Base.Tree;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Bush extends Tree {
    public Bush(String name, int x, int y){
        super(name,x,y);
        this.seedCD1 = (ran.nextInt(2)+5)*10800;
        this.seedCD2 = seedCD1;
        this.growthTime = (ran.nextInt(3)+2)*10800;
        this.age = (ran.nextInt(5)+3)*21600;
    }

    public void checkCD(Entity[][] toaDoSV, List<Entity> allEntities) {
        if (age <= 0) {
            this.isAlive = false;
            return;
        }

        if (growthTime <= 0) {
            seedCD2--;
            if (seedCD2 <= 0) {
                giveBirth(toaDoSV, allEntities);
            }
        } else {
            growthTime--;
        }
        age--;
    }

    public void giveBirth(Entity[][] toaDoSV, List<Entity> allEntities) {
        int treeCount = 0;
        int i2 = Math.min(x + 3, 499);
        int j2 = Math.min(y + 3, 499);

        // Đếm số lượng cây trong phạm vi bán kính 3 ô
        for (int i1 = Math.max(x - 3, 0); i1 <= i2; i1++) {
            for (int j1 = Math.max(y - 3, 0); j1 <= j2; j1++) {
                if (toaDoSV[i1][j1] != null && toaDoSV[i1][j1] instanceof Tree) {
                    treeCount++;
                }
            }
        }

        // Nếu số lượng cây xung quanh ít hơn 4, tiến hành sinh sản
        if (treeCount < 4) {
            Integer[] huong = {0, 1, 2, 3, 4, 5, 6, 7};
            Collections.shuffle(Arrays.asList(huong));

            for (int direct : huong) {
                int tmp1 = Direction.values()[direct].x;
                int tmp2 = Direction.values()[direct].y;

                int nextX = x + tmp1;
                int nextY = y + tmp2;

                // Kiểm tra điều kiện biên và ô trống
                if (nextX >= 0 && nextX <= 499 && nextY >= 0 && nextY <= 499) {
                    if (toaDoSV[nextX][nextY] == null) {
                        Bush tmp = EntityFactory.CreateEntity(Bush::new, name, nextX, nextY);
                        toaDoSV[nextX][nextY] = tmp;
                        allEntities.add(tmp);
                        seedCD2 = seedCD1;
                        break; // Sinh sản thành công một cây thì dừng vòng lặp
                    }
                }
            }
        }
    }
}