package JOC_DEL_PINGU;

/**
 * ============================================================
 * CLASE: Oso  (extiende Casilla)
 * ============================================================
 * Casilla de tipo Oso. Al caer en ella, el oso ataca al jugador
 * a menos que este pueda sobornarle con un Pez.
 *
 * LÓGICA (realizarAccion):
 *   - Si el jugador tiene un Pez en el inventario:
 *       → El pez se consume (eliminarItem).
 *       → El jugador se queda en su casilla (soborno exitoso).
 *       → Mensaje: "soborna al oso con un pez".
 *   - Si el jugador NO tiene Pez:
 *       → moverPosicion(0): el jugador vuelve al inicio.
 *       → Mensaje: "atacado por un oso".
 *
 * ANIMACIONES VINCULADAS (en PantallaPartida):
 *   encolarAnimacionSobornoOso(j) → cuando tiene pez.
 *   encolarAnimacionOso(j)        → cuando vuelve al inicio.
 * ============================================================
 */

public class Oso extends Casilla {
    
    // ==================== CONSTRUCTOR ====================
    public Oso(int posicion) {
        super(posicion);
    }
    
    // ==================== ACCIÓ DE LA CASELLA ====================
    
    @Override
    public void realizarAccion(Partida p, Jugador j) {
        if (j != null && j.getInventario() != null) {
            GestorEventos ge = p.getGestorEventos();
            Item pez = null;
            boolean trobat = false;
            
            // Buscar un Pez a l'inventari
            java.util.List<Item> items = j.getInventario().getLista();
            for (int i = 0; i < items.size() && !trobat; i++) {
                Item item = items.get(i);
                if (item != null && item.getNombre().equalsIgnoreCase("Pez")) {
                    pez = item;
                    trobat = true;
                }
            }
            
            // ==================== TÉ PEZ: SUBORNA L'ÓS ====================
            if (pez != null) {
                j.getInventario().eliminarItem(pez);
                if (ge != null) {
                    ge.registrar("¡" + j.getNombre() + " soborna al oso con un pez y se queda en su sitio!");
                }
                
            // ==================== NO TÉ PEZ: RETORNA A L'INICI ====================
            } else {
                j.moverPosicion(0);
                if (ge != null) {
                    ge.registrar("¡" + j.getNombre() + " es atacado por un oso! Vuelve al inicio.");
                }
            }
        }
    }
}