package Entities;

import AllEnum.Size;

public abstract class Entity {
    protected String name;
    protected int x,y;
    protected Boolean isAlive = true;
    public Boolean checkAlive(){
        return isAlive;
    }
    public Entity(String name, int x, int y){
        this.name=name;
        this.x=x;
        this.y=y;
    }
    public Entity(){};
}
