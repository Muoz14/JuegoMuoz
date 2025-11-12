package states;

import gameObject.*;
import graphics.Animation;
import graphics.Assets;
import graphics.Background;
import graphics.Sound;
import graphics.SoundManager;
import input.KeyBoard;
import input.MouseInput;
import math.Vector2D;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class GameState extends State {

    private Player player;
    private ArrayList<MovingObject> movingObjects = new ArrayList<>();
    private ArrayList<MovingObject> objectsToAdd = new ArrayList<>();
    private ArrayList<MovingObject> objectsToRemove = new ArrayList<>();
    private ArrayList<Animation> explosion = new ArrayList<>();
    private ArrayList<Message> messages = new ArrayList<>();
    private ArrayList<Message> messagesToRemove = new ArrayList<>();

    private Background background;

    public int meteors;
    private int waves = 1;
    private int lives = 100;

    // Puntuaciones
    private int score = 0;

    // Control de oleadas
    private boolean waveCleared = false;
    private long waveClearTime = 0;
    private boolean nextWaveStarting = false;

    private boolean paused = false;
    private boolean startingCountdown = true;
    private long countdownStartTime;
    private int countdownValue = 3;
    private boolean firstWaveStarted = false;
    private boolean escPressedLastFrame = false;

    // Aparicion del UFO
    private long lastUfoSpawnTime = 0;
    private static final long UFO_SPAWN_INTERVAL = 15000;

    // --- SPAWN DE POWER-UPS (LOGICA MODIFICADA) ---
    private static final long POWERUP_BURST_INTERVAL = 15000; // 15s entre el INICIO de cada rafaga
    private static final long POWERUP_CHAIN_DELAY = 1500;    // 1.5s entre spawns en una rafaga
    private static final double POWERUP_THIRD_CHANCE = 0.40; // 40% de un 3er power-up
    private static final int MAX_POWERUPS_ON_SCREEN = 3;

    private Chronometer burstSpawnTimer; // --- RENOMBRADO --- (antes powerUpSpawnTimer)
    private Chronometer burstChainTimer; // --- NUEVO --- (Timer para 1.5s)
    private int burstChainStep = 0;      // --- NUEVO --- (0=idle, 1=espera 2do, 2=espera 3ro)

    // Boton pausa
    private BufferedImage pauseButtonImg;
    private Rectangle pauseButtonBounds;
    private int pauseButtonOffsetX = 20;
    private int pauseButtonOffsetY = 20;

    // Menu de pausa
    private Rectangle resumeButtonBounds;
    private Rectangle settingsButtonBounds;
    private Rectangle menuButtonBounds;
    private BufferedImage resumeButtonImg;
    private BufferedImage settingsButtonImg;
    private BufferedImage menuButtonImg;
    private boolean showPauseMenu = false;
    private float pauseOverlayAlpha = 0.5f;

    // Botones hover
    private BufferedImage resumeButtonHoverImg;
    private BufferedImage settingsButtonHoverImg;
    private BufferedImage menuButtonHoverImg;

    public GameState() {

        ShipData playerData = ShipLibrary.getSelectedShip();
        BufferedImage laserImg = ShipLibrary.getSelectedLaserImage();

        player = new Player(new Vector2D(560, 320), new Vector2D(), this, playerData, laserImg);
        movingObjects.add(player);

        background = new Background();
        meteors = 1;

        // --- MODIFICADO: Inicializar timers de rafaga ---
        burstSpawnTimer = new Chronometer();
        burstSpawnTimer.run(POWERUP_BURST_INTERVAL); // Inicia el timer principal
        burstChainTimer = new Chronometer(); // Timer secundario, no se inicia aun
        // --- FIN MODIFICADO ---

        // Boton pausa
        pauseButtonImg = Assets.buttonPause;
        int buttonWidth = pauseButtonImg.getWidth();
        int buttonHeight = pauseButtonImg.getHeight();
        pauseButtonBounds = new Rectangle(pauseButtonOffsetX, pauseButtonOffsetY, buttonWidth, buttonHeight);

        // Botones menu de pausa
        resumeButtonImg = Assets.buttonS1;
        settingsButtonImg = Assets.buttonS1;
        menuButtonImg = Assets.buttonS1;

        // Hover
        resumeButtonHoverImg = Assets.buttonS2;
        settingsButtonHoverImg = Assets.buttonS2;
        menuButtonHoverImg = Assets.buttonS2;

        resumeButtonBounds = new Rectangle(Constants.WIDTH / 2 - 180, Constants.HEIGHT / 2 - 100, 360, 60);
        settingsButtonBounds = new Rectangle(Constants.WIDTH / 2 - 180, Constants.HEIGHT / 2, 360, 60);
        menuButtonBounds = new Rectangle(Constants.WIDTH / 2 - 180, Constants.HEIGHT / 2 + 100, 360, 60);

        startCountdown(); // <--- Metodo de countdown

        // Musica de fondo unica
        if (Assets.backgroundMusic != null) {
            Assets.backgroundMusic.stop();
        }

        Assets.backgroundMusic.setVolume(SettingsData.getVolume());
        Assets.backgroundMusic.play();
    }

    // ------------------ Countdown ------------------
    public void startCountdown() {
        // ... (codigo sin cambios) ...
        startingCountdown = true;
        countdownStartTime = System.currentTimeMillis();
        countdownValue = 3;
    }

    private void handleCountdown() {
        // ... (codigo sin cambios) ...
        long elapsed = System.currentTimeMillis() - countdownStartTime;
        if (elapsed >= 1000) {
            countdownValue--;
            countdownStartTime = System.currentTimeMillis();
        }

        if (countdownValue <= 0) {
            startingCountdown = false;
            if (!firstWaveStarted) {
                startWave();
                firstWaveStarted = true;
            }
        }
    }

    private void startWave() {
        // ... (codigo sin cambios) ...
        Message waveMessage = new Message(
                new Vector2D(Constants.WIDTH / 2, Constants.HEIGHT / 2),
                false,
                "OLEADA " + waves,
                Color.WHITE,
                true,
                Assets.fontBig,
                this
        );
        waveMessage.setLifespan(3500);
        addMessage(waveMessage);

        for (int i = 0; i < meteors; i++) {
            double x = i % 2 == 0 ? Math.random() * Constants.WIDTH : 0;
            double y = i % 2 == 0 ? 0 : Math.random() * Constants.HEIGHT;
            Vector2D velocity = new Vector2D(Math.random() * 2 - 1, Math.random() * 2 - 1).normalize().scale(2);
            int family = (int) (Math.random() * 3);
            Meteor meteor = new Meteor(
                    new Vector2D(x, y),
                    velocity,
                    2,
                    Assets.bigs[family],
                    this,
                    Size.BIG,
                    family
            );
            addObject(meteor);
        }
    }
    public void playExplosion(Vector2D position) {
        // ... (codigo sin cambios) ...
        explosion.add(new Animation(
                Assets.exp,
                110,
                position.subtract(new Vector2D(
                        Assets.exp[0].getWidth() / 2,
                        Assets.exp[0].getHeight() / 2))
        ));
    }

    private void spawnUfo() {
        // ... (codigo sin cambios) ...
        long now = System.currentTimeMillis();
        if (now - lastUfoSpawnTime < UFO_SPAWN_INTERVAL) return;

        lastUfoSpawnTime = now;

        double x = 0, y = 0;
        int side = (int) (Math.random() * 4);
        switch (side) {
            case 0:
                x = Math.random() * Constants.WIDTH;
                y = 0;
                break;
            case 1:
                x = Math.random() * Constants.WIDTH;
                y = Constants.HEIGHT;
                break;
            case 2:
                x = 0;
                y = Math.random() * Constants.HEIGHT;
                break;
            case 3:
                x = Constants.WIDTH;
                y = Math.random() * Constants.HEIGHT;
                break;
        }

        ArrayList<Vector2D> path = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            double posX = Math.random() * Constants.WIDTH;
            double posY = Math.random() * Constants.HEIGHT;
            path.add(new Vector2D(posX, posY));
        }

        Ufo ufo = new Ufo(
                new Vector2D(x, y),
                new Vector2D(),
                Constants.UFO_MAX_VEL,
                Assets.ufo,
                path,
                this
        );
        addObject(ufo);
    }

    // --- METODOS PARA POWER-UPS ---

    // --- METODO REESCRITO ---
    /**
     * Gestiona la logica de aparicion de rafagas de power-ups.
     * Se llama en cada frame desde update().
     */
    private void updatePowerUpSpawns() {

        // 1. Revisar si es momento de INICIAR una nueva rafaga
        burstSpawnTimer.update();
        if (burstSpawnTimer.isFinished() && burstChainStep == 0) {

            // Es hora de intentar el 1er spawn
            if (trySpawnOnePowerUp()) {
                // Exito. Empezar la cadena
                burstChainStep = 1; // Estado: "esperando al 2do"
                burstChainTimer.run(POWERUP_CHAIN_DELAY); // Iniciar timer de 1.5s
            }

            // Reiniciar el timer principal de rafagas (15s)
            // Lo reiniciamos haya funcionado o no, para que no intente
            // spawnear cada frame si la pantalla esta llena.
            burstSpawnTimer.run(POWERUP_BURST_INTERVAL);
        }

        // 2. Revisar si estamos DENTRO de una rafaga (esperando al 2do o 3ro)
        if (burstChainStep > 0) {
            burstChainTimer.update();

            if (burstChainTimer.isFinished()) {

                if (burstChainStep == 1) {
                    // Estaba esperando al 2do
                    if (trySpawnOnePowerUp()) {
                        // Exito. Ver si hay un 3ro (40% chance)
                        if (Math.random() < POWERUP_THIRD_CHANCE) {
                            burstChainStep = 2; // Estado: "esperando al 3ro"
                            burstChainTimer.run(POWERUP_CHAIN_DELAY); // Reiniciar timer 1.5s
                        } else {
                            burstChainStep = 0; // Se acabo la rafaga
                        }
                    } else {
                        burstChainStep = 0; // No pudo spawnear (pantalla llena), se acabo la rafaga
                    }
                }

                else if (burstChainStep == 2) {
                    // Estaba esperando al 3ro
                    trySpawnOnePowerUp(); // Intentar spawnear el 3ro (no nos importa si falla)
                    burstChainStep = 0; // La rafaga SIEMPRE termina aqui
                }
            }
        }
    }

    /**
     * Intenta spawnear un unico power-up, respetando el limite en pantalla.
     * @return true si el power-up se anadio, false si no (pantalla llena).
     */
    private boolean trySpawnOnePowerUp() {
        // 1. Contar cuantos power-ups hay en pantalla
        int currentPowerUps = 0;
        for (MovingObject obj : movingObjects) {
            if (obj instanceof PowerUp) {
                currentPowerUps++;
            }
        }

        // 2. Si esta lleno, abortar
        if (currentPowerUps >= MAX_POWERUPS_ON_SCREEN) {
            return false;
        }

        // 3. Si hay espacio, crear y anadir uno
        PowerUpType type = selectPowerUpType();
        double x = Math.random() * (Constants.WIDTH - 100) + 50;
        double y = Math.random() * (Constants.HEIGHT - 100) + 50;
        Vector2D position = new Vector2D(x, y);

        PowerUp powerUp = new PowerUp(position, type, this);
        addObject(powerUp);
        return true;
    }

    /**
     * Decide que tipo de power-up spawnear
     */
    private PowerUpType selectPowerUpType() {
        // ... (codigo sin cambios) ...
        double rand = Math.random();

        // 5% Oro   (0.0 -> 0.05)
        if (rand < 0.05) {
            return PowerUpType.GOLD;
        }
        // 15% Plata (0.05 -> 0.20)
        else if (rand < 0.20) {
            return PowerUpType.SILVER;
        }
        // 20% Multi-Disparo (0.20 -> 0.40)
        else if (rand < 0.40) {
            return PowerUpType.MULTI_SHOT;
        }
        // 25% Disparo Rapido (0.40 -> 0.65)
        else if (rand < 0.65) {
            return PowerUpType.RAPID_FIRE;
        }
        // 35% Bronce (0.65 -> 1.0)
        else {
            return PowerUpType.BRONZE;
        }
    }

    @Override
    public void update() {
        KeyBoard.update();
        MouseInput.update();
        Point mouse = MouseInput.getMousePosition();

        // Actualizar explosiones
        for (int i = explosion.size() - 1; i >= 0; i--) {
            Animation anim = explosion.get(i);
            anim.update();
            if (!anim.isRunning()) explosion.remove(i);
        }

        // Pausa con boton
        if (pauseButtonBounds.contains(mouse) && MouseInput.isPressed()) {
            Assets.buttonSelected.play();
            paused = true;
            showPauseMenu = true;
            MouseInput.releaseClick();
            pauseMusic();
            SoundManager.getInstance().pauseAll();
        }

        // Pausa con ESC
        boolean escNow = KeyBoard.isKeyPressed(KeyEvent.VK_ESCAPE);
        if (escNow && !escPressedLastFrame) {
            paused = !paused;
            showPauseMenu = paused;

            if (paused) {
                pauseMusic();
                SoundManager.getInstance().pauseAll();
            } else {
                startCountdown();
                resumeMusic();
            }
        }
        escPressedLastFrame = escNow;

        if (paused && showPauseMenu) {
            handlePauseMenu(mouse);
            KeyBoard.endFrame();
            return;
        }

        if (startingCountdown) {
            handleCountdown();
            return;
        }

        // ------------------ Actualizar objetos ------------------
        background.update();

        for (int i = 0; i < movingObjects.size(); i++) {
            movingObjects.get(i).update();
        }

        // Reaparicion del player
        if (player.isDead() && !player.isSpawning()) {
            player.startRespawn();
        }
        if (player.isSpawning()) {
            player.updateSpawnTimer();
        }

        // Mensajes
        long currentTime = System.currentTimeMillis();
        for (Message msg : messages) {
            msg.update();
            if (msg.isExpired(currentTime)) messagesToRemove.add(msg);
        }

        // Aplicar anadidos y removidos
        if (!objectsToAdd.isEmpty()) {
            movingObjects.addAll(objectsToAdd);
            objectsToAdd.clear();
        }

        if (!objectsToRemove.isEmpty()) {
            movingObjects.removeAll(objectsToRemove);
            objectsToRemove.clear();
        }

        if (!messagesToRemove.isEmpty()) {
            messages.removeAll(messagesToRemove);
            messagesToRemove.clear();
        }

        spawnUfo();

        // --- MODIFICADO ---
        updatePowerUpSpawns(); // Llamar a la nueva logica de spawn
        // --- FIN MODIFICADO ---

        handleWaveLogic();
    }

    private void handlePauseMenu(Point mouse) {
        // ... (codigo sin cambios) ...
        if (resumeButtonBounds.contains(mouse) && MouseInput.isPressed()) {
            paused = false;
            showPauseMenu = false;
            Assets.buttonSelected.play();
            startCountdown();
            resumeMusic();
            SoundManager.getInstance().resumeAll();
            MouseInput.releaseClick();
        }

        if (settingsButtonBounds.contains(mouse) && MouseInput.isPressed()) {
            Assets.buttonSelected.play();
            State.changeState(new SettingsState(this));
            MouseInput.releaseClick();
        }

        if (menuButtonBounds.contains(mouse) && MouseInput.isPressed()) {
            Assets.buttonSelected.play();
            stopMusic();
            State.changeState(new MenuState());
            MouseInput.releaseClick();
        }
    }

    private void handleWaveLogic() {
        // ... (codigo sin cambios) ...
        boolean hayMeteoros = false;
        boolean hayMiniBoss = false;

        // 1. Contar enemigos
        for (MovingObject obj : movingObjects) {
            if (obj instanceof Meteor) {
                hayMeteoros = true;
            }
            if (obj instanceof MiniBoss) {
                hayMiniBoss = true;
            }
        }

        // 2. Revisar si la oleada normal termino
        if (!hayMeteoros && !hayMiniBoss && firstWaveStarted && !waveCleared && !nextWaveStarting) {
            waveCleared = true;
            waveClearTime = System.currentTimeMillis();

            Message completeMsg = new Message(
                    new Vector2D(Constants.WIDTH / 2, Constants.HEIGHT / 2),
                    false,
                    "OLEADA COMPLETADA!",
                    Color.WHITE,
                    true,
                    Assets.fontBig,
                    this
            );
            completeMsg.setLifespan(3000);
            addMessage(completeMsg);
        }
        // 3. NUEVO CASO: Meteoros despejados, PERO el boss SIGUE VIVO
        else if (!hayMeteoros && hayMiniBoss && firstWaveStarted && !waveCleared && !nextWaveStarting) {
            nextWaveStarting = true;
            spawnMeteorSubWave(waves);
        }


        // 4. Este bloque maneja la transicion a la SIGUIENTE oleada
        if (waveCleared && !nextWaveStarting) {
            long elapsed = System.currentTimeMillis() - waveClearTime;
            if (elapsed >= 3000) {
                waveCleared = false;
                nextWaveStarting = true;
                waves++;
                meteors++;

                Message waveMessage = new Message(
                        new Vector2D(Constants.WIDTH / 2, Constants.HEIGHT / 2),
                        false,
                        "OLEADA " + waves + "!",
                        Color.WHITE,
                        true,
                        Assets.fontBig,
                        this
                );
                waveMessage.setLifespan(3000);
                addMessage(waveMessage);

                new Thread(() -> {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ignored) {}
                    startWave();
                    nextWaveStarting = false;

                    // Aparicion del MiniBoss cada 2 oleadas
                    if (waves % 2 == 0) {
                        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

                        ShipData selectedShip = ShipLibrary.getSelectedShip();
                        int numCannons = selectedShip.getGunOffsets().size();

                        int bossHealth;
                        if (numCannons >= 2) {
                            bossHealth = 30;
                        } else {
                            bossHealth = 12;
                        }

                        Vector2D bossPos = new Vector2D(Constants.WIDTH / 2 - 100, 100);
                        MiniBoss boss = new MiniBoss(bossPos, this, bossHealth);
                        addObject(boss);


                        Message bossMsg = new Message(
                                new Vector2D(Constants.WIDTH / 2, Constants.HEIGHT / 2 - 100),
                                false,
                                "MINI JEFE DETECTADO!",
                                Color.RED,
                                true,
                                Assets.fontBig,
                                this
                        );
                        bossMsg.setLifespan(4000);
                        addMessage(bossMsg);
                    }

                }).start();
            }
        }
    }

    // Spawnear sub-oleada de meteoros "poco a poco"
    private void spawnMeteorSubWave(int count) {
        // ... (codigo sin cambios) ...
        Message subWaveMsg = new Message(
                new Vector2D(Constants.WIDTH / 2, Constants.HEIGHT / 2 + 60),
                false,
                "REFUERZOS DETECTADOS!",
                Color.ORANGE,
                true,
                Assets.fontMed,
                this
        );
        subWaveMsg.setLifespan(2500);
        addMessage(subWaveMsg);

        new Thread(() -> {
            try {
                for (int i = 0; i < count; i++) {
                    double x = i % 2 == 0 ? Math.random() * Constants.WIDTH : 0;
                    double y = i % 2 == 0 ? 0 : Math.random() * Constants.HEIGHT;
                    Vector2D velocity = new Vector2D(Math.random() * 2 - 1, Math.random() * 2 - 1).normalize().scale(2);
                    int family = (int) (Math.random() * 3);
                    Meteor meteor = new Meteor(
                            new Vector2D(x, y),
                            velocity,
                            2,
                            Assets.bigs[family],
                            this,
                            Size.BIG,
                            family
                    );
                    addObject(meteor);

                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                nextWaveStarting = false;
            }
        }).start();
    }


    @Override
    public void draw(Graphics g) {
        // ... (codigo de draw (inicio) sin cambios) ...
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        background.draw(g);

        for (Message msg : messages)
            msg.draw(g2d);

        for (MovingObject obj : movingObjects)
            obj.draw(g);

        for (Animation anim : explosion) {
            g2d.drawImage(
                    anim.getCurrentFrame(),
                    (int) anim.getPosition().getX(),
                    (int) anim.getPosition().getY(),
                    null
            );
        }

        g.drawImage(pauseButtonImg, pauseButtonBounds.x, pauseButtonBounds.y, null);

        if (startingCountdown && countdownValue > 0) {
            g.setFont(Assets.fontBig);
            g.setColor(Color.WHITE);
            String text = countdownValue + "!";
            int textWidth = g.getFontMetrics().stringWidth(text);
            g.drawString(text, (Constants.WIDTH - textWidth) / 2, Constants.HEIGHT / 2);
        }

        if (paused && showPauseMenu) {
            g2d.setColor(new Color(0, 0, 0, (int) (255 * pauseOverlayAlpha)));
            g2d.fillRect(0, 0, Constants.WIDTH, Constants.HEIGHT);

            g.drawImage(
                    resumeButtonBounds.contains(MouseInput.getMousePosition()) ? resumeButtonHoverImg : resumeButtonImg,
                    resumeButtonBounds.x, resumeButtonBounds.y,
                    resumeButtonBounds.width, resumeButtonBounds.height, null
            );
            g.setColor(Color.BLACK);
            g.setFont(Assets.fontMed);
            g.drawString("REANUDAR", resumeButtonBounds.x + 120, resumeButtonBounds.y + 38);

            g.drawImage(
                    settingsButtonBounds.contains(MouseInput.getMousePosition()) ? settingsButtonHoverImg : settingsButtonImg,
                    settingsButtonBounds.x, settingsButtonBounds.y,
                    settingsButtonBounds.width, settingsButtonBounds.height, null
            );
            g.drawString("CONFIGURACIONES", settingsButtonBounds.x + 60, settingsButtonBounds.y + 38);

            g.drawImage(
                    menuButtonBounds.contains(MouseInput.getMousePosition()) ? menuButtonHoverImg : menuButtonImg,
                    menuButtonBounds.x, menuButtonBounds.y,
                    menuButtonBounds.width, menuButtonBounds.height, null
            );
            g.drawString("MENU PRINCIPAL", menuButtonBounds.x + 80, menuButtonBounds.y + 38);
        }


        drawScore(g);
        drawLives(g);

        // Dibujo dinamico del HUD (sin cambios)
        if (player != null) {
            int activePowerUps = 0; // Contador para la posicion horizontal

            // 1. Dibujar Escudo (si esta activo)
            if (player.isShielded()) {
                drawPowerUpBar(g,
                        player.getShieldTimeRemaining(),
                        "ESCUDO",
                        Color.CYAN,
                        activePowerUps
                );
                activePowerUps++;
            }

            // 2. Dibujar Disparo Rapido (si esta activo)
            if (player.isRapidFire()) {
                drawPowerUpBar(g,
                        player.getRapidFireTimeRemaining(),
                        "DISPARO RAPIDO",
                        Color.YELLOW,
                        activePowerUps
                );
                activePowerUps++;
            }

            // 3. Dibujar Multi-Disparo (si esta activo)
            if (player.isMultiShot()) {
                drawPowerUpBar(g,
                        player.getMultiShotTimeRemaining(),
                        "MULTI-DISPARO",
                        Color.GREEN,
                        activePowerUps
                );
                activePowerUps++;
            }
        }
    }

    private void drawScore(Graphics g) {
        // ... (codigo sin cambios) ...
        Vector2D pos = new Vector2D(1120, 35);
        String scoreToString = Integer.toString(score);

        for (int i = 0; i < scoreToString.length(); i++) {
            g.drawImage(
                    Assets.numbers[Integer.parseInt(scoreToString.substring(i, i + 1))],
                    (int) pos.getX(),
                    (int) pos.getY(),
                    null
            );
            pos.setX(pos.getX() + 15);
        }
    }

    private void drawLives(Graphics g) {
        // ... (codigo sin cambios) ...
        Vector2D livePosition = new Vector2D(20, 620);
        g.drawImage(Assets.life, (int) livePosition.getX(), (int) livePosition.getY(), null);
        g.drawImage(Assets.numbers[10], (int) livePosition.getX() + 40, (int) livePosition.getY() + 2, null);

        String livesToString = Integer.toString(lives);
        Vector2D pos = new Vector2D(livePosition.getX(), livePosition.getY());
        for (int i = 0; i < livesToString.length(); i++) {
            int number = Integer.parseInt(livesToString.substring(i, i + 1));
            if (number <= 0) break;
            g.drawImage(Assets.numbers[number], (int) pos.getX() + 65, (int) pos.getY() + 2, null);
            pos.setX(pos.getX() + 25);
        }
    }

    /**
     * Dibuja una barra de estado para un power-up en una posicion horizontal dinamica.
     */
    private void drawPowerUpBar(Graphics g, double percent, String title, Color color, int positionIndex) {
        // ... (codigo sin cambios) ...

        int barWidth = 150; // Ancho de la barra
        int barHeight = 15; // Alto
        int spacing = 20;   // Espacio entre barras

        // Posicion Y (fija, en la parte inferior)
        int y = Constants.HEIGHT - barHeight - 40;

        // Posicion X (dinamica, basada en el indice)
        // Se alinea desde la derecha de la pantalla hacia la izquierda
        int x = Constants.WIDTH - (barWidth + 30) - (positionIndex * (barWidth + spacing));

        // Dibujar el texto del titulo
        g.setFont(Assets.fontMed);
        g.setColor(color);
        g.drawString(title, x, y - 5);

        // Dibujar el fondo de la barra
        g.setColor(Color.DARK_GRAY);
        g.fillRect(x, y, barWidth, barHeight);

        // Dibujar la barra de tiempo restante
        g.setColor(color);
        g.fillRect(x, y, (int)(barWidth * percent), barHeight);

        // Dibujar el borde
        g.setColor(Color.WHITE);
        g.drawRect(x, y, barWidth, barHeight);
    }


    // ------------------ Musica ------------------
    public void pauseMusic() {
        // ... (codigo sin cambios) ...
        if (Assets.backgroundMusic != null)
            Assets.backgroundMusic.pause();
    }

    public void resumeMusic() {
        // ... (codigo sin cambios) ...
        if (Assets.backgroundMusic != null && !Assets.backgroundMusic.isPlaying())
            Assets.backgroundMusic.resume();
    }

    public void stopMusic() {
        // ... (codigo sin cambios) ...
        if (Assets.backgroundMusic != null) {
            Assets.backgroundMusic.stop();
        }
    }

    // ------------------ Gestion de objetos y mensajes ------------------
    public Player getPlayer() {
        return player;
    }

    public void subtractLife() {
        // ... (codigo sin cambios) ...
        lives--;
        if (lives <= 0) {
            stopMusic();
            State.changeState(new GameOverState(score, waves));
            Assets.gameOver.play();
        }
    }

    public void addObject(MovingObject obj) {
        objectsToAdd.add(obj);
    }

    public void removeObject(MovingObject obj) {
        objectsToRemove.add(obj);
    }

    public void addMessage(Message msg) {
        messages.add(msg);
    }

    public void removeMessage(Message msg) {
        messagesToRemove.add(msg);
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public ArrayList<MovingObject> getMovingObjects() {
        return movingObjects;
    }

    public Sound getBackgroundMusic() {
        return Assets.backgroundMusic;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
        if (paused)
            pauseMusic();
        else
            resumeMusic();
    }

    public void addScore(int value, Vector2D position) {
        score += value;
        messages.add(new Message(position, true, "+" + value + " puntos", Color.WHITE, false, Assets.fontMed, this));
    }

    public void subtractScore(int value, Vector2D position) {
        score -= value;
        if (score < 0) score = 0; // evitar negativos
        messages.add(new Message(position, true, "-" + value + " puntos", Color.RED, false, Assets.fontMed, this));
    }
}