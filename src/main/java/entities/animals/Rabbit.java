package entities.animals;

import brain.strategy.PassiveStrategy;
import entities.base.Animal;

/*
 * Rabbit là động vật cụ thể.
 *
 * Rabbit mặc định dùng PassiveStrategy.
 */
public class Rabbit extends Animal {

    /*
     * Constructor
     */
    public Rabbit() {

        /*
         * super() gọi constructor của Animal
         */
        super(new PassiveStrategy());
    }
}