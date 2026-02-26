package JOC_DEL_PINGU;
//SUBCLASE EVENTO
public class Evento extends Casilla {
	//ATRIBUTOS
	private String[] eventos;
	//CONSTRUCTOR
	public Evento(int posicion) {
		super(posicion);
		this.eventos = eventos;
	}
	//FUNCIÓN REALIZAR ACCIÓN SI ES EVENTO
	@Override
	public void realizarAccion(Partida p, Jugador j) {
		
	}
}
