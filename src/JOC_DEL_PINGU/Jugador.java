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
	
	//Getters y setters
	public int getPosicion() {
		return posicion;	
	}
	
	public void setPosicion(int posicion) {
		this.posicion = posicion;
	}
	
	public String getNombre() {
		return nombre;
	}
	
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	
	public String getColor() {
		return color;
	}
	
	public void setColor(String color) {
		this.color = color;
	}
	

}
