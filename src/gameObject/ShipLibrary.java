package gameObject;

import graphics.Assets;
import math.Vector2D;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ShipLibrary {

    public static List<ShipData> ships = new ArrayList<>();
    private static int selectedShip = 0;
    private static int selectedLaser = 0;

    // Metodo para inicializar naves
    public static void init() {

        ships.clear();

        //NAVE 1
        //Tiro unico
        List<Vector2D> gunOffsets1 = List.of(
                new Vector2D(0, 0) // disparo central
        );
        List<Vector2D> thrusterOffsets1 = List.of(
                new Vector2D(35 - 6, 53 - 10) // propulsor central
        );

        ShipData ship1 = new ShipData(
                "OneShot",
                Assets.player2,
                Constants.PLAYER_MAX_VEL,
                Constants.FIRERATE,
                gunOffsets1,
                thrusterOffsets1,
                Assets.propulsor1,
                Assets.laserPersonalizado1
        );

        //NAVE 2
        //Doble tiro
        double separation = 20; // distancia entre armas
        List<Vector2D> gunOffsets2 = List.of(
                new Vector2D(-separation, 0), // lado izquierdo
                new Vector2D(separation, 0)   // lado derecho
        );

        // Propulsor central
        List<Vector2D> thrusterOffsets2= List.of(
                new Vector2D(64 / 2 - 6, 69 - 10) // centro de la nave (width/2 - CORRECCION_X, height - CORRECCION_Y)
        );

        ShipData ship2 = new ShipData(
                "DoubleShot",
                Assets.player1,
                Constants.PLAYER_MAX_VEL,
                Constants.FIRERATE,
                gunOffsets2,
                thrusterOffsets2,
                Assets.propulsor2,
                Assets.laserPersonalizado2
        );

        ships.add(ship1);
        ships.add(ship2);

        // Puedes añadir más naves aquí con diferentes armas y propulsores
    }

    // Obtener lista de naves
    public static List<ShipData> getShips() {

        return ships;

    }

    // Obtener una nave especifica
    public static ShipData getShip(int index) {

        return ships.get(index);

    }

    // === Métodos de selección ===

    public static void setSelectedShip(int index) {
        if (index >= 0 && index < ships.size()) {
            selectedShip = index;
        }
    }

    public static ShipData getSelectedShip() {
        return ships.get(selectedShip);
    }

    // Opcional: si luego agregas tipos de láser aparte
    public static int getSelectedLaser() {
        return selectedLaser;
    }

    public static void setSelectedLaser(int index) {
        selectedLaser = index;
    }

    public static BufferedImage getSelectedLaserImage() {
        switch (selectedLaser) {
            case 0: return Assets.laserPersonalizado1;
            case 1: return Assets.laserPersonalizado2;
            // ... más tipos si quieres
            default: return Assets.laserPersonalizado1;
        }
    }

}