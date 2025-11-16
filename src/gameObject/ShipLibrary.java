package gameObject;

import graphics.Assets;
import math.Vector2D;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase estatica que inicializa y almacena todas las plantillas (ShipData)
 * de las naves disponibles en el juego.
 */
public class ShipLibrary {

    public static List<ShipData> ships = new ArrayList<>();
    private static int selectedShip = 0;
    // --- INICIO DE LA MODIFICACION ---
    private static int selectedLaser = 0; // 0: LaserRed02, 1: Laser01-Pers, 2: Bolt, 3: Spark, 4: Pulse
    // --- FIN DE LA MODIFICACION ---

    /**
     * Carga todas las naves en la memoria.
     */
    public static void init() {

        ships.clear();

        // =================================================================
        // NAVE 1 (playerShip4_Muoz, "OneShot")
        // Imagen: Assets.player2
        // =================================================================

        // Base: 1 disparo central
        List<Vector2D> gunOffsets1 = List.of(
                new Vector2D(0, 0)
        );

        // Mejorado: 2 disparos laterales
        List<Vector2D> upgradedGunOffsets1 = List.of(
                new Vector2D(-25, 0), // (X, Y) Disparo Izquierdo
                new Vector2D(25, 0)   // (X, Y) Disparo Derecho
        );

        // Propulsor
        List<Vector2D> thrusterOffsets1 = List.of(
                new Vector2D(35 - 6, 53 - 10) // propulsor central
        );

        ShipData ship1 = new ShipData(
                "OneShot",
                Assets.player2, // Textura de la nave 1
                Constants.PLAYER_MAX_VEL,
                Constants.FIRERATE,
                gunOffsets1,
                upgradedGunOffsets1, // Lista mejorada
                thrusterOffsets1,
                Assets.propulsor1,
                Assets.laserPersonalizado1
        );

        // =================================================================
        // NAVE 2 (playerShip1_Muoz, "DoubleShot")
        // Imagen: Assets.player1
        // =================================================================

        double separation = 20; // Distancia entre canones base

        // Base: 2 disparos laterales
        List<Vector2D> gunOffsets2 = List.of(
                new Vector2D(-separation, 0),
                new Vector2D(separation, 0)
        );

        // Mejorado: 3 disparos (laterales + 1 central por la "boca")
        List<Vector2D> upgradedGunOffsets2 = List.of(
                new Vector2D(-separation, 0), // (X, Y) Disparo Izquierdo
                new Vector2D(separation, 0),  // (X, Y) Disparo Derecho
                new Vector2D(0, -10)          // (X, Y) Disparo Central
        );

        // Propulsor
        List<Vector2D> thrusterOffsets2= List.of(
                new Vector2D(64 / 2 - 6, 69 - 10) // centro de la nave
        );

        ShipData ship2 = new ShipData(
                "DoubleShot",
                Assets.player1, // Textura de la nave 2
                Constants.PLAYER_MAX_VEL,
                Constants.FIRERATE,
                gunOffsets2,
                upgradedGunOffsets2, // Lista mejorada
                thrusterOffsets2,
                Assets.propulsor2,
                Assets.laserPersonalizado2
        );

        // =================================================================
        // NAVE 3 (playerShip2_Carlos, "Nova")
        // Imagen: Assets.player3 (66x65)
        // =================================================================

        // Base: 2 disparos (Ajusta el valor X para separarlos)
        List<Vector2D> gunOffsets3 = List.of(
                new Vector2D(-20, 0), // Canon izquierdo (X negativo)
                new Vector2D(20, 0)   // Canon derecho (X positivo)
        );

        // Mejorado: 3 disparos (los 2 base + 1 en la "boca")
        List<Vector2D> upgradedGunOffsets3 = List.of(
                new Vector2D(-20, 0), // Canon izquierdo
                new Vector2D(20, 0),  // Canon derecho
                new Vector2D(0, -10)  // Disparo de la "boca" (Y negativo = adelante)
        );

        // Propulsor (Nave 66x65, Propulsor 50x50)
        // (X: 66/2 - 6 = 27)
        // (Y: 65 - 10 = 55)
        List<Vector2D> thrusterOffsets3 = List.of(
                // Ajusta estos valores X, Y para centrar el propulsor
                new Vector2D(8, 40)
        );

        ShipData ship3 = new ShipData(
                "Nova", // Puedes cambiarle el nombre aqui
                Assets.player3, // Textura de la nave 3
                Constants.PLAYER_MAX_VEL,
                Constants.FIRERATE,
                gunOffsets3,
                upgradedGunOffsets3, // Lista mejorada
                thrusterOffsets3,
                Assets.propulsor3, // Nuevo propulsor
                Assets.laserPersonalizado1 // Puedes cambiarlo a laserPersonalizado2 si quieres
        );


        // --- Anadir naves a la lista ---
        ships.add(ship1);
        ships.add(ship2);
        ships.add(ship3);
    }

    // === Metodos de seleccion ===

    public static List<ShipData> getShips() {
        return ships;
    }

    public static ShipData getShip(int index) {
        return ships.get(index);
    }

    public static void setSelectedShip(int index) {
        if (index >= 0 && index < ships.size()) {
            selectedShip = index;
        }
    }

    public static ShipData getSelectedShip() {
        return ships.get(selectedShip);
    }

    public static int getSelectedLaser() {
        return selectedLaser;
    }

    public static void setSelectedLaser(int index) {

        // Ahora tenemos 5 lasers (0, 1, 2, 3, 4)
        if (index >= 0 && index <= 4) {
            selectedLaser = index;
        }

    }

    public static BufferedImage getSelectedLaserImage() {

        switch (selectedLaser) {
            case 0: return Assets.laserPersonalizado1;
            case 1: return Assets.laserPersonalizado2;
            case 2: return Assets.boltLasers[3]; // Devuelve bolt4.png como preview
            case 3: return Assets.sparkLasers[0]; // Devuelve spark1.png como preview
            case 4: return Assets.pulseLasers[0]; // Devuelve pulse1.png como preview
            default: return Assets.laserPersonalizado1;
        }

    }
}