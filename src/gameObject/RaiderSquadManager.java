package gameObject;

import graphics.Assets;
import math.Vector2D;
import states.GameState;

import java.awt.Color;
import java.awt.Graphics;

/**
 * Estados del Asalto del Escuadrón.
 */
enum RaiderAssaultState {
    SPAWNING_1_RL, // Pasada 1
    WAITING_1,
    SPAWNING_2_LR, // Pasada 2
    WAITING_2,
    SPAWNING_3_TB, // Pasada 3
    WAITING_3,
    SPAWNING_4_BT, // Pasada 4
    FINISHING,     // Mostrando mensajes
    INACTIVE       // Evento terminado
}

/**
 * Objeto invisible que gestiona el asalto del escuadrón Raider.
 * Se encarga de los temporizadores y de spawnear las naves para CADA pasada.
 */
// --- MODIFICACIÓN: Ya no extiende GameObject ---
public class RaiderSquadManager {

    private GameState gameState;
    private RaiderAssaultState currentState;
    private Chronometer timer;

    // Tiempos (en milisegundos)
    // Duración de la pasada = (Distancia / Velocidad) * 1000
    // (Ajuste: (Distancia / px_por_frame) * (ms_por_frame))
    // Asumiendo 60 FPS (16.6ms por frame)
    private static final long PASS_DURATION_HORIZ = (long)((Constants.WIDTH / Constants.RAIDER_MAX_VEL) * 16.6) + 1000; // ~6.3s + 1s buffer
    private static final long PASS_DURATION_VERT = (long)((Constants.HEIGHT / Constants.RAIDER_MAX_VEL) * 16.6) + 1000; // ~4s + 1s buffer
    private static final long WAIT_TIME = 2000; // 2s de espera

    public RaiderSquadManager(GameState gameState) {
        // --- MODIFICACIÓN: Se eliminó super() ---
        this.gameState = gameState;
        this.timer = new Chronometer();
        this.currentState = RaiderAssaultState.SPAWNING_1_RL;

        // Mensaje inicial
        Message raiderMsg = new Message(new Vector2D(Constants.WIDTH / 2, 100),
                false, "¡ESCUADRÓN ENEMIGO DETECTADO!", Color.ORANGE, true, Assets.fontBig, gameState);
        raiderMsg.setLifespan(4000);
        gameState.addMessage(raiderMsg);
    }

    // --- MODIFICACIÓN: Se eliminó @Override ---
    public void update() {
        if (currentState == RaiderAssaultState.INACTIVE) return;

        timer.update();

        switch (currentState) {

            case SPAWNING_1_RL:
                // Spawnea 3 naves para la primera pasada
                spawnSquad(RaiderTask.FLY_RL);
                // Configura el timer para la siguiente pasada
                timer.run(PASS_DURATION_HORIZ + WAIT_TIME);
                currentState = RaiderAssaultState.WAITING_1;
                break;

            case WAITING_1:
                if (timer.isFinished()) {
                    currentState = RaiderAssaultState.SPAWNING_2_LR;
                }
                break;

            case SPAWNING_2_LR:
                spawnSquad(RaiderTask.FLY_LR);
                timer.run(PASS_DURATION_HORIZ + WAIT_TIME);
                currentState = RaiderAssaultState.WAITING_2;
                break;

            case WAITING_2:
                if (timer.isFinished()) {
                    currentState = RaiderAssaultState.SPAWNING_3_TB;
                }
                break;

            case SPAWNING_3_TB:
                spawnSquad(RaiderTask.FLY_TB);
                timer.run(PASS_DURATION_VERT + WAIT_TIME);
                currentState = RaiderAssaultState.WAITING_3;
                break;

            case WAITING_3:
                if (timer.isFinished()) {
                    currentState = RaiderAssaultState.SPAWNING_4_BT;
                }
                break;

            case SPAWNING_4_BT:
                spawnSquad(RaiderTask.FLY_BT);
                // Este es el último, solo esperamos que termine la pasada
                timer.run(PASS_DURATION_VERT);
                currentState = RaiderAssaultState.FINISHING;
                break;

            case FINISHING:
                if (timer.isFinished()) {
                    postFinalMessages();
                    currentState = RaiderAssaultState.INACTIVE;
                    // --- MODIFICACIÓN: Llama al nuevo método en GameState ---
                    gameState.onRaiderAssaultFinished();
                }
                break;

            case INACTIVE:
                // No hacer nada
                break;
        }
    }

    /**
     * Spawnea 3 naves Raider con la tarea y posición correctas.
     */
    private void spawnSquad(RaiderTask task) {
        int verticalSpacing = 100 + 20; // height + padding
        int horizontalSpacing = Constants.WIDTH / 4;

        for (int i = 0; i < 3; i++) {
            Vector2D pos = new Vector2D();

            // Calcular posiciones de spawn
            switch (task) {
                case FLY_RL:
                    pos.setX(Constants.WIDTH); // Spawn a la derecha
                    pos.setY((Constants.HEIGHT / 2) - verticalSpacing + (i * verticalSpacing));
                    break;
                case FLY_LR:
                    pos.setX(-100); // Spawn a la izquierda (width del Raider)
                    pos.setY((Constants.HEIGHT / 2) - verticalSpacing + (i * verticalSpacing));
                    break;
                case FLY_TB:
                    pos.setX(horizontalSpacing + (i * horizontalSpacing));
                    pos.setY(-100); // Spawn arriba
                    break;
                case FLY_BT:
                    pos.setX(horizontalSpacing + (i * horizontalSpacing));
                    pos.setY(Constants.HEIGHT); // Spawn abajo
                    break;
            }

            gameState.addObject(new Raider(pos, task, gameState));
        }
    }

    /**
     * Muestra los mensajes de fin de asalto.
     */
    private void postFinalMessages() {
        // Mensaje 1: "Saliendo"
        Message leavingMsg = new Message(
                new Vector2D(Constants.WIDTH / 2, Constants.HEIGHT / 2),
                false,
                "Saliendo del area peligrosa",
                Color.ORANGE,
                true,
                Assets.fontBig,
                gameState
        );
        leavingMsg.setLifespan(3000);
        gameState.addMessage(leavingMsg);

        // Mensaje 2: "Acaba con los restantes" (con delay)
        new Thread(() -> {
            try {
                Thread.sleep(2800);
            } catch (InterruptedException ignored) {}

            Message cleanupMsg = new Message(
                    new Vector2D(Constants.WIDTH / 2, Constants.HEIGHT / 2 + 60),
                    false,
                    "ACABA CON LOS METEORITOS RESTANTES",
                    Color.WHITE,
                    true,
                    Assets.fontMed,
                    gameState
            );
            cleanupMsg.setLifespan(3500);
            gameState.addMessage(cleanupMsg);
        }).start();
    }

    public void pauseTimers() {
        timer.pause();
    }

    public void resumeTimers() {
        timer.resume();
    }

}