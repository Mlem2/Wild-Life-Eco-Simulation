package entities.base;

public abstract class Entity {
    protected int x,y;
    protected int age;
    protected Boolean isAlive = true;

    public Entity(int x, int y){
        this.x = x;
        this.y = y;
    }

    public Entity(){}

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Boolean checkAlive(){
        return isAlive;
    }

}
