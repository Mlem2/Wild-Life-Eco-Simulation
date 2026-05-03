package brain.strategy;

import entities.base.Animal;
import java.util.Random;

/*
 * Passive Strategy
 * Dùng cho động vật ăn cỏ/ hiền lành như:
 * - Thỏ
 * - Hươu
 * Hành vi:
 * - Di chuyển ngẫu nhiên
 * - Không săn đuổi
 */
public class PassiveStrategy implements MoveStrategy {

    // Random để tạo hướng đi ngẫu nhiên
    private Random random = new Random();

    @Override
    public void move(Animal animal) {

        /*
         * random.nextDouble()
         * trả về số từ 0 → 1
         * *2 -1 => khoảng [-1 ; 1]
         */

        double dx = random.nextDouble() * 2 - 1;
        double dy = random.nextDouble() * 2 - 1;

        /*
         * Cập nhật vị trí mới
         */
        animal.setX(animal.getX() + dx);
        animal.setY(animal.getY() + dy);
    }
}