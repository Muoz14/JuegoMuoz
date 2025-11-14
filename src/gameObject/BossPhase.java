package gameObject;

/**
 * Estados de la IA del Jefe Final.
 * Declarado como public en su propio archivo para que
 * GameState (en el paquete 'states') pueda acceder a él.
 */
public enum BossPhase {
    ENTERING,   // Moviéndose a la posición 1 (arriba)
    PHASE_1,    // Arriba, atacando (100% -> 50% vida)
    TRANSITION_1, // Embestida 1 (hacia el centro)
    TRANSITION_2, // Embestida 2 (hacia atrás, fuera de pantalla)
    TRANSITION_3, // Embestida 3 (de abajo al centro)
    PHASE_2,    // Abajo, atacando (50% -> 0% vida)
    DYING,      // Animación de muerte
    DEFEATED    // Estado final
}