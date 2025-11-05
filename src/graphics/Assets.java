package graphics;

import javax.sound.sampled.Clip;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Assets {

    //Objeto que almacenara la imagen de la nave del jugador
    public static BufferedImage player1;
    public static BufferedImage player2;

    //efectos de propulsion
    public static BufferedImage propulsor1;
    public static BufferedImage propulsor2;

    //lasers
    public static BufferedImage laserPersonalizado1;
    public static BufferedImage laserPersonalizado2;

    //Laser UFO
    public static BufferedImage ufoLaser;

    //Meteoritos
    public static BufferedImage[] bigs = new BufferedImage[3];
    public static BufferedImage[] meds = new BufferedImage[3];
    public static BufferedImage[] smalls = new BufferedImage[3];
    public static BufferedImage[] tinies = new BufferedImage[3];

    //Animacion Explosion

    public static BufferedImage[] exp = new BufferedImage[9];

    // Nave ufo

    public static BufferedImage ufo;

    // Numeros puntuacion

    public static BufferedImage[] numbers = new BufferedImage[11];

    // Vidas

    public static BufferedImage life;

    // Fuentes

    public static Font fontBig;
    public static Font fontMed;

    // Sonidos

    public static Clip backgroundMusic, explosion, playerLoose, playerShoot, ufoShoot, hoverSound, buttonSelected, gameOver;

    //Botones

    public static BufferedImage buttonS1;
    public static BufferedImage buttonS2;
    public static BufferedImage buttonPause;

    public static void init(){

        player1 = Loader.ImageLoader("/ships/playerShip1_Muoz.png");
        player2 = Loader.ImageLoader("/ships/playerShip4_Muoz.png");

        propulsor1 = Loader.ImageLoader("/effects/fire03.png");
        propulsor2 = Loader.ImageLoader("/effects/fire01.png");

        laserPersonalizado1 = Loader.ImageLoader("/lasers/laserRed02.png");
        laserPersonalizado2 = Loader.ImageLoader("/lasers/laser01-Pers.png");

        // Cargar cada familia de meteoros
        for (int i = 0; i < 3; i++) {

            bigs[i] = Loader.ImageLoader("/meteors/big" + (i + 1) + ".png");

            meds[i] = Loader.ImageLoader("/meteors/med" + (i + 1) + ".png");

            smalls[i] = Loader.ImageLoader("/meteors/small" + (i + 1) + ".png");

            tinies[i] = Loader.ImageLoader("/meteors/tiny" + (i + 1) + ".png");

        }

        //Carga de animacion de explosion
        for (int i = 0; i < exp.length; i++){

            exp[i] = Loader.ImageLoader("/explosion/"+i+".png");

        }

        //Carga de nave enemiga 1 UFO
        ufo = Loader.ImageLoader("/ships/ufo2.png");

        //Laser del ufo
        ufoLaser = Loader.ImageLoader("/lasers/laserGreen11.png");

        //Puntuacion numeros
        for(int i = 0; i < numbers.length; i++){

            numbers[i] = Loader.ImageLoader("/numbers/" + i + ".png");

        }

        //Carga de icono de vidas
        life = Loader.ImageLoader("/others/life.png");

        //Fuentes
        fontBig = Loader.loadFont("/fonts/futureFont.ttf", 42);
        fontMed = Loader.loadFont("/fonts/futureFont.ttf", 20);

        //Carga de botones de menu
        buttonS1 = Loader.ImageLoader("/ui/button1State1.png");
        buttonS2 = Loader.ImageLoader("/ui/button1State2.png");
        buttonPause = Loader.ImageLoader("/ui/buttonPause.png");

        //Carga de Sonidos
        backgroundMusic = Loader.loadSound("/sounds/backgroundMusic.wav");
        explosion = Loader.loadSound("/sounds/explosion.wav");
        playerLoose = Loader.loadSound("/sounds/playerLoose2.wav");
        playerShoot = Loader.loadSound("/sounds/playerShoot.wav");
        ufoShoot = Loader.loadSound("/sounds/ufoShoot.wav");
        buttonSelected = Loader.loadSound("/sounds/ButtonSelected.wav");
        gameOver = Loader.loadSound("/sounds/GameOver.wav");
        hoverSound = Loader.loadSound("/sounds/HoverBSound.wav");

    }

}
