package entities;

import allEnum.Size;
import entities.attributes.Herbivore;
import entities.base.Animals;

public class Rabbit extends Animals implements Herbivore {
    public Rabbit(int x, int y){
        super(x,y);
        this.size = Size.SMALL;
        this.defaultMoveCooldown = 6 * 25;
        this.currentMoveCooldown = 6 * 25;
        this.age = (random.nextInt(3) + 5) * 21600;
        this.foodEfficiency = 0.6; // Rabbits are moderately efficient at using food
        this.waterEfficiency = 0.9; // Rabbits have some water needs
        this.hungerRecoveryAmount = 50; // Rabbits are small, so they don't provide much nutrition
        this.thirstRecoveryAmount = 20; // Rabbits have low thirst recovery
    }

    @Override
    public void makeSound(){
        System.out.println("chit chit");
    }

    public int getNutrient() {
        return hungerRecoveryAmount;
    }
}
