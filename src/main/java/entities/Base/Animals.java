package entities.Base;

import AllEnum.Size;

import java.util.List;
import java.util.Random;
public abstract class Animals extends Entity {
    protected double hunger = 100;
    protected double thirst = 100;
    protected Size size;
    /*protected Strategy Strat;*/
    protected int defaultMoveCD; // thời gian hồi method di chuyển (chỉ để lưu)
    protected int currentMoveCD;// thời gian hồi method di chuyển (chỉ để tính toán sau mỗi chu kì clock)
    protected static Random ran = new Random();

    public Animals(String name, int x, int y){
        super(name,x,y);
    }

    public void updateCD(Entity[][] toaDoSV,List<Entity> allEntities){
        currentMoveCD--;
        updateHungerThirst();
        if(age <= 0 || hunger <= 0 || thirst <= 0){
            this.isAlive = false;
        }
        else{
            updateHungerThirst();
            if(currentMoveCD == 0){
               
            }
            age--;
        }
    }
    public void updateHungerThirst(){ // cập nhật đói + khát
        hunger -= 1 * size.multiplier;
        thirst -= 1 * size.multiplier;
    }
    public abstract void makeSound();
}
