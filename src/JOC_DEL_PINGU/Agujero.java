package JOC_DEL_PINGU;
//SUBCLASE AGUJERO
public class Agujero extends Casilla{
	public Agujero(int posicion) {
		super(posicion);
	}
	//FUNCIÓN REALIZAR ACCIÓN SI ES AGUJERO
	@Override
	public void realizarAccion(Partida p , Jugador j) {
		Tablero t = p.getTablero();
		int cont = 0;
		//BUCLE PARA VER HASTA QUE CASILLA RETROCEDEMOS
		for (int i = j.getPosicion(); i > 50; i--) {
			t.getCasillas.get(i);
			Casilla c = t.getCasillas.get(i);
			cont++;
			if (c instanceof Agujero) { //SI TENEMOS UN AGUJERO MÁS ATRÁS
				j.moverPosicion(i);
				System.out.println(j.getNombre() + " se ha caído por un agujero y ha retrocedido " + cont + " casillas.");
			} else { //SI NO LO TENEMOS (VOLVEMOS A LA CASILLA INICIAL)
				System.out.println("Como no hay más agujeros detrás de " + j.getNombre() + ", retrocede hasta la casilla inicial.");
				j.moverPosicion(1);
			}
		}		
	}
}
