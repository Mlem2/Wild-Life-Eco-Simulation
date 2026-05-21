package brain.strategy;

import Entities.Base.Animals;
import Entities.Base.Entity;import Entities.Base.Entity;import AllEnum.Direction;
import AllEnum.Size;
import java.util.List;
import java.util.Random;

/*
 * Passive Strategy
 * Dùng cho động vật ăn cỏ/ hiền lành như:
 * - Thỏ
 * - Hươu
 * Hành vi:
 * - Di chuyển ngẫu nhiên theo lưới
 * - Tốc độ dựa trên size của động vật
 */
public class PassiveStrategy implements MoveStrategy {

    // Random để tạo hướng đi ngẫu nhiên
    private Random random = new Random();

    @Override
    public Direction move(Animals animal, List<Entity> visibleEntities) {

        /*
         * Chọn hướng ngẫu nhiên (trừ CENTER)
         */
        Direction[] directions = {Direction.NORTH, Direction.NORTHEAST, Direction.EAST,
                                Direction.SOUTHEAST, Direction.SOUTH, Direction.SOUTHWEST,
                                Direction.WEST, Direction.NORTHWEST};

        Direction chosenDirection = directions[random.nextInt(directions.length)];

        return chosenDirection;
    }

    /**
     * Lấy chu kỳ cooldown di chuyển cho passive animal theo size.
     * Core có thể dùng giá trị này để đồng bộ tốc độ.
     */
    public int getMovementCooldown(Animals animal) {
        return getMovementCooldown(animal.getKichCo());
    }

    public int getMovementCooldown(Size size) {
        switch (size) {
            case SMALL:
                return 4;
            case MEDIUM:
                return 6;
            case LARGE:
                return 8;
            default:
                return 6;
        }
    }
}