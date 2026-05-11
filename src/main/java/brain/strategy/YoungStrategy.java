package brain.strategy;

import Entities.Base.Animals;
import Entities.Base.Entity;
import AllEnum.Direction;
import brain.scanner.TargetScanner;
import java.util.List;

/*
 * Young Strategy
 *
 * Dành cho con non, sẽ cố gắng đi theo con cùng loài gần nhất.
 *
 * Hành vi:
 * - Tìm con cùng loài gần nhất
 * - Di chuyển tới đó
 */
public class YoungStrategy implements MoveStrategy {

    // Khoảng cách scan mục tiêu
    private static final double SCAN_RADIUS = 50;

    private Entity currentTarget;
    private Direction lastDirection = Direction.CENTER;
    private double lastSpeed = 0;

    @Override
    public Direction move(Animals animal, List<Entity> visibleEntities) {
        // Nếu con non đã già, không di chuyển nữa
        if (animal.getAge() > animal.adultAge()) { // Giả sử adultAge() trả về độ tuổi trưởng thành
            lastDirection = Direction.CENTER;
            lastSpeed = 0;
            return lastDirection;
        }

        currentTarget = TargetScanner.findNearest(animal, visibleEntities, SCAN_RADIUS, entity -> entity.getClass() == animal.getClass() && entity != animal);

        if(currentTarget == null) {
            lastDirection = Direction.CENTER;
            lastSpeed = 0;
            return lastDirection;
        }

        double dx = currentTarget.getX() - animal.getX();
        double dy = currentTarget.getY() - animal.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);

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
        return Direction.SOUTHWEST;
    }

    private double getSpeedFromCooldown(Animals animal) {
        int cooldown = getCooldownValue(animal);
        return Math.max(0.5, 10.0 / Math.max(1, cooldown));
    }

    private int getCooldownValue(Animals animal) {
        try {
            java.lang.reflect.Field field = animal.getClass().getDeclaredField("mCD1");
            field.setAccessible(true);
            return field.getInt(animal);
        } catch (Exception e) {
            return 1;
        }
    }
}