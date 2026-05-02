import AllEnum.State;
import AllEnum.Size;
import AllEnum.Direction;

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
    public void updatemCD(){
        mCD2--;
        updateHT();
        if(mCD2==0){
            mCD2=mCD1;
            move();
        }
    }
    public void updateHT(){
        hunger-=0.01 * kichCo.getM();
        thirst-=0.01 * kichCo.getM();
    }
    public void move(){
        Random rand = new Random();
        int huong = rand.nextInt(8);
        int ranX = Direction.values()[huong].x;
        int ranY = Direction.values()[huong].y;
        x+=ranX;
        y+=ranY;
        updateHT();
        printLocation();
    }

}
