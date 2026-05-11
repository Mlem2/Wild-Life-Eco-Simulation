package Entities;

import AllEnum.Size;
import Entities.Attributes.Carnivore;
import Entities.Base.Animals;

public class Wolf extends Animals implements Carnivore {
    public Wolf(String name, int x, int y, int mCD1, int age){
        super(name,x,y,mCD1,age);
        this.kichCo= Size.MEDIUM;
    }
}
