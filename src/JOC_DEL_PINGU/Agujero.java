package JOC_DEL_PINGU;

/**
 * ============================================================
 * CLASE: Agujero  (extiende Casilla)
 * ============================================================
 * Casilla de tipo Agujero en el hielo. Al caer en ella, el
 * jugador retrocede a la casilla Agujero anterior más cercana,
 * o al inicio si no hay ninguna antes.
 *
 * LÓGICA (realizarAccion):
 *   Busca hacia atrás (desde pos-1 hasta 0) la primera casilla
 *   de tipo Agujero. Si la encuentra, mueve al jugador allí.
 *   Si no hay ningún agujero anterior, moverPosicion(0).
 *
 * USO EN EL JUEGO:
 *   - La Foca también usa la lógica de agujero anterior cuando
 *     da un coletazo: buscarAgujeroAnterior() en PantallaPartida.
 *   - PantallaPartida encola encolarAnimacionAgujero() al detectar
 *     que el jugador se movió por esta casilla.
 * ============================================================
 */

public class Agujero extends Casilla {
    
    public Agujero(int posicion) {
        
    	super(posicion);
    	
    }
    
    @Override
    public void realizarAccion(Partida p, Jugador j) {
        if (j != null) {
            GestorEventos ge = p.getGestorEventos();
            int posActual = j.getPosicion();
            
            // Busca la casella "Agujero" anterior a la posició actual
            int posAnterior = -1;
            Tablero t = p.getTablero();
            
            for (int i = posActual - 1; i >= 0; i--) {
                if (t.getCasilla(i) instanceof Agujero) {
                    posAnterior = i;
                    i = -1; 
                }
            }
            
            if (posAnterior != -1) {
                j.moverPosicion(posAnterior);
                if (ge != null) {
                    ge.registrar("¡" + j.getNombre() + " cae en un agujero! Retrocede al agujero anterior (casilla " + posAnterior + ").");
                }
            } else {
                j.moverPosicion(0);
                if (ge != null) {
                    ge.registrar("¡" + j.getNombre() + " cae en un agujero! No hay agujeros anteriores, vuelve al inicio.");
                }
            }
        }
    }
    
}