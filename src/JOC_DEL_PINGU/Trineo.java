package JOC_DEL_PINGU;


public class Trineo extends Casilla {
    
    // ==================== CONSTRUCTOR ====================
    public Trineo(int posicion) {
        super(posicion);
    }
    
    // ==================== ACCIÓ DE LA CASELLA ====================
    
    @Override
    public void realizarAccion(Partida p, Jugador j) {
        if (j == null || p == null || p.getTablero() == null) {
            return;
        }
        
        Tablero t = p.getTablero();
        GestorEventos ge = p.getGestorEventos();
        int tamañoTablero = t.getCasillas().size();
        
        // Buscar el següent trineu
        for (int i = j.getPosicion() + 1; i < tamañoTablero; i++) {
            Casilla c = t.getCasilla(i);
            
            if (c instanceof Trineo) {
                int casillasAvanzadas = i - j.getPosicion();
                j.moverPosicion(i);
                
                if (ge != null) {
                    ge.registrar("¡" + j.getNombre() + " usa un trineo! Avanza " + casillasAvanzadas + " casillas.");
                }
                return;
            }
        }
        
        // ==================== NO HI HA MÉS TRINEUS: CAP EFECTE ====================
        if (ge != null) {
            ge.registrar(j.getNombre() + " se baja del trineo (último trineo del tablero).");
        }
    }
}