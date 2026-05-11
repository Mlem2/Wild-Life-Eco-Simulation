package Entities;

import AllEnum.State;
import AllEnum.Size;
import AllEnum.Direction;
import brain.strategy.MoveStrategy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
public class Animals extends Entities.Entity {
    protected State trangThai = State.values()[0];
    private double hunger = 100;
    private double thirst = 100;
    private final Size kichCo;
    private int age; // độ tuổi
    private int mCD1; // thời gian hồi method di chuyển (chỉ để lưu)
    private int mCD2; // thời gian hồi method di chuyển (chỉ để tính toán sau mỗi chu kì clock)
    private MoveStrategy moveStrategy;
    public Animals(String name, int x, int y, int mCD1, int age){
        super(name,x,y);
        this.mCD1=mCD1;
        this.mCD2=mCD1;
        this.age = age;
        // Xác định kích cỡ dựa trên độ tuổi
        if(age <= 100) { // con non
            this.kichCo = Size.SMALL;
        } else if(age <= 500) {
            this.kichCo = Size.MEDIUM;
        } else {
            this.kichCo = Size.LARGE;
        }
    }
    public void setMoveStrategy(MoveStrategy strategy) {
        this.moveStrategy = strategy;
    }
    public void printLocation(){
        System.out.println(x + " " + y);
    }
    public Size getKichCo(){
        return kichCo;
    }
    public int getAge(){
        return age;
    }
    public void updatemCD(Entities.Entity[][] toaDoSV, List<Entities.Entity> allEntities){
        mCD2--;
        updateHT();
        if(mCD2==0){
            Random rand = new Random();
            mCD2=mCD1;
            move(toaDoSV,allEntities);
        }
    }
    public void updateHT(){ // cập nhật đói + khát
        hunger-=0.01 * kichCo.multiplier;
        thirst-=0.01 * kichCo.multiplier;
    }
    public void move(Entities.Entity[][] toaDoSV, List<Entities.Entity> allEntities){
        int xTmp=0;
        int yTmp=0;
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
        }

    }


}