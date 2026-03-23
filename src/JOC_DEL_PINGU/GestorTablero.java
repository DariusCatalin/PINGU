package JOC_DEL_PINGU;

public class GestorTablero { // creamos la clase que gestiona el tablero.
	
	public void ejecutarCasilla(Partida partida, Jugador j, Casilla c) { // método el cual va a ejecutar la casilla donde se situa el jugador.
		
		System.out.println("Ejecutando la casilla para el jugador: " + j.getNombre());
		
		c.realizarAccion(partida, j);
		
	}
	
	public void comprobarFinTurno(Partida partida) { 
		GestorEventos ge = partida.getGestorEventos();
		int meta = 49;
		if (partida.getTablero() != null && !partida.getTablero().getCasillas().isEmpty()) {
		    meta = partida.getTablero().getCasillas().size() - 1;
		}

		// Comprobar TODOS los jugadores para ver si alguien llegó a la meta
		for (Jugador j : partida.getJugadores()) {
			if (j.getPosicion() >= meta) {
				partida.setGanador(j);
				partida.setFinalizada(true);
				
				if (ge != null) {
					ge.registrar("********************************");
					ge.registrar("¡FIN DE LA PARTIDA!");
					ge.registrar("EL GANADOR ES: " + j.getNombre());
					ge.registrar("********************************");
				}
				break;
			}
		}
	}

}
