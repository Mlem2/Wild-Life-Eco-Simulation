package entities;

import allEnum.Size;
import entities.attributes.Herbivore;
import entities.base.Animals;

public class Fish extends Animals implements Herbivore {

    public Fish(int x, int y){
        super(x,y);
        this.size = Size.SMALL;
        this.defaultMoveCooldown = 5 * 25;
        this.currentMoveCooldown = 5 * 25;
        this.age = (random.nextInt(3) + 3) * 21600;
        this.foodEfficiency = 0.5; // Fish are quite efficient at using food
        this.waterEfficiency = 3.0; // Fish can't live without water
        this.hungerRecoveryAmount = 30;
        this.thirstRecoveryAmount = 30;
    }

    @Override
    public void makeSound(){
        System.out.println("GoocGooc");
    }
}
