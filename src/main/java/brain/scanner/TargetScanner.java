package brain.scanner;

import entities.base.Animals;
import entities.base.Entity;
import java.util.List;
import java.util.function.Predicate;

public class TargetScanner {
    public static Entity findNearest(Animals self, List<Entity> entities, double radius, Predicate<Entity> filter) {
        Entity nearest = null;
        double minDistance = Double.MAX_VALUE;

        for(Entity entity : entities) {
            if(entity == self || !filter.test(entity)) {
                continue;
            }

            double distance = distanceBetween(self, entity);
            if(distance < radius && distance < minDistance) {
                nearest = entity;
                minDistance = distance;
            }
        }

        return nearest;
    }

    public static Entity findNearest(Animals self, List<Entity> entities, double radius) {
        return findNearest(self, entities, radius, entity -> true);
    }

    private static double distanceBetween(Animals self, Entity entity) {
        double dx = self.getX() - entity.getX();
        double dy = self.getY() - entity.getY();
        return (dx * dx + dy * dy);
    }
}