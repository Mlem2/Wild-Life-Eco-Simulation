package entities;

import allEnum.Size;
import entities.attributes.Apex;
import entities.attributes.Herbivore;
import entities.base.Animals;

public class Elephant extends Animals implements Apex, Herbivore {
    public Elephant( int x, int y){
        super(x,y);
        this.size = Size.LARGE;
        this.defaultMoveCooldown = 8;
        this.currentMoveCooldown = 8;
        this.age = (random.nextInt(15) + 10) * 21600;
        this.foodEfficiency = 0.5; // Elephants are very efficient at using food
        this.waterEfficiency = 0.5; // Elephants are also efficient at using water
        this.hungerRecoveryAmount = 200; // Elephants are huge!
        this.thirstRecoveryAmount = 100; // I wonder who can eat them...
    }

@Override
    public void makeSound(){
        System.out.println("HEE HEE");
    }
}
