package entities;

import AllEnum.Size;
import entities.Attributes.Carnivore;
import entities.Base.Animals;

public class Wolf extends Animals implements Carnivore {


    public Wolf(String name, int x, int y){
        super(name,x,y);
        this.size = Size.MEDIUM;
        this.defaultMoveCD = 6;
        this.currentMoveCD = 6;
        this.age = (ran.nextInt(4)+6)*21600;
    }

    public void makeSound(){
        System.out.println("AOWUUUUUUUUU");
    }
}
