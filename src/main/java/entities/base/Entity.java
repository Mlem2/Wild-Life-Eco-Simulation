package entities.base;

/*
 * Entity là lớp cha của mọi vật thể trong game.
 * Bao gồm:
 * - Animal
 * - Plant
 * - Object
 */
public abstract class Entity {

    // Vị trí trên map
    protected double x;
    protected double y;

    /*
     * Getter vị trí X
     */
    public double getX() {
        return x;
    }

    /*
     * Getter vị trí Y
     */
    public double getY() {
        return y;
    }

    /*
     * Setter X
     */
    public void setX(double x) {
        this.x = x;
    }

    /*
     * Setter Y
    */
    public void setY(double y) {
        this.y = y;
    }

    /*
     * Update logic mỗi frame
     * Mỗi entity sẽ tự định nghĩa cách update.
     */
    public abstract void update();
}