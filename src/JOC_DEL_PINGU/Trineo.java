package JOC_DEL_PINGU;
//SUBCLASE TRINEO
public class Trineo extends Casilla {
	public Trineo(int posicion) {
		super(posicion);
	}
	//FUNCIÓN REALIZAR ACCIÓN SI ES TRINEO
	@Override
	public void realizarAccion(Partida p, Jugador j) {
		Tablero t = p.getTablero();
		int cont = 0;
		//BUCLE PARA VER HASTA DÓNDE AVANZAMOS
		for (int i = j.getPosicion(); i < 50; i++) {
			t.getCasillas.get(i);
			Casilla c = t.getCasillas.get(i);
			cont++;
			if (c instanceof Trineo) { //SI TENEMOS UN TRINEO MÁS ADELANTE (AVANZAMOS HASTA ESA CASILLA)
				j.moverPosicion(i);
				System.out.println(j.getNombre() + " se ha desplazado en trineo " + cont + " casillas.");
			} else { //SI NO LO TENEMOS (NOS QUEDAMOS EN LA MISMA POSICIÓN)
				System.out.println("Como no hay más trineos de aquí en adelante, " + j.getNombre() + " se queda en su misma casilla");
			}
		}

	}
}
