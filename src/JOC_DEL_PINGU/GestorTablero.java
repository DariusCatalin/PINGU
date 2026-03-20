package JOC_DEL_PINGU;

public class GestorTablero { // creamos la clase que gestiona el tablero.
	
	public void ejecutarCasilla(Partida partida, Jugador j, Casilla c) { // método el cual va a ejecutar la casilla donde se situa el jugador.
		
		System.out.println("Ejecutando la casilla para el jugador: " + j.getNombre());
		
		c.realizarAccion(partida, j);
		
	}
	
	public void comprobarFinTurno(Partida partida) { //creamos el método que va a comprobar cuando finaliza el turno del jugador y si ha terminado la partida llegando a la meta.
		
		Jugador jugador = partida.getJugadorActual();
		
		// La meta debe ser la última casilla del tablero, no un estático 50,
		// ya que GestorJugador no deja pasar de ese máximo (t.getCasillas().size() - 1).
		int meta = 49;
		if (partida.getTablero() != null && !partida.getTablero().getCasillas().isEmpty()) {
		    meta = partida.getTablero().getCasillas().size() - 1;
		}
		
		if(jugador.getPosicion() >= meta) {
			
			System.out.println("El jugador " + jugador.getNombre() + " ha llegado a la meta.");
			
			partida.setGanador(jugador);
			partida.setFinalizada(true);
			
		} else {
			
			// El turno ya cambia de Jugador en GestorPartida.siguienteTurno().
			// Si lo hacemos aquí también, se salta el turno a un jugador de forma injusta.
			// System.out.println("Fin de turno de " + jugador.getNombre());
		}
		
	}

}
