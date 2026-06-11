package entities;

import allEnum.Size;
import entities.attributes.Aquatic;
import entities.base.Animals;

public class Fish extends Animals implements Aquatic {

    public Fish(int x, int y){
        super(x,y);
        this.size = Size.SMALL;
        this.defaultMoveCooldown = 10;
        this.currentMoveCooldown = 10;
        this.age = (random.nextInt(3) + 3) * 21600;
        this.foodEfficiency = 0.5; // Fish are quite efficient at using food
        this.waterEfficiency = 3.0; // Fish can't live without water
        this.defaultMatingCooldown = 5400;
        this.hungerRecoveryAmount = 30;
        this.thirstRecoveryAmount = 30;
        this.breedingSeason.add("Autumn");
        this.matingCooldown = this.getDefaultMatingCooldown();
    }

    @Override
    public void makeSound(){
        System.out.println("GoocGooc");
    }
}
