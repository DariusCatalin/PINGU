package JOC_DEL_PINGU;

public class Trineo extends Casilla {
	public Trineo(int posicion) {
		super(posicion);
	}
	
	@Override
	public void realizarAccion(Partida p, Jugador j) {
		System.out.println(j.getNombre() + " se ha desplazado en trineo hasta el próximo trineo del tablero.");
		Tablero t = p.getTablero();
		for (int i = j.getPosicion(); i < 50; i++) {
			t.getCasillas.get(i);
			Casilla c = t.getCasillas.get(i);
			if (c instanceof Trineo) {
				j.moverPosicion(i);
			}
		}
	}
}
