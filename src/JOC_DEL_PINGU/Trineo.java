package JOC_DEL_PINGU;

/**
 * ============================================================
 * CLASE: Trineo  (extiende Casilla)
 * ============================================================
 * Casilla de tipo Trineo. Al caer en ella, el jugador avanza
 * casillas extra (deslizamiento hacia adelante).
 *
 * LÓGICA (realizarAccion):
 *   El trineo hace avanzar al jugador hacia adelante en el
 *   tablero. El número de casillas avanzadas depende de la
 *   implementación concreta (avance fijo o hasta el siguiente
 *   trineo según la versión).
 *
 * USO EN EL JUEGO:
 *   - El evento "Moto de nieve" (en Evento.java) también busca
 *     la siguiente casilla Trineo y teletransporta al jugador.
 *   - PantallaPartida encola encolarAnimacionTrineo() cuando
 *     detecta que el jugador se movió por esta casilla.
 * ============================================================
 */


public class Trineo extends Casilla {
    
    // ==================== CONSTRUCTOR ====================
    public Trineo(int posicion) {
        super(posicion);
    }
    
    // ==================== ACCIÓ DE LA CASELLA ====================
    
    @Override
    public void realizarAccion(Partida p, Jugador j) {
        if (j != null && p != null && p.getTablero() != null) {
            Tablero t = p.getTablero();
            GestorEventos ge = p.getGestorEventos();
            int tamañoTablero = t.getCasillas().size();
            boolean trobat = false;
            
            // Buscar el següent trineu
            for (int i = j.getPosicion() + 1; i < tamañoTablero && !trobat; i++) {
                Casilla c = t.getCasilla(i);
                
                if (c instanceof Trineo) {
                    int casillasAvanzadas = i - j.getPosicion();
                    j.moverPosicion(i);
                    
                    if (ge != null) {
                        ge.registrar("¡" + j.getNombre() + " usa un trineo! Avanza " + casillasAvanzadas + " casillas.");
                    }
                    trobat = true;
                }
            }
            
            // ==================== NO HI HA MÉS TRINEUS: CAP EFECTE ====================
            if (!trobat && ge != null) {
                ge.registrar(j.getNombre() + " se baja del trineo (último trineo del tablero).");
            }
        }
    }
}