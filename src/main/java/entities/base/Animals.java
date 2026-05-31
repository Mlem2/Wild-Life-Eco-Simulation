package entities.base;

import allEnum.Size;
import allEnum.State;
import brain.strategy.MoveStrategy;

import java.util.List;
import java.util.Random;

public abstract class Animals extends Entity {
    protected double hunger = 100;
    protected double thirst = 100;
    protected Size size;
    protected State state; // dùng để quyết định moveStrategy
    protected MoveStrategy moveStrategy;
    protected int defaultMoveCooldown; // thời gian hồi method di chuyển (chỉ để lưu)
    protected int currentMoveCooldown;// thời gian hồi method di chuyển (chỉ để tính toán sau mỗi chu kì clock)
    protected static Random random = new Random();

    public Animals(int x, int y){
        super(x,y);
    }

    public void updateMoveCooldown(Entity[][] animalCoordinates, List<Entity> allEntities){
        currentMoveCooldown--;
        updateHungerThirst();
        if(age <= 0 || hunger <= 0 || thirst <= 0){
            this.isAlive = false;
        }
        else{
            updateHungerThirst();
            if(currentMoveCooldown == 0){
               
            }
            age--;
        }
    }

    public void updateHungerThirst(){ // cập nhật đói + khát
        hunger -= 1 * size.multiplier;
        thirst -= 1 * size.multiplier;
    }

    public abstract void makeSound();

    public double getHunger() {
        return hunger;
    }

    public double getThirst() {
        return thirst;
    }

    public Size getSize() {
        return size;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setMoveStrategy(MoveStrategy moveStrategy) {
        this.moveStrategy = moveStrategy;
    }
}
