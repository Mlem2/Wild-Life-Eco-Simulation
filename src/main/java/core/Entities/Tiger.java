package Entities;

import AllEnum.Size;
import Entities.Attributes.*;
import Entities.Base.Animals;

public class Tiger extends Animals implements Carnivore, Apex {
    public Tiger(String name, int x, int y){
        super(name,x,y);
        this.size= Size.LARGE;
        this.spd1 = 5;
        this.spd2 = 5;
        this.age = (ran.nextInt(6)+7)*21600;
    }
    public void makeSound(){
        System.out.println("GRAWWWWW");
    }
}
