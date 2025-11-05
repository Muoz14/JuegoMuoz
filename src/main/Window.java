package main;

import gameObject.Constants;
import gameObject.ShipLibrary;
import graphics.Assets;
import input.KeyBoard;
import input.MouseInput;
import states.GameState;
import states.MenuState;
import states.State;

import javax.swing.JFrame;
import java.awt.*;
import java.awt.image.BufferStrategy;

//Permite crear la ventana en la que se correra el juego
public class Window extends JFrame implements Runnable{

    //Objeto para poder dibujar en pantalla
    private Canvas canvas;

    //Hilo principal para los eventos y logica del juego, para no sobrecargar el JFrame que maneja los eventos de ventana
    private Thread thread;

    //Objeto boleano para manejar el estado del ciclo principal del juego
    private boolean running = false;

    private BufferStrategy bs;
    private Graphics g;

    //Limitador de FPS para que el juego corra de misma manera en diferentes computadoras
    private final int FPS = 60;

    //Tiempo requerido para aumentar fotogramas en nanosegundos para mayor precision
    private double TARGETTIME = 1000000000 / FPS;

    //Esta almacenara temporalmente el tiempo que vaya pasando
    private double delta = 0;

    //Promedio de FPS
    private int AVERAGEFPS = FPS; //Para saber la cantidad de fps

    private GameState gameState;

    private KeyBoard keyBoard;

    private MouseInput mouseInput;

    //Consturctor con parametros generales
    public Window(){

        setTitle("Space Star Ships Game");
        setSize(Constants.WIDTH, Constants.HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Permite cerrar ventana al presionar x
        setResizable(false); // Para no permitir redimension de ventana por parte del usuario
        setLocationRelativeTo(null); // Para que la ventana se abra en el centro de la pantalla al iniciarse el juego

        canvas = new Canvas();
        keyBoard = new KeyBoard();
        mouseInput = new MouseInput();

        //Limitar tamanio de dibujado a las dimsenisones de la ventana
        canvas.setPreferredSize(new Dimension(Constants.WIDTH, Constants.HEIGHT));
        canvas.setMaximumSize(new Dimension(Constants.WIDTH, Constants.HEIGHT));
        canvas.setMinimumSize(new Dimension(Constants.WIDTH, Constants.HEIGHT));

        //Permitir recibir entradas de teclado
        canvas.setFocusable(true);

        add(canvas); //Aniadimos el canvas a la ventana

        canvas.addKeyListener(keyBoard);//Implementamos el keylistener para que nos permita capturar la entrada de teclado

        canvas.addMouseListener(mouseInput);

        canvas.addMouseMotionListener(mouseInput);

        setVisible(true); //Para hacer visible la ventana al ejecutarse

    }

    //Metodo para actualizar objetos en pantalla
    private void update(){

        keyBoard.update();
        State.getCurrentState().update();

    }

    //Metodo para dibujar objetos en pantalla
    private void draw(){

        bs = canvas.getBufferStrategy();

        if (bs == null){

            canvas.createBufferStrategy(3); //Numeros de Buffers que usa un canvas, y en esta linea se lo asignamos
            return;

        }

        g = bs.getDrawGraphics();

        //Desde aqui empezamos a dibujar

        //Fondo de color negro
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, Constants.WIDTH, Constants.HEIGHT);

        //Para dibujar objetos desde otras clases
        State.getCurrentState().draw(g);


        //Hasta aqui termina

        g.dispose();
        bs.show();

    }

    //Metodo para inicializar los recursos (imagenes y videos)
    private void init(){

        Assets.init();

        ShipLibrary.init();

        State.changeState(new MenuState());

    }

    //Metodo abstracto
    @Override
    public void run() {

        long now = 0; //Para registrar el tiempo que vaya pasando
        long lastTime = System.nanoTime(); //Nos devolvera la hora actual del sistema en nanosegundos, para mayor presicion
        int frames = 0;
        long time = 0;

        init();

        //Ciclo principal encargado de actualizar la posicion de todos los objetos del juego
        //Para posteriormente dibujar constantemente y manetener el juego actualizado

        while(running){

            now = System.nanoTime(); // este valor sera diferente al de lasTime por el tiempo que se toma en ingresar al bucle
            delta += (now - lastTime) / TARGETTIME;
            time += (now - lastTime);
            lastTime = now;

            if (delta >= 1){

                update();
                draw();
                delta --; // Para cronometar el siguiente fotograma
                frames ++;

            }

            if (time >= 1000000000){

                AVERAGEFPS = frames;
                frames = 0;
                time = 0;

            }

        }

        stop();

    }

    //Para iniciar el hilo principal
    public void start(){

        //Esta clase recibe como parametro el constructor
        thread = new Thread(this);
        thread.start(); // Llama el metodo abstracto run
        running = true; //Para iniciar el ciclo principal

    }

    //Para detener el hilo principal
    public void stop(){
        //Detiene el hilo y lo encerramos en un try catch para capturar erroeres
        try {
            thread.join();
            running = false; //Para detener el ciclo principal
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
