package brain.strategy;

import entities.base.Animals;
import entities.base.Entity;
import entities.attributes.Carnivore;
import allEnum.Direction;
import brain.scanner.TargetScanner;
import java.lang.reflect.Field;
import java.util.List;

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

    private static final double SCAN_RADIUS = 30; // Bán kính quét để tìm predator

    private Entity currentThreat; // Lưu predator gần nhất
    private Direction lastDirection = Direction.CENTER; // Hướng di chuyển cuối cùng
    private double lastSpeed = 0; // Tốc độ di chuyển cuối cùng

    @Override
    public Direction move(Animals animal, List<Entity> visibleEntities) {
        currentThreat = TargetScanner.findNearest(animal, visibleEntities, SCAN_RADIUS, entity -> entity instanceof Carnivore);

        if(currentThreat == null) { // Không có predator nào trong tầm, di chuyển ngẫu nhiên
            lastDirection = Direction.CENTER;
            lastSpeed = 0;
            return lastDirection;
        }

        double dx = animal.getX() - currentThreat.getX();
        double dy = animal.getY() - currentThreat.getY();
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

    public Entity getCurrentThreat() {
        return currentThreat;
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
            Field field = animal.getClass().getDeclaredField("defaultMoveCooldown");
            field.setAccessible(true);
            return field.getInt(animal);
        } catch (Exception e) {
            return 1;
        }
    }
}