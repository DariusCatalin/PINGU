package JOC_DEL_PINGU;

import java.util.ArrayList;

public class Partida {
	//ATRIBUTOS
	private Tablero tablero;
	private ArrayList<Jugador> jugadores;
	private int turnos;
	private int jugadorActual;
	private boolean finalizada;
	private Jugador ganador;
	//CONSTRUCTOR
	public Partida() {
		this.tablero = new Tablero();
		this.jugadores = new ArrayList<Jugador>();
		this.turnos = 0;
		this.jugadorActual = 0;
		this.finalizada = false;
		this.ganador = null;
	}
	//GETTERS Y SETTERS
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
	//GETTER Y SETTER JUGADOR ACTUAL
	public Jugador getJugadorActual() {
		return this.jugadores.get(this.jugadorActual);
	}
	
	public void setJugadorActual(int jugadorActual) {
		this.jugadorActual = jugadorActual;
	}
}
