package JOC_DEL_PINGU;

public class CasillaNormal extends Casilla {

	public CasillaNormal(int posicion) {
		super(posicion);
	}

	@Override
	public void realizarAccion(Partida p, Jugador j) {
		System.out.println(j.getNombre() + " pisa hielo firme. Aquí no pasa nada.");
	}
}
