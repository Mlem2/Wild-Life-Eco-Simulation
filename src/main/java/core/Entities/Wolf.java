package Entities;

import AllEnum.Size;
import Entities.Attributes.Carnivore;
import Entities.Base.Animals;

import java.util.Random;

public class Wolf extends Animals implements Carnivore {


    public Wolf(String name, int x, int y){
        super(name,x,y);
        this.size= Size.MEDIUM;
        this.spd1 = 6;
        this.spd2 = 6;
        this.age = (ran.nextInt(4)+6)*21600;
    }

    public void makeSound(){
        System.out.println("AOWUUUUUUUUU");
    }
}
