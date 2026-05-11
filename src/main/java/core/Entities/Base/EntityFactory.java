package Entities.Base;

import Entities.*;

import java.util.Random;

public class EntityFactory {
    public static Random ran = new Random();
    public static Entity createEntity(String type, String name, int x, int y){
        if(type.equalsIgnoreCase("TREE")){
            return new Tree(name, x, y , (ran.nextInt(31)+90)*24*60,(ran.nextInt(40)+20)*24*60,(ran.nextInt(30)+15)*24*60);
        }
        else if(type.equalsIgnoreCase("Wolf")){
            return new Wolf(name,x,y,5,(ran.nextInt(15)+15)*24*60);
        }
        else if(type.equalsIgnoreCase("Rabbit")){
            return new Rabbit(name,x,y,9,(ran.nextInt(10)+8)*24*60);
        }
        return null;
    }
}
