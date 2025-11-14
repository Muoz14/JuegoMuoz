package gameObject;

public class Chronometer {

    private long startTime;
    private long duration;
    private boolean running;

    private long pauseStartTime;
    private boolean isPaused;

    public Chronometer() {
        running = false;
        isPaused = false; // Añadido
    }

    // Inicia el cronometro con la duracion en milisegundos
    public void run(long duration) {
        this.duration = duration;
        startTime = System.currentTimeMillis();
        running = true;
        isPaused = false; // Añadido
    }

    /**
     * Pausa el cronómetro, registrando el tiempo actual.
     */
    public void pause() {
        if (running && !isPaused) {
            isPaused = true;
            pauseStartTime = System.currentTimeMillis();
        }
    }

    /**
     * Reanuda el cronómetro, ajustando el tiempo de inicio
     * para compensar la duración de la pausa.
     */
    public void resume() {
        if (running && isPaused) {
            isPaused = false;
            // Calcula cuánto tiempo estuvo en pausa
            long pauseDuration = System.currentTimeMillis() - pauseStartTime;
            // Mueve el 'startTime' hacia adelante por esa cantidad
            startTime += pauseDuration;
        }
    }

    // Actualiza el cronometro
    public void update() {
        if (!running || isPaused) return; // <-- MODIFICADO

        if (System.currentTimeMillis() - startTime >= duration) {
            running = false;
        }
    }

    public boolean isRunning() {
        return running;
    }

    // Indica si el cronometro ya termino
    public boolean isFinished() {
        // Añadimos una comprobación por si acaso update() no se ha llamado
        if (running && !isPaused && System.currentTimeMillis() - startTime >= duration) {
            running = false;
        }
        return !running;
    }

    /**
     * Devuelve la duracion total para la que se configuro el cronometro.
     */
    public long getDuration() {
        return duration;
    }

    /**
     * Devuelve cuanto tiempo (en ms) le queda al cronometro.
     */
    public long getTimeRemaining() {
        if (!running) {
            return 0;
        }

        if (isPaused) {
            // Si está en pausa, calcula el tiempo transcurrido hasta el momento de la pausa
            long elapsedOnPause = pauseStartTime - startTime;
            return Math.max(0, duration - elapsedOnPause);
        }

        long elapsed = System.currentTimeMillis() - startTime;
        return Math.max(0, duration - elapsed);
    }


    // Reinicia el cronometro
    public void reset() {
        running = false;
        isPaused = false; // Añadido
    }
}