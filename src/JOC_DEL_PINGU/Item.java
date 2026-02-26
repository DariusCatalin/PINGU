package JOC_DEL_PINGU;

public abstract class Item {
	
	//Creación de los atributos:
	
	private String nombre; 
	private int cantidad;
	
	public Item(String nombre, int cantidad) { //Constructor que llama a los atributos de la clase
		this.nombre = nombre;
		this.cantidad = cantidad;
	}
	
	//Getters y Setters

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public int getCantidad() {
		return cantidad;
	}

	public void setCantidad(int cantidad) {
		this.cantidad = cantidad;
	}
	
	

}
