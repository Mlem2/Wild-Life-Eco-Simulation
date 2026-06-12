package core;

import allEnum.Month;

public class TimeSystem {
    public static int year = 2000;
    public static int month = 1;
    public static int day = 1;
    public static int hour = 0;
    public static int minute = 0;
    public static String partOfDay = "Night";
    public static String season = "Spring";

    public static int getLimit(){
        Month m = Month.values()[month - 1];
        int days = m.numberOfDays;
        if(m.equals(Month.FEB)){
            if(year % 400 == 0 || (year % 4 == 0 && year % 100 != 0)) return days + 1;
        }
        return days;
    }

    public static void updateDays(){
        day++;
        hour = 0;
    }
    public static void updateMonths(){
        month++;
        day = 1;
        updateSeasonFromMonth();
    }
    public static void updateYears(){
        year++;
        month = 1;
        day = 1;
        updateSeasonFromMonth();
    }
    public static void updateHours(){
        hour++;
        minute = 0;
        updatePartOfDay();
    }
    public static void updateMinute(){
        minute+=30;
    }

    private static void updateSeasonFromMonth() {
        if(month >= 1 && month < 4) season = "Spring";
        else if(month >= 4 && month < 7) season = "Summer";
        else if(month >= 7 && month < 10) season = "Autumn";
        else season = "Winter";
    }

    private static void updatePartOfDay() {
        if(hour > 4 && hour < 18) {
            partOfDay = "Day";
        } else {
            partOfDay = "Night";
        }
    }

    private static int getDaysInMonth(int year, int month) {
        Month m = Month.values()[month - 1];
        int days = m.numberOfDays;
        if (m.equals(Month.FEB)) {
            if (year % 400 == 0 || (year % 4 == 0 && year % 100 != 0)) {
                days += 1;
            }
        }
        return days;
    }

    public static void jumpToLastDayOfPreviousSeasonAt2330() {
        int targetYear = year;
        int targetMonth;

        switch (season.toLowerCase()) {
            case "spring" -> targetMonth = 3;
            case "summer" -> targetMonth = 6;
            case "autumn" -> targetMonth = 9;
            case "winter" -> targetMonth = 12;
            default -> targetMonth = month;
        }

        int targetDay = getDaysInMonth(targetYear, targetMonth);
        year = targetYear;
        month = targetMonth;
        day = targetDay;
        hour = 23;
        minute = 30;
        updateSeasonFromMonth();
        updatePartOfDay();
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

}
