package Entities;

import AllEnum.Size;

public abstract class Entity {
    protected String name;
    protected int x,y;
    protected Boolean isAlive = true;
    public Boolean checkAlive(){
        return isAlive;
    }
    public int getX(){
        return x;
    }
    public int getY(){
        return y;
    }
    public void setX(int x){
        this.x = x;
    }
    public void setY(int y){
        this.y = y;
    }
    public Entity(String name, int x, int y){
        this.name=name;
        this.x=x;
        this.y=y;
    }
    public Entity(){};
}
