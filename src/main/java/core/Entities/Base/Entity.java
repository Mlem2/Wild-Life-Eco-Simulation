package Entities.Base;

public abstract class Entity {
    protected String name;
    protected int x,y;
    public Entity(String name, int x, int y){
        this.name=name;
        this.x=x;
        this.y=y;
    }
    public Entity(){}
}
