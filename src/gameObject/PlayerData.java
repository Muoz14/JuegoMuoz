package gameObject;

/**
 * Clase estática simple para mantener el nombre del jugador actual
 * entre el estado de NameInput y el GameState.
 */
public class PlayerData {

    // Nombre por defecto si el jugador no ingresa nada (aunque lo forzaremos)
    private static String currentPlayerName = "PLAYER";

    public static String getCurrentPlayerName() {
        return currentPlayerName;
    }

    public static void setCurrentPlayerName(String name) {
        // Evitar nombres vacíos
        if (name == null || name.trim().isEmpty()) {
            currentPlayerName = "PLAYER";
        } else {
            currentPlayerName = name;
        }
    }
}