package JOC_DEL_PINGU;

public abstract class Jugador {
	//Atributos
	private int posicion;
	private String nombre;
	private String color;
	
	//Constructor
	public Jugador(int posicion, String nombre, String color) {
		this.posicion = posicion;
		this.nombre = nombre;
		this.color = color;
		
	}
	
	public void moverPosicion(int p) {
		this.posicion = p;
		System.out.println(this.nombre + "se ha movido a la posicion" + this.posicion);
	}
	

}
