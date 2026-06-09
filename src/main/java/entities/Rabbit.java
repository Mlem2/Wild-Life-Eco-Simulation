package entities;

import allEnum.Size;
import entities.attributes.Herbivore;
import entities.base.Animals;

public class Rabbit extends Animals implements Herbivore {
    public Rabbit(int x, int y){
        super(x,y);
        this.size = Size.SMALL;
        this.defaultMoveCooldown = 8;
        this.currentMoveCooldown = 8;
        this.age = (random.nextInt(3) + 2) * 21600;
        this.foodEfficiency = 0.6; // Rabbits are moderately efficient at using food
        this.waterEfficiency = 0.9; // Rabbits have some water needs
        this.defaultMatingCooldown = 3000;
        this.hungerRecoveryAmount = 50; // Rabbits are small, so they don't provide much nutrition
        this.thirstRecoveryAmount = 0; // Rabbits have low thirst recovery
        this.breedingSeason.add("Spring"); // mùa sinh sản
        this.breedingSeason.add("Summer");
        this.breedingSeason.add("Autumn");
        this.breedingSeason.add("Winter");
    }

    @Override
    public void makeSound(){
        System.out.println("chit chit");
    }

    public int getNutrient() {
        return hungerRecoveryAmount;
    }
}
