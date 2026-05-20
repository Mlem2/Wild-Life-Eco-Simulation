package entities.Base;

import AllEnum.Size;

import java.util.List;
import java.util.Random;
public abstract class Animals extends Entity {
    protected double hunger = 100;
    protected double thirst = 100;
    protected Size size;
    /*protected Strategy Strat;*/
    protected int spd1; // thời gian hồi method di chuyển (chỉ để lưu)
    protected int spd2;// thời gian hồi method di chuyển (chỉ để tính toán sau mỗi chu kì clock)
    protected static Random ran = new Random();

    public Animals(String name, int x, int y){
        super(name,x,y);
    }

    public void updatemCD(Entity[][] toaDoSV,List<Entity> allEntities){
        spd2--;
        updateHT();
        if(age<=0 || hunger <=0 || thirst <=0){
            this.isAlive = false;
        }
        else{
            updateHT();
            if(spd2==0){
                Random rand = new Random();
                spd2 = spd1;
                /*move(toaDoSV,allEntities);*/
            }
            age--;
        }
    }
    public void updateHT(){ // cập nhật đói + khát
        hunger-= 1 * size.multiplier;
        thirst-= 1 * size.multiplier;
    }
    public abstract void makeSound();
}