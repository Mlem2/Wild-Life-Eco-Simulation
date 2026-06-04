package entities;

import allEnum.Size;
import entities.attributes.AquaticHerbivore;
import entities.base.Animals;

public class Fish extends Animals implements AquaticHerbivore {

    public Fish(int x, int y){
        super(x,y);
        this.size = Size.SMALL;
        this.defaultMoveCooldown = 100;
        this.currentMoveCooldown = 100;
        this.matingTimeCost = 50;
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
