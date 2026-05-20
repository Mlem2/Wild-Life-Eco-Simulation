package entities;

import AllEnum.Size;
import entities.Attributes.Herbivore;
import entities.Base.Animals;

public class Fish extends Animals implements Herbivore {

    public Fish(String name, int x, int y){
        super(name,x,y);
        this.size = Size.SMALL;
        this.defaultMoveCD = 12;
        this.currentMoveCD = 12;
        this.age = (ran.nextInt(3)+3)*21600;
    }

    public void makeSound(){
        System.out.println("GoocGooc");
    }
}
