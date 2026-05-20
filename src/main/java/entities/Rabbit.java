package entities;

import AllEnum.Size;
import entities.Attributes.Herbivore;
import entities.Base.Animals;

public class Rabbit extends Animals implements Herbivore {
    public Rabbit(String name, int x, int y){
        super(name,x,y);
        this.size = Size.SMALL;
        this.defaultMoveCD = 10;
        this.currentMoveCD = 10;
        this.age = (ran.nextInt(3)+5)*21600;
    }

    public void makeSound(){
        System.out.println("chit chit");
    }
}
