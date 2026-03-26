package JOC_DEL_PINGU;

public class Agujero extends Casilla {
    
    public Agujero(int posicion) {
        super(posicion);
    }
    
    @Override
    public void realizarAccion(Partida p, Jugador j) {
        if (j == null || p == null || p.getTablero() == null) {
            return;
        }
        
        Tablero t = p.getTablero();
        GestorEventos ge = p.getGestorEventos();
        
        // Buscar el forat anterior
        for (int i = j.getPosicion() - 1; i >= 0; i--) {
            Casilla c = t.getCasilla(i);
            
            if (c instanceof Agujero) {
                int casillasAtras = j.getPosicion() - i;
                j.moverPosicion(i);
                
                if (ge != null) {
                    ge.registrar(j.getNombre() + " cae al agujero! Retrocede " + casillasAtras + " casillas.");
                }
                return;
            }
        }
        
        // NO HI HA FORAT ANTERIOR: RETORNA A L'INICI
        j.moverPosicion(0);
        
        if (ge != null) {
            ge.registrar(j.getNombre() + " resbala hasta la casilla inicial (primer agujero).");
        }
    }
}