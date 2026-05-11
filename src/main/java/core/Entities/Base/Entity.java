package Entities.Base;

public abstract class Entity {
    protected String name;
    protected int x,y;
    protected int age;
    protected Boolean isAlive = true;

    public Entity(String name, int x, int y){
        this.name=name;
        this.x=x;
        this.y=y;
    }

    public Entity(){}

    public Boolean checkAlive(){
        return isAlive;
    }

}
