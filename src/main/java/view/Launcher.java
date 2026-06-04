package view;

public class Launcher {
    public static void main(String[] args) {
        // Redirigir al Main del backend para asegurar la inicialización correcta
        // Main está en el paquete default
        try {
            Class<?> mainClass = Class.forName("Main");
            java.lang.reflect.Method mainMethod = mainClass.getMethod("main", String[].class);
            mainMethod.invoke(null, (Object) args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}