package entities;

import allEnum.Size;
import entities.attributes.Carnivore;
import entities.base.Animals;

public class Wolf extends Animals implements Carnivore {


    public Wolf(int x, int y){
        super(x,y);
        this.size = Size.MEDIUM;
        this.defaultMoveCooldown = 5 * 25;
        this.currentMoveCooldown = 5 * 25;
        this.age = (random.nextInt(4) + 6) * 21600;
        this.foodEfficiency = 0.8; // Wolves are efficient at using food
        this.waterEfficiency = 0.9; // Wolves have slightly below average water needs
        this.hungerRecoveryAmount = 85; // Wolves can be nutrious!
        this.thirstRecoveryAmount = 40; // Eat meat can also help with thirst, but not as much as water.
    }

    @Override
    public void makeSound(){
        System.out.println("AOWUUUUUUUUU");
    }
}
