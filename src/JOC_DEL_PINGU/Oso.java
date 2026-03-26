package JOC_DEL_PINGU;

public class Oso extends Casilla {
    
    // ==================== CONSTRUCTOR ====================
    public Oso(int posicion) {
        super(posicion);
    }
    
    // ==================== ACCIÓ DE LA CASELLA ====================
    
    @Override
    public void realizarAccion(Partida p, Jugador j) {
        if (j == null || j.getInventario() == null) {
            return;
        }
        
        GestorEventos ge = p.getGestorEventos();
        Item pez = null;
        
        // Buscar un Pez a l'inventari
        for (Item item : j.getInventario().getLista()) {
            if (item != null && item.getNombre().equalsIgnoreCase("Pez")) {
                pez = item;
                break;
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