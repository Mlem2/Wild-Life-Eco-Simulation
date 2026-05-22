package entities.base;

public class EntityFactory {
    @FunctionalInterface
    public interface FakeConstructor<T, A, B, C>{
        T apply(A a, B b, C c);
    }
    public static <T extends Entity> T CreateEntity(FakeConstructor<T, String, Integer, Integer> constructor, String name,int x, int y){
        return constructor.apply(name,x,y);
    }
}
