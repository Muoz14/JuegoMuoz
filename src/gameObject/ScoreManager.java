package gameObject;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class ScoreManager {

    private static final String SCORE_FILE = "scores.json";
    private static ArrayList<ScoreEntry> scores = new ArrayList<>();

    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Añade una nueva puntuación.
     * Si el nombre ya existe, solo actualiza si la nueva puntuación es MÁS ALTA.
     */
    public static void addScore(String name, int score) {

        // 1. Creamos una variable con el nombre procesado.
        final String finalName;
        if (name == null || name.trim().isEmpty()) {
            finalName = "PLAYER";
        } else {
            finalName = name;
        }


        loadScores(); // Asegurarse de tener la lista más reciente

        boolean wasSaved = false; // Bandera para saber si debemos guardar

        // 2. Usamos 'finalName' (que no se modifica) en la lambda.
        Optional<ScoreEntry> existingEntry = scores.stream()
                .filter(s -> s.name.equalsIgnoreCase(finalName))
                .findFirst();

        if (existingEntry.isPresent()) {
            // El jugador SI existe
            ScoreEntry entry = existingEntry.get();
            if (score > entry.score) {
                // La nueva puntuación es MEJOR, actualizar
                entry.score = score;
                System.out.println("Puntuación actualizada para " + finalName);
                wasSaved = true;
            } else {
                // La nueva puntuación es MÁS BAJA o igual, no hacer nada
                System.out.println("Puntuación más baja para " + finalName + " ignorada.");
                wasSaved = false;
            }
        } else {
            // El jugador es NUEVO, añadirlo (usando finalName)
            scores.add(new ScoreEntry(finalName, score));
            System.out.println("Nueva puntuación añadida para " + finalName);
            wasSaved = true;
        }

        // Solo guardar en el archivo si realmente hubo un cambio
        if (wasSaved) {
            sortScores();
            saveScores(); // Guardar cambios
        }
    }

    /**
     * Ordena la lista de puntuaciones de mayor a menor.
     */
    private static void sortScores() {
        scores.sort(Comparator.comparingInt((ScoreEntry s) -> s.score).reversed());
    }

    /**
     * Devuelve una lista de las N mejores puntuaciones.
     * @param limit El número de puntuaciones a devolver.
     */
    public static List<ScoreEntry> getHighScores(int limit) {
        loadScores(); // Cargar siempre antes de mostrar
        sortScores();

        return scores.subList(0, Math.min(scores.size(), limit));
    }

    /**
     * Guarda la lista actual de puntuaciones en el archivo scores.json.
     */
    public static void saveScores() {
        sortScores();

        try (Writer writer = new FileWriter(SCORE_FILE)) {
            gson.toJson(scores, writer);
            System.out.println("Puntuaciones guardadas en " + SCORE_FILE);
        } catch (IOException e) {
            System.err.println("Error al guardar puntuaciones: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Carga las puntuaciones desde el archivo scores.json.
     */
    public static void loadScores() {
        try (Reader reader = new FileReader(SCORE_FILE)) {

            Type listType = new TypeToken<ArrayList<ScoreEntry>>(){}.getType();
            scores = gson.fromJson(reader, listType);

            if (scores == null) {
                scores = new ArrayList<>();
            }

            System.out.println("Puntuaciones cargadas.");

        } catch (FileNotFoundException e) {
            System.out.println(SCORE_FILE + " no encontrado. Se creará uno nuevo.");
            scores = new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Error al cargar puntuaciones: " + e.getMessage());
            e.printStackTrace();
            scores = new ArrayList<>();
        }
    }
}