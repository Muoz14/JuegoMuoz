package gameObject;

import java.awt.*;

public class Constants {

    //Propiedades ventana

    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;

    // Propiedades del jugador

    public static final int FIRERATE = 180;
    public static final double DELTAANGLE = 0.1;
    public static final double ACC = 0.2;
    public static final double PLAYER_MAX_VEL = 5.0;

    //Propiedades del laser

    public static final double LASER_VEL = 12.0;

    //Propiedades del meteoro

    public static final double METEOR_VEL = 2.0;
    public static final int METEOR_SCORE = 30;

    // Propiedades del ufo

    public static final int NODE_RADIUS = 160;
    public static final double UFO_MASS = 60;
    public static final int UFO_MAX_VEL = 3;

    public static final double SWAY_AMPLITUDE_VISUAL = 10.0;
    public static final double SWAY_SPEED_VISUAL = 3.0;
    public static final int UFO_SCORE = 60;

    //Cadencia de disparo de ufo
    public static long UFO_FIRE_RATE = 1000;


    //Bonotes menu principal
    public static final String PLAY = "JUGAR";

    public static final String SCORE = "PUNTUACION";

    public static final String NAME = "INGRESE NOMBRE";

    public static final String SHIP = "NAVES";

    public static final String SETTINGS = "AJUSTES";

    public static final String EXIT = "SALIR";


}
