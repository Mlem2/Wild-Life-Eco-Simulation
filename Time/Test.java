import java.util.Timer;
import java.util.TimerTask;

public class Test {
    public static void main(String[] args){
        Timer timer = new Timer();
        TimerTask task = new TimerTask(){
            public void run(){
                TimeSystem.updateHours();
                int limit=TimeSystem.getLimit();
                if(TimeSystem.getHours()>23){
                    TimeSystem.updateDays();
                }
                if(TimeSystem.getDays()>limit){
                    TimeSystem.updateMonths();
                }
                if(TimeSystem.getMonths()>12){
                    TimeSystem.updateYears();
                }
                TimeSystem.showTime();
            }
        };
        timer.scheduleAtFixedRate(task,0,40);
    }
}
