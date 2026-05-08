package JOC_DEL_PINGU;

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
                    // No usem break, usem i = -1 per sortir del bucle
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