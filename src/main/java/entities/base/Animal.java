package entities.base;

import brain.strategy.MoveStrategy;
import org.jetbrains.annotations.NotNull;

/*
 * Animal là lớp cha của mọi động vật.
 *
 * Animal kế thừa Entity.
 *
 * Animal chứa:
 * - MoveStrategy
 * - Logic update
 */
public abstract class Animal extends Entity {

    /*
     * Strategy Pattern
     *
     * Animal không tự biết cách di chuyển.
     * Nó giao việc này cho strategy.
     */
    protected MoveStrategy moveStrategy;

    /*
     * Constructor
     *
     * Khi tạo animal phải truyền strategy vào.
     */
    public Animal(MoveStrategy strategy) {
        this.moveStrategy = strategy;
    }

    /*
     * Update mỗi frame
     */
    @Override
    public void update() {

        /*
         * Nếu có strategy thì thực hiện move
         */
        if(moveStrategy != null) {
            moveStrategy.move(this);
        }
    }

    /*
     * Cho phép đổi chiến lược khi runtime
     *
     * Ví dụ:
     * rabbit.setMoveStrategy(new ScaredStrategy());
     */
    public void setMoveStrategy(MoveStrategy strategy) {
        this.moveStrategy = strategy;
    }

    /*
     * Tính khoảng cách giữa 2 entity
     */
    public double distanceTo(@NotNull Entity other) {

        double dx = other.getX() - this.getX();
        double dy = other.getY() - this.getY();

        return Math.sqrt(dx * dx + dy * dy);
    }
}