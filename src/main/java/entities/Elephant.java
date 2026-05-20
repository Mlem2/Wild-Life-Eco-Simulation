package entities;

import AllEnum.Size;
import entities.Attributes.*;
import entities.Base.Animals;

public class Elephant extends Animals implements Apex, Herbivore {
    public Elephant(String name, int x, int y){
        super(name,x,y);
        this.size = Size.LARGE;
        this.defaultMoveCD = 10;
        this.currentMoveCD = 10;
        this.age = (ran.nextInt(15)+10)*21600;
    }

    public void makeSound(){
        System.out.println("HEHEHEHE");
    }
}
