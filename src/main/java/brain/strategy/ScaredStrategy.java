package brain.strategy;

import entities.base.Animal;
import entities.base.Entity;
import brain.scanner.TargetScanner;

/*
 * Scared Strategy
 *
 * Dùng cho prey.
 *
 * Hành vi:
 * - Tìm predator gần nhất
 * - Chạy ngược hướng
 */
public class ScaredStrategy implements MoveStrategy {

    private static final double SCAN_RADIUS = 120;

    @Override
    public void move(Animal animal) {

        /*
         * Tìm predator gần nhất
         */
        Entity predator = TargetScanner.findNearest(animal, SCAN_RADIUS);

        /*
         * Không có nguy hiểm
         */
        if(predator == null) {
            return;
        }

        /*
         * Vector chạy ngược
         */
        double dx = animal.getX() - predator.getX();
        double dy = animal.getY() - predator.getY();

        double distance = Math.sqrt(dx * dx + dy * dy);

        if(distance == 0) {
            return;
        }

        /*
         * Normalize
         */
        dx /= distance;
        dy /= distance;

        /*
         * Speed chạy trốn
         */
        double speed = 2.0;

        /*
         * Move away
         */
        animal.setX(animal.getX() + dx * speed);
        animal.setY(animal.getY() + dy * speed);
    }
}