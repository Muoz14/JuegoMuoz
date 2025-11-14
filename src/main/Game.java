package main;

public class Game {

    public static void main(String[] args) {

        // Forzar la aceleracion por hardware de Java2D (OpenGL/Direct3D)
        // Esto soluciona el lag en laptops con graficos integrados (iGPU)
        // al forzar el uso de la GPU en lugar del CPU para dibujar las imagenes pesadas del fondo.
        System.setProperty("sun.java2d.opengl", "true");
        System.setProperty("sun.java2d.d3d", "true");
        System.setProperty("sun.java2d.ddforcevram", "true");

        new Window().start();

    }
}