package brain.scanner;

import entities.base.Animal;
import entities.base.Entity;
import core.manager.EntityManager;

/*
 * Scanner tìm entity gần nhất.
 *
 * Dùng cho:
 * - HunterStrategy
 * - ScaredStrategy
 */
public class TargetScanner {

    /*
     * Tìm entity gần nhất trong bán kính
     */
    public static Entity findNearest(Animal self, double radius) {

        Entity nearest = null;

        double minDistance = Double.MAX_VALUE;

        /*
         * Loop tất cả entity trong world
         */
        for(Entity entity : EntityManager.getEntities()) {

            /*
             * Không scan chính nó
             */
            if(entity == self) {
                continue;
            }

            /*
             * Tính khoảng cách
             */
            double distance = self.distanceTo(entity);

            /*
             * Trong radius
             */
            if(distance < radius && distance < minDistance) {

                nearest = entity;
                minDistance = distance;
            }
        }

        return nearest;
    }
}