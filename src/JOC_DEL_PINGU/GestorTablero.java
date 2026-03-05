package JOC_DEL_PINGU;

public class GestorTablero { // creamos la clase que gestiona el tablero.
	
	public void ejecutarCasilla(Partida partida, Pinguino p, Casilla c) { // método el cual va a ejecutar la casilla donde se situa el jugador.
		
		System.out.println("Ejecutando la casilla para el Pingüino: " + p.getNombre());
		
		c.realizarAccion(partida, p);
		
	}
	
	public void comprobarFinTurno(Partida partida) { //creamos el método que va a comprobar cuando finaliza el turno del jugador y si ha terminado la partida llegando a la meta.
		
		Jugador jugador = partida.getJugadorActual();
		
		int meta = 50;
		
		if(jugador.getPosicion() >= meta) {
			
			System.out.println("El jugador " + jugador.getNombre() + " ha llegado a la meta.");
			
			partida.setGanador(jugador);
			partida.setFinalizada(true);
			
		} else {
			
			System.out.println("Fin de turno de " + jugador.getNombre());
			
			int jug_actual = partida.getIndiceJugadorActual();
			int jugadores = partida.getJugadores().size();
			
			int siguiente = (jug_actual + 1) % jugadores;
			
			partida.setJugadorActual(siguiente);
		}
		
		
	}

}
