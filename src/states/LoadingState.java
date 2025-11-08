package states;

import gameObject.Constants;
import gameObject.ShipLibrary;
import graphics.Assets;
import graphics.Text;
import math.Vector2D;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class LoadingState extends State {

    private Thread loadingThread;
    private volatile boolean loadingComplete = false;
    private volatile String loadingStatus = "CARGANDO...";

    public LoadingState() {
        // Inicia la carga en un hilo separado para no congelar el juego
        loadingThread = new Thread(this::loadAssets);
        loadingThread.start();
    }

    /**
     * Metodo que se ejecuta en el hilo secundario
     */
    private void loadAssets() {
        try {
            // Este es el metodo que causa el congelamiento.
            // Ahora se ejecuta de forma segura en segundo plano.
            loadingStatus = "CARGANDO IMAGENES...";
            Assets.init(); // Llama a tu metodo de carga

            loadingStatus = "CARGANDO NAVES...";
            ShipLibrary.init();

            loadingStatus = "CARGA COMPLETA!";

        } catch (Exception e) {
            loadingStatus = "ERROR AL CARGAR ASSETS!";
            e.printStackTrace();
        } finally {
            // Avisa al hilo principal que la carga termino
            loadingComplete = true;
        }
    }
    @Override
    public void update() {
        // Cuando el hilo secundario termina, cambiamos al Menu
        if (loadingComplete) {
            State.changeState(new MenuState()); // Asumo que tienes MenuState
        }
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Dibuja un fondo simple
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, Constants.WIDTH, Constants.HEIGHT);

        // Dibuja el texto "CARGANDO..."
        Text.drawText(
                g,
                loadingStatus,
                new Vector2D(Constants.WIDTH / 2, Constants.HEIGHT / 2),
                true, // centrado
                Color.WHITE,
                Assets.fontMed // Usamos la fuente que se carga rapido
        );
    }
}