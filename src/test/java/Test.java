import java.util.Timer;
import java.util.TimerTask;

public class Test {
    public static void main(String[] args){
        Animals a = new Animals("Cao",0,0,5,2);
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
                a.updatemCD();

            }
        };
        timer.scheduleAtFixedRate(task,0,4);

    }

}
