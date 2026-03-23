package JOC_DEL_PINGU;

public class Agujero extends Casilla {
	public Agujero(int posicion) {
		super(posicion);
	}
	//FUNCIÓN REALIZAR ACCIÓN SI ES AGUJERO
	@Override
	public void realizarAccion(Partida p , Jugador j) {
		Tablero t = p.getTablero();
		
		// BUCLE PARA VER HASTA QUÉ CASILLA RETROCEDEMOS (Empezamos a mirar desde la casilla justo detrás)
		for (int i = j.getPosicion() - 1; i >= 0; i--) { 
			
			Casilla c = t.getCasillas().get(i); 
			
			if (c instanceof Agujero) {
				int casillasAtras = j.getPosicion() - i;
				j.moverPosicion(i);
				if (p.getGestorEventos() != null) {
					p.getGestorEventos().registrar("¡" + j.getNombre() + " cae al agujero! Retrocede " + casillasAtras + ".");
				}
				return;
			} 
		}
		
		// SI TERMINA EL BUCLE Y NO HA ENCONTRADO NINGÚN AGUJERO:
		if (p.getGestorEventos() != null) {
			p.getGestorEventos().registrar(j.getNombre() + " resbala hasta la casilla inicial.");
		}
		j.moverPosicion(0); 
	}
}
