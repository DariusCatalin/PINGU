package JOC_DEL_PINGU;
import java.util.Random;
public class GestorPartida {
	//ATRIBUTOS
	private Partida partida;
	private GestorTablero gestorTablero;
	private GestorJugador gestorJugador;
	private GestorBBDD gestorBBDD;
	private Random random;
	
	public void nuevaPartida(ArrayList<Jugador> jugadores, Tablero tablero) {
		//CREAMOS LA INSTANCIA DEL MODELO PARTIDA
		this.partida = new Partida();
		
		//ASIGNAMOS EL TABLERO Y LA LISTA DE JUGADORES
		this.partida.setTablero(tablero);
		this.partida.setJugadores(jugadores);
		
		//INICIALIZAMOS LOS VALORES POR DEFECTO DE LA PARTIDA
		this.partida.setTurnos(0);
		this.partida.setJugadorActual(0);
		this.partida.setFinalizada(false);
		
		//ASIGNAR POSICIÓN INICIAL A CADA JUGADOR
		for (Jugador j : jugadores) {
			j.setPosicion(0);
			if (j instanceof Pinguino) {
				((Pinguino) j).setInventario(new Inventario());
			}
		}
		//REINICIAR LA INTERFAZ DEL JUEGO
		this.actualizarEstadoTablero();
		
		System.out.println("Partida creada. Número de Jugadores: " + jugadores.size());
	}
}
