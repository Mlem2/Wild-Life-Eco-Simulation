package entities;

import allEnum.Size;
import entities.attributes.*;
import entities.base.Animals;

public class Elephant extends Animals implements Apex, Herbivore {
    public Elephant( int x, int y){
        super(x,y);
        this.size = Size.LARGE;
        this.defaultMoveCooldown = 10;
        this.currentMoveCooldown = 10;
        this.age = (random.nextInt(15) + 10) * 21600;
    }

    public void makeSound(){
        System.out.println("HEE HEE");
    }
}
