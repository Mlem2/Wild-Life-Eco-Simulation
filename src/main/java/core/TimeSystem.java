import AllEnum.Month;

public class TimeSystem {
    private static int year=2000;
    private static int month=1;
    private static int day=1;
    private static int hour=0;
    private static int minute=0;
    private static String partOfDay="Night";
    private static String season = "Spring";

    public static int getLimit(){
        Month m = Month.values()[month-1];
        int days=m.numberOfDays;
        if(m.equals(Month.FEB)){
            if(year%400==0 || (year%4==0 && year%100!=0)) return days+1;
        }
        return days;
    }

    public static void updateDays(){
        day++;
        hour=0;
    }
    public static void updateMonths(){
        month++;
        day=1;
        if(month>=1 && month<4) season="Spring";
        else if(month>=4 && month<7) season="Summer";
        else if(month >=7 && month<10) season="Autumn";
        else season="Winter";
    }
    public static void updateYears(){
        year++;
        month=1;
    }
    public static void updateHours(){
        hour++;
        minute=0;
        if(hour>4 && hour <18){
            partOfDay="Day";
        }
        else{
            partOfDay="Night";
        }
    }
    public static void updateMinute(){
        minute++;
    }
    public static int getHours(){
        return hour;
    }
    public static int getDays(){
        return day;
    }
    public static int getMonths(){
        return month;
    }
    public static int getMinute(){
        return minute;
    }
    public static void showTime(){
        System.out.println(season + " " + day + "/" + month + "/" + year + " " + partOfDay + " Hour:" + hour + " Minute:" + minute);
    }
}
