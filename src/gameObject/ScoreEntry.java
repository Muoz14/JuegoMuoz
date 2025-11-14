package gameObject;

/**
 * Una clase simple para representar una entrada en la tabla de puntuaciones.
 * Debe ser 'public' y tener un constructor vacío (o campos public)
 * para que el serializador JSON (como Gson) funcione correctamente.
 */
public class ScoreEntry {

    public String name;
    public int score;

    // Constructor vacío (necesario para Gson)
    public ScoreEntry() {
        this.name = "AAAA";
        this.score = 0;
    }

    // Constructor principal
    public ScoreEntry(String name, int score) {
        this.name = name;
        this.score = score;
    }
}