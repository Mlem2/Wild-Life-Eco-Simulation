package entities;

import allEnum.Size;
import entities.attributes.Apex;
import entities.attributes.Carnivore;
import entities.base.Animals;

public class Tiger extends Animals implements Carnivore, Apex {
    public Tiger(int x, int y){
        super(x,y);
        this.size = Size.LARGE;
        this.defaultMoveCooldown = 5 * 25;
        this.currentMoveCooldown = 5 * 25;
        this.age = (random.nextInt(6) + 7) * 21600;
        this.foodEfficiency = 0.7; // Tigers are quite efficient at using food
        this.waterEfficiency = 0.8; // Tigers have moderate water needs
        this.hungerRecoveryAmount = 100; // Tigers are very big!
        this.thirstRecoveryAmount = 40; // Eat meat can also help with thirst, but not as much as water.
    }

    
@Override
    public void makeSound(){
        System.out.println("GRAWWWWW");
    }
}
