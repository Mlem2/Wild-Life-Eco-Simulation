package core;

public class TimeSystem {
    public static int day = 1;
    public static int hour = 0;
    public static String partOfDay = "Night";
    public static String season = "Spring";

    private static final String[] SEASONS = {"Spring", "Summer", "Autumn", "Winter"};

    public static void updateDays(){
        day++;
        updateSeason();
    }

    private static void updateSeason() {
        // Season changes every 7 days.
        // day 1-7: Spring (index 0)
        // day 8-14: Summer (index 1)
        // ...
        int seasonIndex = ((day - 1) / 7) % 4;
        season = SEASONS[seasonIndex];
    }

    public static void updateHours(){
        hour++;
        if(hour >= 24){
            hour = 0;
            updateDays();
        }

        if(hour > 4 && hour < 18){
            partOfDay ="Day";
        }
        else{
            partOfDay = "Night";
        }
    }

    public static int getHours(){
        return hour;
    }
    public static int getDays(){
        return day;
    }
}