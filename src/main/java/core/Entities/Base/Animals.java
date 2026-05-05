package Entities.Base;

import AllEnum.Size;

import java.util.List;
import java.util.Random;
public abstract class Animals extends Entity {
    protected int hp = 100;
    protected int age;
    protected double hunger = 100;
    protected double thirst = 100;
    protected Size kichCo;
    protected int spd1; // thời gian hồi method di chuyển (chỉ để lưu)
    protected int spd2; // thời gian hồi method di chuyển (chỉ để tính toán sau mỗi chu kì clock)
    public Animals(String name, int x, int y, int mCD1, int age){
        super(name,x,y);
        this.spd1 = mCD1;
        this.spd2 = mCD1;
        this.age=age;
    }
    public void printLocation(){
        System.out.println(x + " " + y);
    }
    public void updatemCD(Entity[][] toaDoSV,List<Entity> allEntities){
        spd2--;
        updateHT();
        if(age<=0 || hp<=0){
            toaDoSV[x][y]=null;
            allEntities.remove(this);
            return;
        }
        else{
            updateHT();
            if(spd2==0){
                Random rand = new Random();
                spd2 = spd1;
                /*move(toaDoSV,allEntities);*/
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