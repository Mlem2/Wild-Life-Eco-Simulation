package entities;

import allEnum.Size;
import entities.attributes.*;
import entities.base.Animals;

public class Tiger extends Animals implements Carnivore, Apex {
    public Tiger(String name, int x, int y){
        super(name,x,y);
        this.size = Size.LARGE;
        this.defaultMoveCooldown = 5;
        this.currentMoveCooldown = 5;
        this.age = (random.nextInt(6) + 7) * 21600;
    }
    public void makeSound(){
        System.out.println("GRAWWWWW");
    }
}
