package JOC_DEL_PINGU;

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
		
			Casilla c = t.getCasillas().get(i); 
			cont++;
			if (c instanceof Trineo && i != j.getPosicion()) { // Para que no detecte el mismo trineo
				j.moverPosicion(i);
				System.out.println(j.getNombre() + " se ha desplazado en trineo " + cont + " casillas.");
				return; 
				
				// Salimos de la función al encontrar el trineo
			} 
		}
		
		// SI TERMINA EL BUCLE Y NO HAY TRINEOS:
		System.out.println("Como no hay más trineos de aquí en adelante, " + j.getNombre() + " se queda en su misma casilla");
	}
}