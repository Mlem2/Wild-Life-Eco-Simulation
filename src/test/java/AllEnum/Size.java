package AllEnum;

public enum Size {
    SMALL(1),
    MEDIUM(2),
    LARGE(5);
    public final double multiplier;
    Size(double multiplier){
        this.multiplier=multiplier;
    }
}
