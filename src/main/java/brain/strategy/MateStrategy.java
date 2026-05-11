package brain.strategy;

import Entities.Base.Animals;
import Entities.Base.Entity;
import AllEnum.Direction;
import brain.scanner.TargetScanner;
import java.util.List;

/*
 * Mate Strategy
 *
 * Dành cho con trưởng thành trong mùa sinh sản.
 * Hành vi:
 * - Tìm con cùng loài và cùng điều kiện sinh sản gần nhất
 * - Đi về phía đó
 * - Giữ vị trí khi đã ở gần nhau để hoàn tất quá trình sinh sản
 */
public class MateStrategy implements MoveStrategy {

    private static final double SCAN_RADIUS = 40;

    private Entity currentTarget;
    private Direction lastDirection = Direction.CENTER;
    private double lastSpeed = 0;

    @Override
    public Direction move(Animals animal, List<Entity> visibleEntities) {
        currentTarget = TargetScanner.findNearest(animal, visibleEntities, SCAN_RADIUS, entity -> {
            if (!(entity instanceof Animals)) {
                return false;
            }
            Animals other = (Animals) entity;
            return other.getClass() == animal.getClass() && other != animal && other.canMate();
        });

        if (currentTarget == null) {
            lastDirection = Direction.CENTER;
            lastSpeed = 0;
            return lastDirection;
        }

        double dx = currentTarget.getX() - animal.getX();
        double dy = currentTarget.getY() - animal.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);

        // Nếu đã ở ngay cạnh nhau, giữ vị trí để sinh sản
        if (distance <= Math.sqrt(2)) {
            lastDirection = Direction.CENTER;
            lastSpeed = 0;
            return lastDirection;
        }

        if (distance == 0) {
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