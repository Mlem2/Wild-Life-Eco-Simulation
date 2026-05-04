import Entities.Animals;
import Entities.Entity;
import Entities.Tree;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Test {
    static int interval = 50;
    static ArrayList<Entity> sv = new ArrayList<>(); // lưu danh sách các thực thể
    static Entity[][] toaDoSV = new Entity[500][500]; // lưu danh sách các thực thể dựa trên tọa độ của chúng
    public static void main(String[] args){
        addAnimal("Cao",0,0,10,1);
        addAnimal("Voi",0,0,20,3);
        Timer timer = new Timer();
        TimerTask task = new TimerTask(){
            public void run() {
                TimeSystem.updateMinute();
                if (TimeSystem.getMinute() > 59) {
                    TimeSystem.updateHours();

                    if (TimeSystem.getHours() > 23) {
                        TimeSystem.updateDays();

                        int limit = TimeSystem.getLimit();
                        if (TimeSystem.getDays() > limit) {
                            TimeSystem.updateMonths();

                            if (TimeSystem.getMonths() > 12) {
                                TimeSystem.updateYears();
                            }
                        }
                    }
                }
                int soLuong=sv.size();
                for(int i=0;i<soLuong;i++){
                    Entity tmp = sv.get(i);
                    if(tmp.checkAlive() && tmp instanceof Animals){
                        Animals tmp1 = (Animals)tmp;
                        tmp1.move(toaDoSV,sv);
                    }
                    else if(tmp.checkAlive() && tmp instanceof Tree){
                        Tree tmp1 = (Tree)tmp;
                        tmp1.checkCD(toaDoSV,sv);
                    }
                    else{
                        sv.remove(tmp);
                    }
                }

            }
        };
        timer.scheduleAtFixedRate(task,0,interval);

    }

    public static void addAnimal(String ten,int x,int y,int mCD1,int Sizes){
        Animals tmp = new Animals(ten,x,y,mCD1,Sizes);
        sv.add(tmp);
        toaDoSV[x][y]=tmp;
    }
}