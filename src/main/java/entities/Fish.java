package entities;

import allEnum.Size;
import entities.attributes.Herbivore;
import entities.base.Animals;

public class Fish extends Animals implements Herbivore {

    public Fish(String name, int x, int y){
        super(name,x,y);
        this.size = Size.SMALL;
        this.defaultMoveCooldown = 12;
        this.currentMoveCooldown = 12;
        this.age = (random.nextInt(3) + 3) * 21600;
    }

    public void makeSound(){
        System.out.println("GoocGooc");
    }
}
