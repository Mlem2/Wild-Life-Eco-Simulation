import AllEnum.State;
import AllEnum.Size;
import AllEnum.Direction;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
public class Animals {
    private final String name;
    private int x,y;
    private double hunger = 100;
    private double thirst = 100;
    private Boolean isAlive = true;
    private State trangThai = State.values()[0];
    private final Size kichCo;
    private int mCD1;
    private int mCD2;
    Animals(String name, int x, int y, int mCD1, int Sizes){
        this.name=name;
        this.x=x;
        this.y=y;
        this.mCD1=mCD1;
        this.mCD2=mCD1;
        this.kichCo = Size.values()[Sizes-1];
    }
    public void printLocation(){
        System.out.println(x + " " + y);
    }
    public Boolean checkAlive(){
        return isAlive;
    }
    public void updatemCD(Animals[][] toaDoSV){
        mCD2--;
        updateHT();
        if(mCD2==0){
            Random rand = new Random();
            mCD2=mCD1;
            move(toaDoSV);
        }
    }
    public void updateHT(){
        hunger-=0.01 * kichCo.multiplier;
        thirst-=0.01 * kichCo.multiplier;
    }
    public void move(Animals[][] toaDoSV){
        int xTmp=0;
        int yTmp=0;
        Integer[] huong = {0,1,2,3,4,5,6,7,8};
        Collections.shuffle(Arrays.asList(huong));
        for(int i : huong){
            int ranX = Direction.values()[i].x;
            int ranY = Direction.values()[i].y;
            xTmp = x+ranX;
            yTmp = y+ranY;
            if(xTmp>=0 && xTmp<500 && yTmp>=0 && yTmp<500){
                if(!(toaDoSV[xTmp][yTmp]!=null && toaDoSV[xTmp][yTmp]!=this && toaDoSV[xTmp][yTmp].kichCo.multiplier>=this.kichCo.multiplier)){
                    break;
                }
            }
        }
        if(xTmp==x && yTmp==y){
            return;
        }
        if(toaDoSV[xTmp][yTmp]==null){
                toaDoSV[xTmp][yTmp]=this;
                toaDoSV[x][y]=null;
                x=xTmp;
                y=yTmp;
                updateHT();
        }
        else if(this.kichCo.multiplier>toaDoSV[xTmp][yTmp].kichCo.multiplier){
            Animals tmp = toaDoSV[xTmp][yTmp];
            tmp.move(toaDoSV);
            if(toaDoSV[xTmp][yTmp]==tmp){
                tmp.isAlive=false;
                toaDoSV[xTmp][yTmp]=null;
            }
            toaDoSV[xTmp][yTmp]=this;
            toaDoSV[x][y]=null;
            x=xTmp;
            y=yTmp;
            updateHT();
        }

    }


}
