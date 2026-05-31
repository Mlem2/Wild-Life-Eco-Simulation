package entities;

import allEnum.Size;
import entities.attributes.Herbivore;
import entities.base.Animals;

public class Rabbit extends Animals implements Herbivore {
    public Rabbit(int x, int y){
        super(x,y);
        this.size = Size.SMALL;
        this.defaultMoveCooldown = 10;
        this.currentMoveCooldown = 10;
        this.age = (random.nextInt(3) + 5) * 21600;
    }

    public void makeSound(){
        System.out.println("chit chit");
    }
}
