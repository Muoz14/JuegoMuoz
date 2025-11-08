package graphics;

import math.Vector2D;

import java.awt.image.BufferedImage;

public class Animation {

    private BufferedImage[] frames; //Fotogramas de la animacion
    private int velocity; //Velocidad de la animacion
    private int index; //Indice actual de la imagen dibujada
    private boolean running;
    private Vector2D position; //Posicion en la que se dibujara la animacion
    private long time, lastTime; //Variables auxiliares para controlar tiempo

    private boolean looping; // Bandera para saber si debe repetirse

    /**
     * Constructor para animaciones de un solo uso (como explosiones).
     */
    public Animation(BufferedImage[] frames, int velocity, Vector2D position){
        this(frames, velocity, position, false); // Por defecto, no hace bucle
    }

    /**
     * Constructor completo con opcion de bucle.
     */
    public Animation(BufferedImage[] frames, int velocity, Vector2D position, boolean looping){
        this.frames = frames;
        this.velocity = velocity;
        this.position = position;
        this.looping = looping;
        index = 0;
        running = true;
        time = 0;
        lastTime = System.currentTimeMillis();
    }

    //Se encargara de cambiar los fotogramas al pasar el tiempo para la animacion
    public void update(){

        time += System.currentTimeMillis() - lastTime;
        lastTime = System.currentTimeMillis();

        if (time > velocity){
            time = 0;

            // Solo incrementar si la animacion esta activa
            if (running) {
                index ++;
            }

            // Revisar si el indice se paso
            if (index >= frames.length){
                if (looping) {
                    // Si es un bucle, reiniciar
                    index = 0;
                } else {
                    // Si no es bucle (explosion), detenerse en el ultimo frame
                    running = false;
                    index = frames.length - 1; // Clamp al ultimo frame valido
                }
            }
        }
    }

    public boolean isRunning(){
        return running;
    }

    public Vector2D getPosition(){
        return position;
    }

    public BufferedImage getCurrentFrame(){
        // Esta linea (63) es la que causaba el error
        // Ahora 'index' nunca sera >= frames.length
        return frames[index];
    }
}