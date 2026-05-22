package brain.strategy;

import entities.base.Animals;
import entities.base.Entity;
import entities.base.Tree;
import allEnum.Direction;
import brain.scanner.TargetScanner;
import java.util.List;

/*
 * Priority Strategy
 *
 * Dành cho việc tìm kiếm thức ăn/nước khi đói/khát.
 *
 * Hành vi:
 * - Tìm Tree gần nhất
 * - Di chuyển tới Tree
 */
public class PriorityStrategy implements MoveStrategy {

    // Khoảng cách scan mục tiêu
    private static final double SCAN_RADIUS = 50;

    private Entity currentTarget;
    private Direction lastDirection = Direction.CENTER;
    private double lastSpeed = 0;

    @Override
    public Direction move(Animals animal, List<Entity> visibleEntities) {
        currentTarget = TargetScanner.findNearest(animal, visibleEntities, SCAN_RADIUS, entity -> entity instanceof Tree);

        if(currentTarget == null) {
            lastDirection = Direction.CENTER;
            lastSpeed = 0;
            return lastDirection;
        }

        double dx = currentTarget.getX() - animal.getX();
        double dy = currentTarget.getY() - animal.getY();
        double distance = (dx * dx + dy * dy);

        if(distance == 0) {
            lastDirection = Direction.CENTER;
            lastSpeed = 0;
            return lastDirection;
        }

        dx /= distance;
        dy /= distance;
        lastDirection = directionFromVector(dx, dy);
        lastSpeed = getSpeedFromCooldown(animal);
        return lastDirection;
    }

    public Entity getCurrentTarget() {
        return currentTarget;
    }

    public Direction getLastDirection() {
        return lastDirection;
    }

    public double getLastSpeed() {
        return lastSpeed;
    }

    private Direction directionFromVector(double dx, double dy) {
        if(dx == 0 && dy == 0) {
            return Direction.CENTER;
        }
        if(Math.abs(dx) > Math.abs(dy)) {
            return dx > 0 ? Direction.EAST : Direction.WEST;
        }
        if(Math.abs(dy) > Math.abs(dx)) {
            return dy > 0 ? Direction.NORTH : Direction.SOUTH;
        }
        if(dx > 0 && dy > 0) {
            return Direction.NORTHEAST;
        }
        if(dx > 0 && dy < 0) {
            return Direction.SOUTHEAST;
        }
        if(dx < 0 && dy > 0) {
            return Direction.NORTHWEST;
        }
        if(dx < 0 && dy < 0) {
            return Direction.SOUTHWEST;
        }
        return Direction.CENTER;
    }

    private double getSpeedFromCooldown(Animals animal) {
        int cooldown = getCooldownValue(animal);
        return Math.max(0.5, 10.0 / Math.max(1, cooldown));
    }

    private int getCooldownValue(Animals animal) {
        try {
            java.lang.reflect.Field field = animal.getClass().getDeclaredField("defaultMoveCooldown");
            field.setAccessible(true);
            return field.getInt(animal);
        } catch (Exception e) {
            return 1;
        }
    }
}