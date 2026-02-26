package JOC_DEL_PINGU;

public class Oso extends Casilla{	
	public Oso(int posicion) {
		super(posicion);
	}
	@Override
	public void realizarAccion(Partida p, Jugador j) {
		System.out.println(j.getNombre() + " ha sido atacado por un oso. Por lo que el jugador vuelve a la casilla inicial.");
		j.moverPosicion(0);
	}
}
