package gameObject;

public class Chronometer {

    private long startTime;
    private long duration;
    private boolean running;

    public Chronometer() {
        running = false;
    }

    // Inicia el cronómetro con la duración en milisegundos
    public void run(long duration) {
        this.duration = duration;
        startTime = System.currentTimeMillis();
        running = true;
    }

    // Actualiza el cronómetro, opcional según tu lógica
    public void update() {
        if (!running) return;
        if (System.currentTimeMillis() - startTime >= duration) {
            running = false;
        }
    }

    public boolean isRunning() {
        return running;
    }

    // Indica si el cronómetro ya terminó
    public boolean isFinished() {
        return !running;
    }

    // Reinicia el cronómetro
    public void reset() {
        running = false;
    }
}
