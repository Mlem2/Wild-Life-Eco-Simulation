package entities.base;

public class EntityFactory {
    @FunctionalInterface
    public interface FakeConstructor<T, A, B>{
        T apply(A a, B b);
    }
    public static <T extends Entity> T CreateEntity(FakeConstructor<T, Integer, Integer> constructor,int x, int y){
        return constructor.apply(x,y);
    }
}
