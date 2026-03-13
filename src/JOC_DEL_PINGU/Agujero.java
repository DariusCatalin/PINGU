package JOC_DEL_PINGU;

public class Agujero extends Casilla {
	public Agujero(int posicion) {
		super(posicion);
	}
	//FUNCIÓN REALIZAR ACCIÓN SI ES AGUJERO
	@Override
	public void realizarAccion(Partida p , Jugador j) {
		Tablero t = p.getTablero();
		int cont = 0;
		
		//BUCLE PARA VER HASTA QUE CASILLA RETROCEDEMOS
		
		// CORRECCIÓN: Cambiamos i > 50 por i >= 0 para que vaya hacia atrás correctamente
		
		for (int i = j.getPosicion(); i >= 0; i--) { 
			
			Casilla c = t.getCasillas().get(i); 
			cont++;
			if (c instanceof Agujero && i != j.getPosicion()) { // Añadido para no detectar el mismo agujero en el que estamos
				j.moverPosicion(i);
				System.out.println(j.getNombre() + " se ha caído por un agujero y ha retrocedido " + cont + " casillas.");
				return; // Salimos de la función al encontrar el agujero
			} 
		}		
		// SI TERMINA EL BUCLE Y NO HA ENCONTRADO NADA:
		System.out.println("Como no hay más agujeros detrás de " + j.getNombre() + ", retrocede hasta la casilla inicial.");
		j.moverPosicion(1);
	}
}
