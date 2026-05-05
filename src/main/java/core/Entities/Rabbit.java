package Entities;

import AllEnum.Size;
import Entities.Attributes.Herbivore;
import Entities.Base.Animals;

public class Rabbit extends Animals implements Herbivore {
    public Rabbit(String name, int x, int y, int mCD1, int age){
        super(name,x,y,mCD1,age);
        this.kichCo= Size.SMALL;

    }
}
