package entities.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import allEnum.Size;
import allEnum.State;
import brain.strategy.MoveStrategy;
import core.TimeSystem;

public abstract class Animals extends Entity {
    protected double hunger = 100;
    protected double thirst = 100;
    protected Size size;
    protected State state; // dùng để quyết định moveStrategy
    protected MoveStrategy moveStrategy;
    protected int defaultMoveCooldown; // thời gian hồi method di chuyển (chỉ để lưu)
    protected int currentMoveCooldown;// thời gian hồi method di chuyển (chỉ để tính toán sau mỗi chu kì clock)
    protected int defaultMatingCooldown; // default mating cooldown
    protected static Random random = new Random();
    protected double foodEfficiency; // hệ số hiệu quả tiêu thụ thức ăn
    protected double waterEfficiency; // hệ số hiệu quả tiêu thụ nước
    protected int hungerRecoveryAmount = 10;
    protected int thirstRecoveryAmount = 10;
    protected int matingCooldown;
    // Brain related helpers
    protected Object lockedTargetEntity = null;
    protected Position lastLockedTargetPos = null;
    protected boolean speedUp = false;
    protected ArrayList<String> breedingSeason = new ArrayList<>();

    // =========================================================================
    // 🌟 PHẦN BỔ SUNG: TOẠ ĐỘ MƯỢT PHỤC VỤ HIỂN THỊ ĐỒ HỌA CANVAS (60 FPS)
    // =========================================================================
    // Sử dụng số thực (double) để tịnh tiến pixel lướt mịn, không bị nhảy ô giật cục
    protected double renderX = -1;
    protected double renderY = -1;

    // Tốc độ lướt đuổi theo backend (0.1 là tỉ lệ vàng mượt mà nhất)
    protected double interpolationSpeed = 0.1;

    protected int animationTick = 0;         // Bộ đếm thời gian nhảy khung hình
    protected int currentAnimationFrame = 0;   // Chỉ số ô ảnh hiện tại (0, 1, 2)
    protected boolean isMoving = false;      // Trạng thái kiểm tra xem thú có đang đi không
    protected String currentDirection = "down"; // Hướng nhìn hiện tại ("down", "left", "right", "up")

    // Các hàm Getter để MapController có thể lấy dữ liệu ra vẽ
    public boolean isMoving() { return isMoving; }
    public int getCurrentAnimationFrame() { return currentAnimationFrame; }
    public String getCurrentDirection() { return currentDirection; }


    // Tìm hàm khởi tạo này trong Animals.java của cậu:
    public Animals(int x, int y){
        super(x,y); // Giữ nguyên dòng gọi lớp cha Entity
        this.renderX = x * 32.0;
        this.renderY = y * 32.0;
    }

    public Position getPosition() {
        return Position.of(x, y);
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
        if (this.getMoveStrategyName().equals("HunterStrategy")) {
            return Math.max(1, (int) Math.round(defaultMoveCooldown / 1.2));
        }
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
        age--;
        if (matingCooldown > 0) matingCooldown--;
        if(age <= 0 || hunger <= 0 || thirst <= 0){
            this.isAlive = false;
        }
        else{
            if(currentMoveCooldown == 0){

            }
        }
    }

    public void updateHungerThirst(){ // cập nhật đói + khát
        double tickFactor = 1.0 / 25.0;
        hunger -= (size.multiplier * 0.2 + 1 * (speedUp ? 0.6 : 0.5) * foodEfficiency) * tickFactor;
        thirst -= (size.multiplier * 0.1 + 1 * (speedUp ? 0.6 : 0.5) * waterEfficiency) * tickFactor;
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

    public State getState() {
        return state;
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

    public int getDefaultMoveCooldown() { return this.defaultMoveCooldown; }
    public int getDefaultMatingCooldown() {
        if(breedingSeason.contains(TimeSystem.season)){
            return this.defaultMatingCooldown;
        }
        return this.defaultMatingCooldown * 3;
    }

    public int getMatingCooldown() { return matingCooldown; }
    public void setMatingCooldown(int matingCooldown) { this.matingCooldown = matingCooldown; }
    public boolean isReadyToMate() { return matingCooldown <= 0 && age > 1000; }

    public double getRenderX() {
        // Nếu lúc đầu chưa khởi tạo, gán bằng vị trí pixel lưới gốc (X * 32)
        if (renderX == -1) renderX = this.getX() * 32.0;
        return renderX;
    }

    public double getRenderY() {
        if (renderY == -1) renderY = this.getY() * 32.0;
        return renderY;
    }

    /**
     * Hàm tính toán tịnh tiến pixel nhỏ: Sẽ được MapController gọi liên tục 60 lần/giây
     */
    public void updateAnimation() {
        double targetX = this.getX() * 32.0;
        double targetY = this.getY() * 32.0;

        double prevX = renderX;
        double prevY = renderY;

        renderX += (targetX - renderX) * interpolationSpeed;
        renderY += (targetY - renderY) * interpolationSpeed;

        double distance = Math.sqrt(Math.pow(renderX - prevX, 2) + Math.pow(renderY - prevY, 2));

        if (distance > 0.4) {
            isMoving = true;
            animationTick++;

            //  TỰ ĐỘNG XÁC ĐỊNH HƯỚNG DỰA VÀO VỊ TRÍ ĐÍCH
            if (Math.abs(targetX - prevX) > Math.abs(targetY - prevY)) {
                // Thiên về di chuyển ngang
                currentDirection = (targetX > prevX) ? "right" : "left";
            } else {
                // Thiên về di chuyển dọc
                currentDirection = (targetY > prevY) ? "down" : "up";
            }

            if (animationTick >= 6) {
                animationTick = 0;
                currentAnimationFrame = (currentAnimationFrame + 1) % 3; // Vòng lặp 3 khung hình nằm ngang
            }
        } else {
            isMoving = false;
            animationTick = 0;
            currentAnimationFrame = 0; // Đứng im thì dừng ở khung hình số 0
        }
    }
}