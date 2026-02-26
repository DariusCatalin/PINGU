package JOC_DEL_PINGU;

public abstract class Casilla {
	private int posicion;
	
	public Casilla(int posicion) {
		this.posicion = posicion;
	}

	public int getPosicion() {
		return posicion;
	}

	public void setPosicion(int posicion) {
		this.posicion = posicion;
	}
	
	public abstract void realizarAccion(Partida p, Jugador j); 
		
}
