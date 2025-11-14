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

    private int score = 0;

    // Control de oleadas
    private boolean waveCleared = false;
    private long waveClearTime = 0;
    private boolean nextWaveStarting = false;

    private RaiderSquadManager squadManager = null;
    private FinalBoss finalBoss = null;

    private boolean paused = false;
    private boolean startingCountdown = true;
    private long countdownStartTime;
    private int countdownValue = 3;
    private boolean firstWaveStarted = false;
    private boolean escPressedLastFrame = false;

    // Aparicion del UFO
    private long lastUfoSpawnTime = 0;
    private static final long UFO_SPAWN_INTERVAL = 15000;

    // --- SPAWN DE POWER-UPS ---
    private static final long POWERUP_BURST_INTERVAL = 15000;
    private static final long POWERUP_CHAIN_DELAY = 1500;
    private static final double POWERUP_THIRD_CHANCE = 0.40;
    private static final int MAX_POWERUPS_ON_SCREEN = 3;

    private Chronometer burstSpawnTimer;
    private Chronometer burstChainTimer;
    private int burstChainStep = 0;

    // Botones de pausa
    private BufferedImage pauseButtonImg;
    private Rectangle pauseButtonBounds;
    private int pauseButtonOffsetX = 20;
    private int pauseButtonOffsetY = 20;
    private Rectangle resumeButtonBounds, settingsButtonBounds, menuButtonBounds;
    private BufferedImage resumeButtonImg, settingsButtonImg, menuButtonImg;
    private boolean showPauseMenu = false;
    private float pauseOverlayAlpha = 0.5f;
    private BufferedImage resumeButtonHoverImg, settingsButtonHoverImg, menuButtonHoverImg;

    public GameState() {

        ShipData playerData = ShipLibrary.getSelectedShip();
        BufferedImage laserImg = ShipLibrary.getSelectedLaserImage();

        player = new Player(new Vector2D(560, 320), new Vector2D(), this, playerData, laserImg);
        movingObjects.add(player);

        background = new Background();
        meteors = 1;

        burstSpawnTimer = new Chronometer();
        burstSpawnTimer.run(POWERUP_BURST_INTERVAL);
        burstChainTimer = new Chronometer();

        // Boton pausa
        pauseButtonImg = Assets.buttonPause;
        int buttonWidth = pauseButtonImg.getWidth();
        int buttonHeight = pauseButtonImg.getHeight();
        pauseButtonBounds = new Rectangle(pauseButtonOffsetX, pauseButtonOffsetY, buttonWidth, buttonHeight);

        // Botones menu de pausa
        resumeButtonImg = Assets.buttonS1;
        settingsButtonImg = Assets.buttonS1;
        menuButtonImg = Assets.buttonS1;
        resumeButtonHoverImg = Assets.buttonS2;
        settingsButtonHoverImg = Assets.buttonS2;
        menuButtonHoverImg = Assets.buttonS2;

        resumeButtonBounds = new Rectangle(Constants.WIDTH / 2 - 180, Constants.HEIGHT / 2 - 100, 360, 60);
        settingsButtonBounds = new Rectangle(Constants.WIDTH / 2 - 180, Constants.HEIGHT / 2, 360, 60);
        menuButtonBounds = new Rectangle(Constants.WIDTH / 2 - 180, Constants.HEIGHT / 2 + 100, 360, 60);

        startCountdown();

        // Musica de fondo
        if (Assets.backgroundMusic != null) {
            Assets.backgroundMusic.stop();
        }
        Assets.backgroundMusic.setVolume(SettingsData.getVolume());
        Assets.backgroundMusic.play();
    }

    // ------------------ Countdown ------------------
    public void startCountdown() {
        startingCountdown = true;
        countdownStartTime = System.currentTimeMillis();
        countdownValue = 3;
    }

    private void handleCountdown() {
        long elapsed = System.currentTimeMillis() - countdownStartTime;
        if (elapsed >= 1000) {
            countdownValue--;
            countdownStartTime = System.currentTimeMillis();
        }

        if (countdownValue <= 0) {
            startingCountdown = false;

            // Reanuda TODOS los timers del juego
            player.resumeTimers();
            if (squadManager != null) squadManager.resumeTimers();
            if (finalBoss != null) finalBoss.resumeTimers();
            // Reanudar timers de esbirros y OVNIs
            for(MovingObject m : movingObjects) {
                if (m instanceof Minion) ((Minion)m).resumeTimers();
                if (m instanceof Ufo) ((Ufo)m).resumeTimers();
            }

            if (!firstWaveStarted) {
                startWave();
                firstWaveStarted = true;
            }
        }
    }

    private void startWave() {
        Message waveMessage = new Message(new Vector2D(Constants.WIDTH / 2, Constants.HEIGHT / 2), false, "OLEADA " + waves, Color.WHITE, true, Assets.fontBig, this);
        waveMessage.setLifespan(3500);
        addMessage(waveMessage);

        for (int i = 0; i < meteors; i++) {
            double x = i % 2 == 0 ? Math.random() * Constants.WIDTH : 0;
            double y = i % 2 == 0 ? 0 : Math.random() * Constants.HEIGHT;
            Vector2D velocity = new Vector2D(Math.random() * 2 - 1, Math.random() * 2 - 1).normalize().scale(2);
            int family = (int) (Math.random() * 3);
            Meteor meteor = new Meteor(new Vector2D(x, y), velocity, 2, Assets.bigs[family], this, Size.BIG, family);
            addObject(meteor);
        }
    }
    public void playExplosion(Vector2D position) {
        explosion.add(new Animation(Assets.exp, 110, position.subtract(new Vector2D(Assets.exp[0].getWidth() / 2, Assets.exp[0].getHeight() / 2))));
    }

    private void spawnUfo() {
        long now = System.currentTimeMillis();
        if (now - lastUfoSpawnTime < UFO_SPAWN_INTERVAL) return;
        lastUfoSpawnTime = now;
        double x = 0, y = 0;
        int side = (int) (Math.random() * 4);
        switch (side) {
            case 0: x = Math.random() * Constants.WIDTH; y = 0; break;
            case 1: x = Math.random() * Constants.WIDTH; y = Constants.HEIGHT; break;
            case 2: x = 0; y = Math.random() * Constants.HEIGHT; break;
            case 3: x = Constants.WIDTH; y = Math.random() * Constants.HEIGHT; break;
        }
        ArrayList<Vector2D> path = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            double posX = Math.random() * Constants.WIDTH;
            double posY = Math.random() * Constants.HEIGHT;
            path.add(new Vector2D(posX, posY));
        }
        Ufo ufo = new Ufo(new Vector2D(x, y), new Vector2D(), Constants.UFO_MAX_VEL, Assets.ufo, path, this);
        addObject(ufo);
    }

    // --- METODOS PARA POWER-UPS ---

    private void updatePowerUpSpawns() {
        burstSpawnTimer.update();
        if (burstSpawnTimer.isFinished() && burstChainStep == 0) {
            if (trySpawnOnePowerUp()) {
                burstChainStep = 1;
                burstChainTimer.run(POWERUP_CHAIN_DELAY);
            }
            burstSpawnTimer.run(POWERUP_BURST_INTERVAL);
        }

        if (burstChainStep > 0) {
            burstChainTimer.update();
            if (burstChainTimer.isFinished()) {
                if (burstChainStep == 1) {
                    if (trySpawnOnePowerUp()) {
                        if (Math.random() < POWERUP_THIRD_CHANCE) {
                            burstChainStep = 2;
                            burstChainTimer.run(POWERUP_CHAIN_DELAY);
                        } else {
                            burstChainStep = 0;
                        }
                    } else {
                        burstChainStep = 0;
                    }
                } else if (burstChainStep == 2) {
                    trySpawnOnePowerUp();
                    burstChainStep = 0;
                }
            }
        }
    }

    private boolean trySpawnOnePowerUp() {
        int currentPowerUps = 0;
        for (MovingObject obj : movingObjects) {
            if (obj instanceof PowerUp) currentPowerUps++;
        }
        if (currentPowerUps >= MAX_POWERUPS_ON_SCREEN) {
            return false;
        }
        PowerUpType type = selectPowerUpType();

        double x, y;

        if (finalBoss != null) {
            // Jefe Final está activo, spawnear en la zona segura
            BossPhase phase = finalBoss.getPhase();

            // Si el jefe está arriba (Fase 1) o en transición 1/2
            if (phase == BossPhase.PHASE_1 || phase == BossPhase.TRANSITION_1 || phase == BossPhase.TRANSITION_2) {
                // Spawnear en la MITAD INFERIOR de la pantalla
                x = Math.random() * (Constants.WIDTH - 100) + 50;
                y = Math.random() * (Constants.HEIGHT / 2 - 100) + (Constants.HEIGHT / 2); // ej. 360 a 620
            } else {
                // Si el jefe está abajo (Fase 2) o en transición 3
                // Spawnear en la MITAD SUPERIOR de la pantalla
                x = Math.random() * (Constants.WIDTH - 100) + 50;
                y = Math.random() * (Constants.HEIGHT / 2 - 150) + 50; // ej. 50 a 210
            }

        } else {
            // Lógica original: spawnear en cualquier lugar
            x = Math.random() * (Constants.WIDTH - 100) + 50;
            y = Math.random() * (Constants.HEIGHT - 100) + 50;
        }

        Vector2D position = new Vector2D(x, y);
        PowerUp powerUp = new PowerUp(position, type, this);
        addObject(powerUp);
        return true;
    }

    /**
     * Decide que tipo de power-up spawnear
     */
    private PowerUpType selectPowerUpType() {
        double rand = Math.random();

        // 5% Oro (0.0 -> 0.05)
        if (rand < 0.05) {
            return PowerUpType.GOLD;
        }
        // 10% Plata (0.05 -> 0.15)
        else if (rand < 0.15) {
            return PowerUpType.SILVER;
        }
        // 10% Vida Extra (0.15 -> 0.25)
        else if (rand < 0.25) {
            return PowerUpType.EXTRA_LIFE;
        }
        // 10% Aleatorio (0.25 -> 0.35)
        else if (rand < 0.35) {
            return PowerUpType.RANDOM_POWER;
        }
        // 15% Puntos x2 (0.35 -> 0.50)
        else if (rand < 0.50) {
            return PowerUpType.DOUBLE_POINTS;
        }
        // 10% Multi-Disparo (0.50 -> 0.60)
        else if (rand < 0.60) {
            return PowerUpType.MULTI_SHOT;
        }
        // 15% Disparo Rapido (0.60 -> 0.75)
        else if (rand < 0.75) {
            return PowerUpType.RAPID_FIRE;
        }
        // 25% Bronce (0.75 -> 1.0)
        else {
            return PowerUpType.BRONZE;
        }
    }

    @Override
    public void update() {
        MouseInput.update();
        Point mouse = MouseInput.getMousePosition();

        // Actualizar explosiones
        for (int i = explosion.size() - 1; i >= 0; i--) {
            Animation anim = explosion.get(i);
            anim.update();
            if (!anim.isRunning()) explosion.remove(i);
        }

        // LÓGICA DE PAUSA MEJORADA
        if (pauseButtonBounds.contains(mouse) && MouseInput.isPressed()) {
            pauseGame();
            MouseInput.releaseClick();
        }
        boolean escNow = KeyBoard.isKeyPressed(KeyEvent.VK_ESCAPE);
        if (escNow && !escPressedLastFrame) {
            if (paused) {
                resumeGame();
            } else {
                pauseGame();
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
            if (i < movingObjects.size()) {
                movingObjects.get(i).update();
            }
        }

        if (squadManager != null) {
            squadManager.update();
        }

        if (finalBoss != null) {
            finalBoss.update();
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
        movingObjects.addAll(objectsToAdd);
        objectsToAdd.clear();
        movingObjects.removeAll(objectsToRemove);
        objectsToRemove.clear();
        messages.removeAll(messagesToRemove);
        messagesToRemove.clear();

        spawnUfo();
        updatePowerUpSpawns();
        handleWaveLogic();
    }

    private void pauseGame() {
        paused = true;
        showPauseMenu = true;
        pauseMusic();
        SoundManager.getInstance().pauseAll();

        // Pausar todo lo que tenga timers
        player.pauseTimers();
        if (squadManager != null) squadManager.pauseTimers();
        if (finalBoss != null) finalBoss.pauseTimers();

        for(MovingObject m : movingObjects) {
            if (m instanceof Minion) ((Minion)m).pauseTimers();
            if (m instanceof Ufo) ((Ufo)m).pauseTimers();
        }
    }

    private void resumeGame() {
        paused = false;
        showPauseMenu = false;
        startCountdown(); // Inicia el contador (que luego reanuda los timers)
        resumeMusic();
        SoundManager.getInstance().resumeAll();
    }

    private void handlePauseMenu(Point mouse) {
        if (resumeButtonBounds.contains(mouse) && MouseInput.isPressed()) {
            resumeGame();
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

        // 1. Contar enemigos activos
        boolean hayMeteoros = false, hayMiniBoss = false;
        boolean hayRaiderEvent = (squadManager != null);
        boolean hayFinalBoss = (finalBoss != null);

        int meteorCount = 0;

        for (MovingObject obj : movingObjects) {
            if (obj instanceof Meteor) {
                hayMeteoros = true;
                meteorCount++;
            }
            if (obj instanceof MiniBoss) hayMiniBoss = true;
        }

        // 2. Lógica de reabastecimiento de meteoros
        if (hayFinalBoss) {
            // Para el Jefe Final, mantener solo 1 meteoro
            if (meteorCount < 1 && !nextWaveStarting) {
                nextWaveStarting = true;
                spawnMeteorSubWave(1);
            }
        } else if (hayRaiderEvent || hayMiniBoss) {
            // Para Mini-Boss y Raiders, mantener hasta 3 meteoros
            if (meteorCount < 3 && !nextWaveStarting) {
                nextWaveStarting = true;
                spawnMeteorSubWave(1);
            }
        }

        // 3. Lógica de Oleada Completada
        if (!hayMeteoros && !hayMiniBoss && !hayRaiderEvent && !hayFinalBoss && firstWaveStarted && !waveCleared && !nextWaveStarting) {
            waveCleared = true;
            waveClearTime = System.currentTimeMillis();
            Message completeMsg = new Message(new Vector2D(Constants.WIDTH / 2, Constants.HEIGHT / 2), false, "OLEADA COMPLETADA!", Color.WHITE, true, Assets.fontBig, this);
            completeMsg.setLifespan(3000);
            addMessage(completeMsg);
        }

        // 4. Lógica para iniciar la siguiente oleada
        if (waveCleared && !nextWaveStarting) {
            long elapsed = System.currentTimeMillis() - waveClearTime;
            if (elapsed >= 3000) {
                waveCleared = false;
                nextWaveStarting = true;
                waves++;

                // --- INICIO DE LA MODIFICACIÓN (METEOR CAP) ---
                if (waves <= 8) {
                    meteors++; // Aumenta hasta 8
                }
                // Si waves > 8, 'meteors' se queda en 8
                // --- FIN DE LA MODIFICACIÓN ---

                Message waveMessage = new Message(new Vector2D(Constants.WIDTH / 2, Constants.HEIGHT / 2), false, "OLEADA " + waves + "!", Color.WHITE, true, Assets.fontBig, this);
                waveMessage.setLifespan(3000);
                addMessage(waveMessage);

                new Thread(() -> {
                    try { Thread.sleep(3000); } catch (InterruptedException ignored) {}

                    // --- 1. SPAWN JEFE FINAL (Cada 10 oleadas) ---
                    if (waves > 0 && waves % 10 == 0) {
                        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

                        // Guardar el contador real de meteoros
                        int oldMeteorCount = meteors;
                        meteors = 1; // Poner 1 meteoro para la oleada del jefe
                        startWave();
                        meteors = oldMeteorCount; // Restaurar para la proxima oleada

                        nextWaveStarting = false;

                        double spawnX = (Constants.WIDTH / 2.0 - Assets.finalBoss.getWidth() / 2.0) + 100;
                        Vector2D bossPos = new Vector2D(spawnX, -Assets.finalBoss.getHeight());

                        finalBoss = new FinalBoss(bossPos, this);
                        addObject(finalBoss);

                        // --- 2. SPAWN MINI-BOSS (Cada 4 oleadas, si NO es oleada de Jefe Final) ---
                    } else if (waves > 0 && waves % 4 == 0) {
                        startWave();
                        nextWaveStarting = false;

                        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                        ShipData selectedShip = ShipLibrary.getSelectedShip();
                        int numCannons = selectedShip.getGunOffsets().size();
                        int bossHealth = (numCannons >= 2) ? 30 : 12;
                        Vector2D bossPos = new Vector2D(Constants.WIDTH / 2 - 100, 100);
                        MiniBoss boss = new MiniBoss(bossPos, this, bossHealth);
                        addObject(boss);
                        Message bossMsg = new Message(new Vector2D(Constants.WIDTH / 2, Constants.HEIGHT / 2 - 100), false, "MINI JEFE DETECTADO!", Color.RED, true, Assets.fontBig, this);
                        bossMsg.setLifespan(4000);
                        addMessage(bossMsg);

                        // --- 3. SPAWN RAIDER SQUAD (Cada 3 oleadas, si NO es oleada de Jefe o MiniJefe) ---
                    } else if (waves > 1 && waves % 3 == 0) {
                        startWave();
                        nextWaveStarting = false;

                        try { Thread.sleep(4000); } catch (InterruptedException ignored) {}
                        squadManager = new RaiderSquadManager(this);

                        // --- 4. OLEADA NORMAL ---
                    } else {
                        startWave();
                        nextWaveStarting = false;
                    }
                    // --- FIN DE LA MODIFICACIÓN ---

                }).start();
            }
        }
    }

    private void spawnMeteorSubWave(int count) {
        Message subWaveMsg = new Message(new Vector2D(Constants.WIDTH / 2, Constants.HEIGHT / 2 + 60), false, "REFUERZOS DETECTADOS!", Color.ORANGE, true, Assets.fontMed, this);
        subWaveMsg.setLifespan(2500);
        addMessage(subWaveMsg);
        new Thread(() -> {
            try {
                for (int i = 0; i < count; i++) {
                    double x = i % 2 == 0 ? Math.random() * Constants.WIDTH : 0;
                    double y = i % 2 == 0 ? 0 : Math.random() * Constants.HEIGHT;
                    Vector2D velocity = new Vector2D(Math.random() * 2 - 1, Math.random() * 2 - 1).normalize().scale(2);
                    int family = (int) (Math.random() * 3);
                    Meteor meteor = new Meteor(new Vector2D(x, y), velocity, 2, Assets.bigs[family], this, Size.BIG, family);
                    addObject(meteor);
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                nextWaveStarting = false; // Permitir que la lógica se ejecute de nuevo
            }
        }).start();
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        background.draw(g);
        for (Message msg : messages) msg.draw(g2d);
        for (MovingObject obj : movingObjects) obj.draw(g);
        for (Animation anim : explosion) {
            g2d.drawImage(anim.getCurrentFrame(), (int) anim.getPosition().getX(), (int) anim.getPosition().getY(), null);
        }
        g.drawImage(pauseButtonImg, pauseButtonBounds.x, pauseButtonBounds.y, null);

        // 1. Dibujar el HUD (Score, Vidas, Barras de Power-up) PRIMERO
        drawScore(g);
        drawLives(g);

        if (player != null) {
            int activePowerUps = 0;
            if (player.isShielded()) {
                drawPowerUpBar(g, player.getShieldTimeRemaining(), "ESCUDO", Color.CYAN, activePowerUps++);
            }
            if (player.isRapidFire()) {
                drawPowerUpBar(g, player.getRapidFireTimeRemaining(), "DISPARO RAPIDO", Color.YELLOW, activePowerUps++);
            }
            if (player.isMultiShot()) {
                drawPowerUpBar(g, player.getMultiShotTimeRemaining(), "MULTI-DISPARO", Color.GREEN, activePowerUps++);
            }
            if (player.isScoreMultiplier()) {
                drawPowerUpBar(g, player.getScoreMultiplierTimeRemaining(), "PUNTOS X2", new Color(255, 175, 0), activePowerUps++);
            }
        }

        // 2. Dibujar el Countdown (si está activo)
        if (startingCountdown && countdownValue > 0) {
            g.setFont(Assets.fontBig);
            g.setColor(Color.WHITE);
            String text = countdownValue + "!";
            int textWidth = g.getFontMetrics().stringWidth(text);
            g.drawString(text, (Constants.WIDTH - textWidth) / 2, Constants.HEIGHT / 2);
        }

        // 3. Dibujar el menú de pausa (si está activo) ÚLTIMO
        if (paused && showPauseMenu) {
            g2d.setColor(new Color(0, 0, 0, (int) (255 * pauseOverlayAlpha)));
            g2d.fillRect(0, 0, Constants.WIDTH, Constants.HEIGHT);
            g.drawImage(resumeButtonBounds.contains(MouseInput.getMousePosition()) ? resumeButtonHoverImg : resumeButtonImg, resumeButtonBounds.x, resumeButtonBounds.y, resumeButtonBounds.width, resumeButtonBounds.height, null);
            g.setColor(Color.BLACK);
            g.setFont(Assets.fontMed);
            g.drawString("REANUDAR", resumeButtonBounds.x + 120, resumeButtonBounds.y + 38);
            g.drawImage(settingsButtonBounds.contains(MouseInput.getMousePosition()) ? settingsButtonHoverImg : settingsButtonImg, settingsButtonBounds.x, settingsButtonBounds.y, settingsButtonBounds.width, settingsButtonBounds.height, null);
            g.drawString("CONFIGURACIONES", settingsButtonBounds.x + 60, settingsButtonBounds.y + 38);
            g.drawImage(menuButtonBounds.contains(MouseInput.getMousePosition()) ? menuButtonHoverImg : menuButtonImg, menuButtonBounds.x, menuButtonBounds.y, menuButtonBounds.width, menuButtonBounds.height, null);
            g.drawString("MENU PRINCIPAL", menuButtonBounds.x + 80, menuButtonBounds.y + 38);
        }
    }

    private void drawScore(Graphics g) {
        Vector2D pos = new Vector2D(1120, 35);
        String scoreToString = Integer.toString(score);
        for (int i = 0; i < scoreToString.length(); i++) {
            g.drawImage(Assets.numbers[Integer.parseInt(scoreToString.substring(i, i + 1))], (int) pos.getX(), (int) pos.getY(), null);
            pos.setX(pos.getX() + 15);
        }
    }

    private void drawLives(Graphics g) {
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

    private void drawPowerUpBar(Graphics g, double percent, String title, Color color, int positionIndex) {
        int barWidth = 150, barHeight = 15, spacing = 20;
        int y = Constants.HEIGHT - barHeight - 40;
        int x = Constants.WIDTH - (barWidth + 30) - (positionIndex * (barWidth + spacing));
        g.setFont(Assets.fontMed);
        g.setColor(color);
        g.drawString(title, x, y - 5);
        g.setColor(Color.DARK_GRAY);
        g.fillRect(x, y, barWidth, barHeight);
        g.setColor(color);
        g.fillRect(x, y, (int)(barWidth * percent), barHeight);
        g.setColor(Color.WHITE);
        g.drawRect(x, y, barWidth, barHeight);
    }

    // ------------------ Musica ------------------
    public void pauseMusic() { if (Assets.backgroundMusic != null) Assets.backgroundMusic.pause(); }
    public void resumeMusic() { if (Assets.backgroundMusic != null && !Assets.backgroundMusic.isPlaying()) Assets.backgroundMusic.resume(); }
    public void stopMusic() { if (Assets.backgroundMusic != null) Assets.backgroundMusic.stop(); }

    // ------------------ Gestion de objetos y mensajes ------------------
    public Player getPlayer() { return player; }

    public void addLife() {
        lives++;
        messages.add(new Message(player.getCenter(), true, "+1 VIDA", Color.GREEN, false, Assets.fontMed, this));
    }

    public void subtractLife() {
        lives--;
        if (lives <= 0) {

            // 1. Obtener el nombre del jugador
            String playerName = PlayerData.getCurrentPlayerName();

            // 2. Añadir la puntuación al ScoreManager
            ScoreManager.addScore(playerName, score);

            stopMusic();
            State.changeState(new GameOverState(score, waves));
            Assets.gameOver.play();
        }
    }

    public void addObject(MovingObject obj) { objectsToAdd.add(obj); }
    public void removeObject(MovingObject obj) { objectsToRemove.add(obj); }
    public void addMessage(Message msg) { messages.add(msg); }
    public void removeMessage(Message msg) { messagesToRemove.add(msg); }
    public ArrayList<Message> getMessages() { return messages; }
    public ArrayList<MovingObject> getMovingObjects() { return movingObjects; }
    public Sound getBackgroundMusic() { return Assets.backgroundMusic; }
    public boolean isPaused() { return paused; }
    public void setPaused(boolean paused) {
        this.paused = paused;
        if (paused) pauseMusic();
        else resumeMusic();
    }

    /**
     * Callback para que el RaiderSquadManager se "destruya" a sí mismo.
     */
    public void onRaiderAssaultFinished() {
        this.squadManager = null;
    }

    /**
     * Callback para que el FinalBoss se "destruya" a sí mismo.
     */
    public void onFinalBossDefeated() {
        this.finalBoss = null;
    }

    /**
     * Devuelve una posición segura para que el jugador reaparezca.
     * @return Vector2D con la posición de reaparición.
     */
    public Vector2D requestPlayerRespawnPosition() {
        // Si el jefe final está activo
        if (finalBoss != null) {
            BossPhase phase = finalBoss.getPhase();

            // Si el jefe está en Fase 2 (abajo) o en la transición 3 (subiendo)
            if (phase == BossPhase.PHASE_2 || phase == BossPhase.TRANSITION_3) {
                // Reaparecer ARRIBA
                return new Vector2D(Constants.WIDTH / 2, 200);
            } else {
                // Reaparecer ABAJO (para Fase 1, Transición 1 y 2)
                return new Vector2D(Constants.WIDTH / 2, 500);
            }
        }

        // Posición por defecto si no hay jefe
        return new Vector2D(Constants.WIDTH / 2, 320);
    }

    public void addScore(int value, Vector2D position) {
        String messageText;
        Color messageColor = Color.WHITE;
        if (player != null && player.isScoreMultiplier()) {
            value *= 2;
            messageText = "+" + value + " (X2!)";
            messageColor = Color.YELLOW;
        } else {
            messageText = "+" + value + " puntos";
        }
        score += value;
        messages.add(new Message(position, true, messageText, messageColor, false, Assets.fontMed, this));
    }

    public void subtractScore(int value, Vector2D position) {
        score -= value;
        if (score < 0) score = 0;
        messages.add(new Message(position, true, "-" + value + " puntos", Color.RED, false, Assets.fontMed, this));
    }
}