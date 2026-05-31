package entities.base;

import java.util.List;
import java.util.Random;

import allEnum.Size;
import allEnum.State;
import brain.strategy.MoveStrategy;

public abstract class Animals extends Entity {
    protected double hunger = 100;
    protected double thirst = 100;
    protected Size size;
    protected State state; // dùng để quyết định moveStrategy
    protected MoveStrategy moveStrategy;
    protected int defaultMoveCooldown; // thời gian hồi method di chuyển (chỉ để lưu)
    protected int currentMoveCooldown;// thời gian hồi method di chuyển (chỉ để tính toán sau mỗi chu kì clock)
    protected static Random random = new Random();
    protected double foodEfficiency; // hệ số hiệu quả tiêu thụ thức ăn
    protected double waterEfficiency; // hệ số hiệu quả tiêu thụ nước
    protected int hungerRecoveryAmount = 10;
    protected int thirstRecoveryAmount = 10;
    // Brain related helpers
    protected Object lockedTargetEntity = null;
    protected Position lastLockedTargetPos = null;
    protected boolean speedUp = false;

    public Animals(String name, int x, int y){
        super(name,x,y);
    }

    public Position getPosition() {
        return new Position(x, y);
    }

    public void setPosition(Position p) {
        this.x = p.getX();
        this.y = p.getY();
    }

    public void lockTargetEntity(Object target) {
        this.lockedTargetEntity = target;
        if (target instanceof Animals) {
            Animals a = (Animals) target;
            this.lastLockedTargetPos = a.getPosition();
        } else if (target instanceof Position) {
            this.lastLockedTargetPos = (Position) target;
        } else {
            this.lastLockedTargetPos = null;
        }
    }


    public boolean hasLockedTargetMoved() {
        if (lockedTargetEntity instanceof Animals) {
            Animals a = (Animals) lockedTargetEntity;
            Position p = a.getPosition();
            if (lastLockedTargetPos == null) return true;
            boolean moved = !lastLockedTargetPos.equals(p);
            lastLockedTargetPos = p;
            return moved;
        }
        return false;
    }

    public void setSpeedUp(boolean v) { this.speedUp = v; }
    public boolean isSpeedUp() { return this.speedUp; }

    public int getOwnMaxSpeedCooldown() {
        // Return a personal cooldown for speed-up actions; fallback to 1 tick
        return Math.max(1, defaultMoveCooldown / 2);
    }

    public void setCurrentMoveCooldown(int v) { this.currentMoveCooldown = v; }
    public int getCurrentMoveCooldown() { return this.currentMoveCooldown; }

    public double getHungerPercentage() { return Math.max(0, Math.min(100, hunger)); }
    public double getThirstPercentage() { return Math.max(0, Math.min(100, thirst)); }

    public int getHungerRecoveryAmount() {
        return hungerRecoveryAmount;
    }

    public int getThirstRecoveryAmount() {
        return thirstRecoveryAmount;
    }

    public void increaseHunger(double amount) { hunger = Math.min(100, hunger + amount); }
    public void increaseHydration(double amount) { thirst = Math.min(100, thirst + amount); }

    // Basic combat/health helpers
    protected int health = 100;

    public void takeDamage(int d) {
        health -= d;
        if (health <= 0) isAlive = false;
    }

    public int getAttackDamage() { return Math.max(1, size.ordinal() + 1); }

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
        hunger -= size.multiplier * 0.2 + 1 * (speedUp ? 0.6 : 0.5) * foodEfficiency;
        thirst -= size.multiplier * 0.1 + 1 * (speedUp ? 0.6 : 0.5) * waterEfficiency;
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

    public MoveStrategy getMoveStrategy() {
        return this.moveStrategy;
    }

    public String getMoveStrategyName() {
        if (this.moveStrategy == null) return "None";
        return this.moveStrategy.getClass().getSimpleName();
    }

    public Position getLastLockedTargetPosition() {
        return this.lastLockedTargetPos;
    }

    public Object getLockedTargetEntity() {
        return this.lockedTargetEntity;
    }

    // Expose default cooldown so external controllers (ActionManager) can use it
    public int getDefaultMoveCooldown() { return this.defaultMoveCooldown; }
}
