package Entities;

import AllEnum.Size;
import Entities.Attributes.*;
import Entities.Base.Animals;

public class Elephant extends Animals implements Apex, Herbivore {
    public Elephant(String name, int x, int y){
        super(name,x,y);
        this.size= Size.LARGE;
        this.spd1 = 10;
        this.spd2 = 10;
        this.age = (ran.nextInt(15)+10)*21600;
    }

    public void makeSound(){
        System.out.println("HEHEHEHE");
    }
}
