import Entities.Base.Animals;
import Entities.Base.Entity;
import Entities.Base.EntityFactory;
import Entities.Base.Tree;
import Entities.*;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class TimeTest {
    static int interval;
    static ArrayList<Entity> sv = new ArrayList<>(); // lưu danh sách các thực thể
    static Entity[][] toaDoSV = new Entity[500][500]; // lưu danh sách các thực thể dựa trên tọa độ của chúng
    public static void main(String[] args){
        setTimeSpd(40);
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
                    if(tmp instanceof Animals){
                        Animals tmp1 = (Animals)tmp;
                        tmp1.updatemCD(toaDoSV,sv);
                    }
                    else{
                        Tree tmp1 = (Tree)tmp;
                        tmp1.checkCD(toaDoSV,sv);
                    }
                }
                sv.removeIf(e -> !e.checkAlive());

            }
        };
        timer.scheduleAtFixedRate(task,0,interval);
        AddEntity(Wolf::new,"Sói Trắng",0,0);
        Wolf soi1 = (Wolf)sv.get(0);

    }

    public static <T extends Entity> void AddEntity(EntityFactory.FakeConstructor<T,String,Integer,Integer> recipe,String ten, int x, int y){
        if(toaDoSV[x][y]!=null){
            System.out.println("toa do da co thuc the");
            return;
        }
        Entity tmp = EntityFactory.<T>CreateEntity(recipe,ten,x,y);
        toaDoSV[x][y] = tmp;
        sv.add(tmp);
    }
    public static void setTimeSpd(int interval1){ // thay đổi tốc độ thời gian (mặc định là 40ms = 1 phút )
        interval=interval1;
    }
}