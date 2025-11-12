package graphics;

import java.awt.*;
import gameObject.Constants;
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

    // Sonidos (Cambiamos de Clip a Sound)
    public static Sound backgroundMusic, explosion, playerLoose, playerShoot, ufoShoot, hoverSound, buttonSelected, gameOver;

    //Botones
    public static BufferedImage buttonS1;
    public static BufferedImage buttonS2;
    public static BufferedImage buttonPause;

    //Jefes
    public static BufferedImage miniBoss;

    //Fondo de GameState
    public static BufferedImage layer01;
    public static BufferedImage layer02;
    public static BufferedImage layer03;

    //POWER-UPS
    public static BufferedImage shield_bronze, shield_silver, shield_gold;
    public static BufferedImage speed_shoot, extra_gun;

    //ANIMACION DE ESCUDO
    public static BufferedImage[] shield_effect = new BufferedImage[3];

    public static void init(){

        // Carga de fuentes PRIMERO, porque la pantalla de carga las usa
        fontBig = Loader.loadFont("/fonts/futureFont.ttf", 42);
        fontMed = Loader.loadFont("/fonts/futureFont.ttf", 20);

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

        //Carga de botones de menu
        buttonS1 = Loader.ImageLoader("/ui/button1State1.png");
        buttonS2 = Loader.ImageLoader("/ui/button1State2.png");
        buttonPause = Loader.ImageLoader("/ui/buttonPause.png");

        // --- INICIALIZAR OBJETOS SOUND Y REGISTRARLOS ---

        //Carga de Sonidos
        // new Sound(path, loop, isMusic)
        backgroundMusic = new Sound("/sounds/backgroundMusic.wav", true, true);
        explosion = new Sound("/sounds/explosion.wav", false, false);
        playerLoose = new Sound("/sounds/playerLoose2.wav", false, false);
        playerShoot = new Sound("/sounds/playerShoot.wav", false, false);
        ufoShoot = new Sound("/sounds/ufoShoot.wav", false, false);
        buttonSelected = new Sound("/sounds/ButtonSelected.wav", false, false);
        gameOver = new Sound("/sounds/GameOver.wav", false, false);
        hoverSound = new Sound("/sounds/HoverBSound.wav", false, false);

        // Registrar todos los sonidos en el manager
        SoundManager.getInstance().registerSound(backgroundMusic);
        SoundManager.getInstance().registerSound(explosion);
        SoundManager.getInstance().registerSound(playerLoose);
        SoundManager.getInstance().registerSound(playerShoot);
        SoundManager.getInstance().registerSound(ufoShoot);
        SoundManager.getInstance().registerSound(buttonSelected);
        SoundManager.getInstance().registerSound(gameOver);
        SoundManager.getInstance().registerSound(hoverSound);

        //Carga de jefes
        miniBoss = Loader.ImageLoader("/boss/miniBoss.png");

        // La altura original es 360, la altura de la ventana es 720 (Constants.HEIGHT)
        // El factor de escala es 720 / 360 = 2.0
        // El ancho original es 5760. El nuevo ancho sera 5760 * 2 = 11520

        int targetHeight = Constants.HEIGHT;
        int targetWidth = (int) (5760 * ( (double)Constants.HEIGHT / 360.0 )); // 11520

        layer01 = Loader.ImageLoader("/backgrounds/Layer 01.png", targetWidth, targetHeight);
        layer02 = Loader.ImageLoader("/backgrounds/Layer 02.png", targetWidth, targetHeight);
        layer03 = Loader.ImageLoader("/backgrounds/Layer 03.png", targetWidth, targetHeight);

        //Carga de power-ups
        shield_bronze = Loader.ImageLoader("/power-ups/shield_bronze.png");
        shield_silver = Loader.ImageLoader("/power-ups/shield_silver.png");
        shield_gold = Loader.ImageLoader("/power-ups/shield_gold.png");

        speed_shoot = Loader.ImageLoader("/power-ups/speed_shoot.png");
        extra_gun = Loader.ImageLoader("/power-ups/extra_gun.png");

        //Carga animacion de escudo
        shield_effect[0] = Loader.ImageLoader("/shield/shield1.png");
        shield_effect[1] = Loader.ImageLoader("/shield/shield2.png");
        shield_effect[2] = Loader.ImageLoader("/shield/shield3.png");

    }
}