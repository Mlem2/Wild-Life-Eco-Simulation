package entities;

import AllEnum.Size;
import entities.Attributes.*;
import entities.Base.Animals;

public class Tiger extends Animals implements Carnivore, Apex {
    public Tiger(String name, int x, int y){
        super(name,x,y);
        this.size = Size.LARGE;
        this.defaultMoveCD = 5;
        this.currentMoveCD = 5;
        this.age = (ran.nextInt(6)+7)*21600;
    }
    public void makeSound(){
        System.out.println("GRAWWWWW");
    }
}
