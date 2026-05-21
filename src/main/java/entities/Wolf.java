package entities;

import allEnum.Size;
import entities.attributes.Carnivore;
import entities.base.Animals;

public class Wolf extends Animals implements Carnivore {


    public Wolf(String name, int x, int y){
        super(name,x,y);
        this.size = Size.MEDIUM;
        this.defaultMoveCooldown = 6;
        this.currentMoveCooldown = 6;
        this.age = (random.nextInt(4) + 6) * 21600;
    }

    public void makeSound(){
        System.out.println("AOWUUUUUUUUU");
    }
}
