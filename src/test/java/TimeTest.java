import core.TimeSystem;
import entities.base.Animals;
import entities.base.Entity;
import entities.base.EntityFactory;
import entities.base.Tree;
import entities.*;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class TimeTest {
    static int interval;
    static ArrayList<Entity> sv = new ArrayList<>(); // lưu danh sách các thực thể
    static Entity[][] toaDoSV = new Entity[500][500]; // lưu danh sách các thực thể dựa trên tọa độ của chúng
    public static void main(String[] args){
        setTimeSpd(40);
        AddEntity(Wolf::new,0,0);
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
                int soLuong = sv.size();
                System.out.println(soLuong);
                for(int i = 0;i < soLuong;i++){
                    Entity tmp = sv.get(i);
                    if(tmp instanceof Animals){
                        Animals tmp1 = (Animals)tmp;
                        tmp1.updateMoveCooldown(toaDoSV,sv);
                    }
                    else if(tmp instanceof Tree){
                        Tree tmp1 = (Tree)tmp;
                        tmp1.checkCD(toaDoSV,sv);
                    }
                    if(!tmp.checkAlive()){

                    }
                }
                sv.removeIf(e -> !e.checkAlive());
            }
        };
        timer.scheduleAtFixedRate(task,0,interval);

    }

    public static <T extends Entity> void AddEntity(EntityFactory.FakeConstructor<T,Integer,Integer> recipe, int x, int y){
        if(toaDoSV[x][y] != null) {
            System.out.println("toa do da co thuc the");
            return;
        }
        Entity tmp = EntityFactory.<T>CreateEntity(recipe,x,y);
        toaDoSV[x][y] = tmp;
        sv.add(tmp);
    }
    public static void setTimeSpd(int interval1){ // thay đổi tốc độ thời gian (mặc định là 40ms = 1 phút )
        interval = interval1;
    }
}