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

    public Animation(BufferedImage[] frames, int velocity, Vector2D position){

        this.frames = frames;
        this.velocity = velocity;
        this.position = position;
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
            index ++;

            if (index >= frames.length){

                running = false;

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

        return frames[index];

    }

}
