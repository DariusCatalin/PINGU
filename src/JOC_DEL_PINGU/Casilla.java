package JOC_DEL_PINGU;

//CLASE ABSTRACTA CASILLA
public abstract class Casilla {
	//ATRIBUTOS
	private int posicion;
	//CONSTRUCTOR
	public Casilla(int posicion) {
		this.posicion = posicion;
	}
	//GETTER
	public int getPosicion() {
		return posicion;
	}
	//SETTER
	public void setPosicion(int posicion) {
		this.posicion = posicion;
	}
	//FUNCIÓN ABSTRACTA
	public abstract void realizarAccion(Partida p, Jugador j); 
		
}
