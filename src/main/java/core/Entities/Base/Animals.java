package Entities.Base;

import AllEnum.Size;
import Interfaces.MoveStrategy;

import java.util.List;
import java.util.Random;
public abstract class Animals extends Entity {
    protected MoveStrategy Strategy;
    protected int hp = 100;
    protected int age;
    protected double hunger = 100;
    protected double thirst = 100;
    protected Size kichCo;
    protected int mCD1; // thời gian hồi method di chuyển (chỉ để lưu)
    protected int mCD2; // thời gian hồi method di chuyển (chỉ để tính toán sau mỗi chu kì clock)
    public Animals(String name, int x, int y, int mCD1, int age){
        super(name,x,y);
        this.mCD1 = mCD1;
        this.mCD2 = mCD1;
        this.age=age;
    }
    public void changeStrategy(MoveStrategy Strategy){
        this.Strategy = Strategy;
    }
    public void printLocation(){
        System.out.println(x + " " + y);
    }
    public void updatemCD(Entity[][] toaDoSV,List<Entity> allEntities){
        mCD2--;
        updateHT();
        if(age<=0 || hp<=0){
            toaDoSV[x][y]=null;
            allEntities.remove(this);
            return;
        }
        else{
            updateHT();
            if(mCD2==0){
                Random rand = new Random();
                mCD2 = mCD1;
                Strategy.move(toaDoSV,allEntities);
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



}