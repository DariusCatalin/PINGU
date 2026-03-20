package JOC_DEL_PINGU;

public class Agujero extends Casilla {
	public Agujero(int posicion) {
		super(posicion);
	}
	//FUNCIÓN REALIZAR ACCIÓN SI ES AGUJERO
	@Override
	public void realizarAccion(Partida p , Jugador j) {
		Tablero t = p.getTablero();
		
		// BUCLE PARA VER HASTA QUÉ CASILLA RETROCEDEMOS (Empezamos a mirar desde la casilla justo detrás)
		for (int i = j.getPosicion() - 1; i >= 0; i--) { 
			
			Casilla c = t.getCasillas().get(i); 
			
			if (c instanceof Agujero) {
				int casillasAtras = j.getPosicion() - i;
				j.moverPosicion(i);
				System.out.println("¡" + j.getNombre() + " se ha caído por un agujero y ha retrocedido " + casillasAtras + " casillas de golpe!");
				return; // Salimos de la función al encontrar el agujero
			} 
		}
		
		// SI TERMINA EL BUCLE Y NO HA ENCONTRADO NINGÚN AGUJERO:
		System.out.println("No hay más agujeros detrás de " + j.getNombre() + ", así que resbala hasta la casilla inicial.");
		j.moverPosicion(0); // El inicio real es el índice 0, no el 1
	}
}
