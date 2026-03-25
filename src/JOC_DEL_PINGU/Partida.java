package JOC_DEL_PINGU;

import java.util.ArrayList;

public class Partida {
	// ATRIBUTOS
	private Tablero tablero;
	private ArrayList<Jugador> jugadores;
	private int turnos;
	private int jugadorActual;
	private boolean finalizada;
	private Jugador ganador;
	private GestorEventos gestorEventos;

	// CONSTRUCTOR
	public Partida() {
		this.tablero = new Tablero();
		this.jugadores = new ArrayList<Jugador>();
		this.turnos = 0; // Empezamos en la ronda 0
		this.jugadorActual = 0; // Empieza el jugador en la posición 0 del ArrayList
		this.finalizada = false;
		this.ganador = null;
	}

	// MÉTODOS DE LÓGICA DE LA PARTIDA -------------------------

	// Método para pasar al siguiente jugador
	
	public void pasarTurno() {
		this.jugadorActual++;
		
		// Si hemos llegado al último jugador, volvemos al primero y sumamos una ronda (turno)
		
		if (this.jugadorActual >= this.jugadores.size()) {
			this.jugadorActual = 0;
			this.turnos++; 
		}
	}

	// Método que comprueba si dos jugadores están en la misma casilla (Guerra de Jugadores)
	
	public void verificarColisiones(Jugador jugadorMovido) {
		
		// No hay peleas en la casilla de salida (0) ni en la meta (49)
		
		if (jugadorMovido.getPosicion() == 0 || jugadorMovido.getPosicion() >= 49) {
			return;
		}

		for (Jugador otro : jugadores) {
			
			// Si no es él mismo y están en la misma posición
			
			if (otro != jugadorMovido && otro.getPosicion() == jugadorMovido.getPosicion()) {
				
				if (gestorEventos != null) {
					gestorEventos.registrar("¡GUERRA! " + jugadorMovido.getNombre() + " y " + otro.getNombre() + " chocan en la casilla " + jugadorMovido.getPosicion());
				}

				int bolasJ1 = contarBolas(jugadorMovido);
				int bolasJ2 = contarBolas(otro);

				// Ambos gastan todas sus bolas de nieve
				gastarBolas(jugadorMovido);
				gastarBolas(otro);

				// Gana el jugador que se acaba de mover (jugadorMovido)
				if (bolasJ1 > bolasJ2) {
					int diferencia = bolasJ1 - bolasJ2;
					otro.moverPosicion(Math.max(0, otro.getPosicion() - diferencia)); // Retrocede la diferencia (sin bajar de 0)
					if (gestorEventos != null) {
						gestorEventos.registrar(jugadorMovido.getNombre() + " gana. " + otro.getNombre() + " retrocede " + diferencia + " casillas.");
					}
				// Gana el jugador que ya estaba en la casilla (otro)
				} else if (bolasJ2 > bolasJ1) {
					int diferencia = bolasJ2 - bolasJ1;
					jugadorMovido.moverPosicion(Math.max(0, jugadorMovido.getPosicion() - diferencia));
					if (gestorEventos != null) {
						gestorEventos.registrar(otro.getNombre() + " gana. " + jugadorMovido.getNombre() + " retrocede " + diferencia + " casillas.");
					}
				// Empate
				} else {
					if (gestorEventos != null) {
						gestorEventos.registrar("¡Empate de bolas! Ambos se quedan sin munición pero nadie retrocede.");
					}
				}
			}
		}
	}

	// Método auxiliar para contar las bolas de nieve de un jugador
	private int contarBolas(Jugador j) {
		int count = 0;
		for (Item i : j.getInventario().getLista()) {
			if (i.getNombre().equalsIgnoreCase("Bola de nieve")) {
				count++;
			}
		}
		return count;
	}

	// Método auxiliar para borrar todas las bolas de nieve de un jugador
	private void gastarBolas(Jugador j) {
		// Removemos de la lista de inventario todos los items que se llamen "Bola de nieve"
		j.getInventario().getLista().removeIf(i -> i.getNombre().equalsIgnoreCase("Bola de nieve"));
	}

	// GETTERS Y SETTERS ---------------------------------------
	
	public Tablero getTablero() {
		return tablero;
	}
	public void setTablero(Tablero tablero) {
		this.tablero = tablero;
	}
	public ArrayList<Jugador> getJugadores() {
		return jugadores;
	}
	public void setJugadores(ArrayList<Jugador> jugadores) {
		this.jugadores = jugadores;
	}
	public int getTurnos() {
		return turnos;
	}
	public void setTurnos(int turnos) {
		this.turnos = turnos;
	}
	public boolean isFinalizada() {
		return finalizada;
	}
	public void setFinalizada(boolean finalizada) {
		this.finalizada = finalizada;
	}
	public Jugador getGanador() {
		return ganador;
	}
	public void setGanador(Jugador ganador) {
		this.ganador = ganador;
	}
	public Jugador getJugadorActual() {
		return this.jugadores.get(this.jugadorActual);
	}
	public void setJugadorActual(int jugadorActual) {
		this.jugadorActual = jugadorActual;
	}
	public int getIndiceJugadorActual() {
		return this.jugadorActual;
	}
	public GestorEventos getGestorEventos() {
		return gestorEventos;
	}
	public void setGestorEventos(GestorEventos gestorEventos) {
		this.gestorEventos = gestorEventos;
	}
}