package brain.strategy;

import entities.base.Animal;
import entities.base.Entity;
import brain.scanner.TargetScanner;

/*
 * Hunter Strategy
 *
 * Dành cho thú săn mồi.
 *
 * Hành vi:
 * - Tìm prey gần nhất
 * - Di chuyển tới prey
 */
public class HunterStrategy implements MoveStrategy {

    // Khoảng cách scan mục tiêu
    private static final double SCAN_RADIUS = 150;

    @Override
    public void move(Animal animal) {

        /*
         * Tìm prey gần nhất
         */
        Entity target = TargetScanner.findNearest(animal, SCAN_RADIUS);

        /*
         * Nếu không có mục tiêu → đứng yên
         */
        if(target == null) {
            return;
        }

        /*
         * Vector hướng tới mục tiêu
         */
        double dx = target.getX() - animal.getX();
        double dy = target.getY() - animal.getY();

        /*
         * Khoảng cách
         */
        double distance = Math.sqrt(dx * dx + dy * dy);

        /*
         * Tránh chia cho 0
         */
        if(distance == 0) {
            return;
        }

        /*
         * Normalize vector
         */
        dx /= distance;
        dy /= distance;

        /*
         * Tốc độ predator
         */
        double speed = 1.5;

        /*
         * Move
         */
        animal.setX(animal.getX() + dx * speed);
        animal.setY(animal.getY() + dy * speed);
    }
}