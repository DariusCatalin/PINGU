package JOC_DEL_PINGU;

public class Trineo extends Casilla {
	public Trineo(int posicion) {
		super(posicion);
	}
	//FUNCIÓN REALIZAR ACCIÓN SI ES TRINEO
	
	@Override
	public void realizarAccion(Partida p, Jugador j) {
		Tablero t = p.getTablero();
		
		// BUCLE PARA VER HASTA DÓNDE AVANZAMOS (Evitando el límite rígido de 50 casillas)
		int tamañoTablero = t.getCasillas().size();
		
		for (int i = j.getPosicion() + 1; i < tamañoTablero; i++) {
		
			Casilla c = t.getCasillas().get(i); 
			
			if (c instanceof Trineo) {
				int casillasAvanzadas = i - j.getPosicion();
				j.moverPosicion(i);
				if (p.getGestorEventos() != null) {
					p.getGestorEventos().registrar("¡" + j.getNombre() + " usa un trineo! Avanza " + casillasAvanzadas + ".");
				}
				return; 
			} 
		}
		
		// SI TERMINA EL BUCLE Y NO HAY TRINEOS:
		if (p.getGestorEventos() != null) {
			p.getGestorEventos().registrar(j.getNombre() + " se baja del trineo (fin de línea).");
		}	}
}