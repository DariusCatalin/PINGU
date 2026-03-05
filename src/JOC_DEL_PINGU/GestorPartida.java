package JOC_DEL_PINGU;
import java.util.Random;
public class GestorPartida {
	//ATRIBUTOS
	private Partida partida;
	private GestorTablero gestorTablero;
	private GestorJugador gestorJugador;
	private GestorBBDD gestorBBDD;
	private Random random;
	
	//CONSTRUCTOR
	public GestorPartida() {
        this.random = new Random();
        this.gestorTablero = new GestorTablero();
        this.gestorJugador = new GestorJugador();
        this.gestorBBDD = new GestorBBDD();
    }
	
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
		actualizarEstadoTablero();
		
		System.out.println("Partida creada. Número de Jugadores: " + jugadores.size());
	}
	
	public int tirarDado(Jugador j, Dado dadoOpcional) {
		Dado dadoAUsar;
		
		//SI USAMOS UN DADO DEL INVENTARIO O NO
		if (dadoOpcional != null) {
			dadoAUsar = dadoOpcional;
			System.out.println(j.getNombre() + " ha usado un dado especial: " + dadoAUsar.getNombre());
		} else {
			dadoAUsar = new Dado("Dado Estándar", 1, 1, 6);
			System.out.println(j.getNombre() + " usa un dado normal.");
		}
		
		//OBTENEMOS EL NÚMERO DEL DADO Y MOVEMOS LA POSICIÓN DEL JUGADOR
		int resultado = dadoAUsar.tirar(this.random);
		System.out.println(j.getNombre() + " avanza " + resultado + " casillas.");
		this.gestorJugador.jugadorSeMueve(j, resultado, this.partida.getTablero());
		j.avanzarCasillas(resultado);
		
		//HACEMOS EL RETURN DEL RESULTADO
		return resultado;
	}
	
	public void ejecutarTurnoCompleto() {
		//OBTENER EL JUGADOR ACTUAL
		Jugador actual = partida.getJugadorActual();
		System.out.println("Turno de " + actual.getNombre());
		
		//PROCESAMOS SU TURNO
		procesarTurnoJugador(actual);
		
		//VEMOS SI HAY GANADOR
		this.gestorTablero.comprobarFinTurno(this.partida);
		
		//PASAMOS AL SIGUIENTE TURNO SI NO SE HA ACABADO LA PARTIDA
		if(!this.partida.isFinalizada()) {
			siguienteTurno();
		} else {
			System.out.println("¡El juego ha terminado! El ganador es " + actual.getNombre());
		}
	}
	
	public void procesarTurnoJugador(Jugador j) {
		//TIRAMOS EL DADO
		int resultadoDado = tirarDado(j, null);
				
		//BUSCAR LA CASILLA A LA CUÁL HA CAÍDO
		Casilla casillaActual = this.partida.getTablero().getCasillas().get(j.getPosicion());
				
		//EJECUTAMOS LA CASILLA
		this.gestorTablero.ejecutarCasilla(this.partida, (Pinguino) j , casillaActual);
				
		//ACTUALIZAMOS LA INTERFAZ
		actualizarEstadoTablero();
	}
}
