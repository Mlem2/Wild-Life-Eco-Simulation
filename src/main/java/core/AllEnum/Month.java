package AllEnum;

public enum Month {
    JAN(31),
    FEB(28),
    MAR(31),
    APR(30),
    MAY(31),
    JUNE(30),
    JUL(31),
    AUG(31),
    SEP(30),
    OCT(31),
    NOV(30),
    DEC(31);

    public final int numberOfDays;
    Month(int Days){
        this.numberOfDays=Days;
    }
}
