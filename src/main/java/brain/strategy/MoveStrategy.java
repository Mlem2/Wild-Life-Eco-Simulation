package brain.strategy;

import java.util.List;

import allEnum.Direction;
import brain.controller.MapSystem;
import entities.base.Animals;
import entities.base.Entity;
import entities.base.Position;

/**
 * Interface Strategy Pattern
 * Mỗi strategy trả về một {@link Position} mục tiêu (ô) để con vật di chuyển tới.
 */
public interface MoveStrategy {
    Position getTarget(Animals owner, MapSystem mapSystem);

    /**
     * Backwards-compatible move API used by older loops (returns CENTER by default).
     */
    default Direction move(Animals owner, List<Entity> allEntities) {
        return Direction.CENTER;
    }
}