package gameObject;

public class Chronometer {

    private long startTime;
    private long duration;
    private boolean running;

    public Chronometer() {
        running = false;
    }

    // Inicia el cronometro con la duracion en milisegundos
    public void run(long duration) {
        this.duration = duration;
        startTime = System.currentTimeMillis();
        running = true;
    }

    // Actualiza el cronometro, opcional segun tu logica
    public void update() {
        if (!running) return;
        if (System.currentTimeMillis() - startTime >= duration) {
            running = false;
        }
    }

    public boolean isRunning() {
        return running;
    }

    // Indica si el cronometro ya termino
    public boolean isFinished() {
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
        long elapsed = System.currentTimeMillis() - startTime;
        return Math.max(0, duration - elapsed);
    }


    // Reinicia el cronometro
    public void reset() {
        running = false;
    }
}