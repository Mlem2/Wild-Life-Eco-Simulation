package Entities.Base;

import AllEnum.Size;
import AllEnum.State;
import AllEnum.Direction;
import Entities.Base.Entity;
import Entities.Base.Tree;
import Entities.Wolf;
import Entities.Rabbit;
import brain.scanner.TargetScanner;
import brain.strategy.MoveStrategy;
import core.TimeSystem;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public abstract class Animals extends Entity {
    protected int hp = 100;
    protected int age;
    protected double hunger = 100;
    protected double thirst = 100;
    protected Size kichCo;
    protected State trangThai = State.PASSIVE;
    private MoveStrategy moveStrategy;
    protected int spd1; // thời gian hồi method di chuyển (chỉ để lưu)
    protected int spd2; // thời gian hồi method di chuyển (chỉ để tính toán sau mỗi chu kì clock)

    protected int breedingCooldown = 0;
    protected int matingTimer = 0;
    protected int matingDuration = 10; // thời gian chờ để sinh sản khi 2 cá thể gặp nhau
    protected String breedingSeason = "Spring";

    public Animals(String name, int x, int y, int mCD1, int age){
        super(name,x,y);
        this.spd1 = mCD1;
        this.spd2 = mCD1;
        this.age = age;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getName() {
        return name;
    }

    public void setMoveStrategy(MoveStrategy strategy) {
        this.moveStrategy = strategy;
    }

    public MoveStrategy getMoveStrategy() {
        return moveStrategy;
    }

    public void setState(State state) {
        this.trangThai = state;
    }

    public State getState() {
        return trangThai;
    }

    public double getHunger() {
        return hunger;
    }

    public double getThirst() {
        return thirst;
    }

    public int getBreedingCooldown() {
        return breedingCooldown;
    }

    public int getMatingDuration() {
        return matingDuration;
    }

    public int adultAge() {
        return 100;
    }

    public boolean canMate() {
        return hunger > 60
                && thirst > 60
                && breedingCooldown <= 0
                && age > adultAge()
                && isInBreedingSeason();
    }

    public boolean isInBreedingSeason() {
        return TimeSystem.getSeason().equalsIgnoreCase(breedingSeason);
    }

    public void updatemCD(Entity[][] toaDoSV,List<Entity> allEntities){
        spd2--;
        updateHT();
        updateBreedingCooldown();
        if(age<=0 || hp<=0){
            toaDoSV[x][y]=null;
            allEntities.remove(this);
            return;
        }
        else{
            updateHT();
            if(spd2==0){
                spd2 = spd1;
                move(toaDoSV, allEntities);
                tryReproduce(toaDoSV, allEntities);
            }
            if(hunger<=0 && thirst<=0) hp-=4;
            else if(hunger<=0 || thirst<=0) hp-=2;
            age--;
        }
    }

    public void updateHT(){ // cập nhật đói + khát
        hunger-= 0.01 * kichCo.multiplier;
        thirst-= 0.01 * kichCo.multiplier;
    }

    public void move(Entity[][] toaDoSV, List<Entity> allEntities){
        int xTmp = x;
        int yTmp = y;
        Direction chosen = Direction.CENTER;

        if (moveStrategy != null) {
            chosen = moveStrategy.move(this, allEntities);
        }

        if (chosen != null && chosen != Direction.CENTER) {
            int targetX = x + chosen.x;
            int targetY = y + chosen.y;
            if (canMoveTo(targetX, targetY, toaDoSV)) {
                xTmp = targetX;
                yTmp = targetY;
            }
        }

        if (xTmp == x && yTmp == y && chosen != Direction.CENTER) {
            Integer[] huong = {0,1,2,3,4,5,6,7,8};
            Collections.shuffle(Arrays.asList(huong)); // lấy hướng ngẫu nhiên để di chuyển
            for(int i : huong){
                int ranX = Direction.values()[i].x;
                int ranY = Direction.values()[i].y;
                xTmp = x+ranX;
                yTmp = y+ranY;
                if(xTmp>=0 && xTmp<500 && yTmp>=0 && yTmp<500){  // kiểm tra xem có bị tràn map ko
                    if(toaDoSV[xTmp][yTmp]==null || toaDoSV[xTmp][yTmp]==this) break; // kiểm tra xem thực thể tại tọa độ địch là rỗng hay là chính mình ( đứng yên)
                    else if(toaDoSV[xTmp][yTmp] instanceof Animals){
                        Animals tmp1 = (Animals)toaDoSV[xTmp][yTmp];
                        if(this.kichCo.multiplier>tmp1.kichCo.multiplier) break;   // nếu thực thể tại tọa độ đích là động vật có size nhỏ hơn => di chuyển ( thực vật được tính là vật cản )
                    }
                }
            }
        }

        if(xTmp==x && yTmp==y){   // nếu đứng yên
            return;
        }
        Animals tmp = (Animals)toaDoSV[xTmp][yTmp];
        if(tmp==null){  // nếu tọa độ x không có thực thể
            toaDoSV[xTmp][yTmp]=this;
            toaDoSV[x][y]=null;
            x=xTmp;
            y=yTmp;
            updateHT();
            // Kiểm tra nếu có Tree và đang ưu tiên tìm thức ăn
            if (toaDoSV[x][y] instanceof Tree && trangThai == State.PRIORITIZE) {
                hunger = Math.min(100, hunger + 20);
                thirst = Math.min(100, thirst + 20);
            }
            return;
        }
        else if(this.kichCo.multiplier > tmp.kichCo.multiplier){ // nếu kích cỡ > thực thể tại tọa độ đích => bắt nhường đường
            tmp.move(toaDoSV,allEntities);
            if(toaDoSV[xTmp][yTmp]==tmp){ // nếu không nhường được thì thực thể tại tọa độ đích sẽ bị đè bẹp => chết
                tmp.isAlive=false;
                toaDoSV[xTmp][yTmp]=null;
                allEntities.remove(tmp);
            }
            toaDoSV[xTmp][yTmp]=this;
            toaDoSV[x][y]=null;
            x=xTmp;
            y=yTmp;
            updateHT();
            // Kiểm tra nếu có Tree và đang ưu tiên tìm thức ăn
            if (toaDoSV[x][y] instanceof Tree && trangThai == State.PRIORITIZE) {
                hunger = Math.min(100, hunger + 20);
                thirst = Math.min(100, thirst + 20);
            }
        }
    }

    private boolean canMoveTo(int xTmp, int yTmp, Entity[][] toaDoSV) {
        if(xTmp<0 || xTmp>=500 || yTmp<0 || yTmp>=500) {
            return false;
        }
        if(toaDoSV[xTmp][yTmp]==null || toaDoSV[xTmp][yTmp]==this) {
            return true;
        }
        if(toaDoSV[xTmp][yTmp] instanceof Animals){
            Animals tmp1 = (Animals)toaDoSV[xTmp][yTmp];
            return this.kichCo.multiplier > tmp1.kichCo.multiplier;
        }
        return false;
    }

    private void updateBreedingCooldown() {
        if (breedingCooldown > 0) {
            breedingCooldown--;
        }
    }

    private void tryReproduce(Entity[][] toaDoSV, List<Entity> allEntities) {
        if (trangThai != State.MATE || !canMate()) {
            matingTimer = 0;
            return;
        }

        Animals partner = findNearbyPartner(allEntities);
        if (partner == null || partner.getState() != State.MATE || !partner.canMate()) {
            matingTimer = 0;
            return;
        }

        matingTimer++;
        if (matingTimer >= matingDuration) {
            if (spawnOffspring(toaDoSV, allEntities, partner)) {
                breedingCooldown = 24 * 60 * 7; // 1 tuần cooldown sau khi sinh sản
                partner.breedingCooldown = 24 * 60 * 7;
            }
            matingTimer = 0;
            partner.matingTimer = 0;
        }
    }

    private Animals findNearbyPartner(List<Entity> allEntities) {
        for (Entity entity : allEntities) {
            if (entity == this || !(entity instanceof Animals)) {
                continue;
            }
            Animals other = (Animals) entity;
            if (other.getClass() != getClass()) {
                continue;
            }
            double dx = other.getX() - x;
            double dy = other.getY() - y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            if (distance <= Math.sqrt(2) + 0.001 && other.canMate()) {
                return other;
            }
        }
        return null;
    }

    private boolean spawnOffspring(Entity[][] toaDoSV, List<Entity> allEntities, Animals partner) {
        int[] dx = {-1, 0, 1};
        int[] dy = {-1, 0, 1};
        for (int ix : dx) {
            for (int iy : dy) {
                int sx = x + ix;
                int sy = y + iy;
                if (sx < 0 || sx >= 500 || sy < 0 || sy >= 500) {
                    continue;
                }
                if (toaDoSV[sx][sy] == null) {
                    Animals baby = createOffspring(sx, sy);
                    if (baby != null) {
                        toaDoSV[sx][sy] = baby;
                        allEntities.add(baby);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected Animals createOffspring(int x, int y) {
        String species = getClass().getSimpleName();
        if (species.equals("Wolf")) {
            return new Wolf(getName() + "_baby", x, y, spd1, 1);
        }
        if (species.equals("Rabbit")) {
            return new Rabbit(getName() + "_baby", x, y, spd1, 1);
        }
        return null;
    }
}
