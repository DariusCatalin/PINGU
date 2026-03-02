package JOC_DEL_PINGU;

public class GestorTablero {
	
	public void ejecutarCasilla(Partida partida, Pinguino p, Casilla c) {
		
		System.out.println("Ejecutando la casilla para el Pingüino: " + p.getNombre());
		
		c.realizarAccion(partida, p);
		
	}
	
	public void comprobarFinTurno(Partida partida) {
		
	}

}
