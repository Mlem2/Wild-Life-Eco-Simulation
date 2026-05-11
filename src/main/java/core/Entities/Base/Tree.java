package Entities.Base;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public abstract class Tree extends Entity {
    protected int age;
    protected double seedCD1;
    protected double growthTime;
    protected double seedCD2;
    protected static Random ran = new Random();

    public Tree(String name, int x, int y){
        super(name,x,y);
    }

    public Tree(){}

    public abstract void checkCD(Entity[][] toaDoSV, List<Entity> allEntities);

}