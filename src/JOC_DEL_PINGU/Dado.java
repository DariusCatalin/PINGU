package JOC_DEL_PINGU;

public class Dado extends Item{ // Clase Dado con relación extend a Item
	
	 //Creamos los Atributos
	
	private int max;
	private int min;
	
	public Dado(String nombre, int cantidad, int max, int min) { // Constructor que llama a los Atributos de Dado y constructor de la clase Item
		super(nombre, cantidad);
		this.max = max;
		this.min = min;
	}
	
	//Getters y Setters

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}
	
	public int tirar() { // Método que acciona el dado haciendo una tirada aleatoria entre el mínimo y máximo del propio dado
		
		int r = (int) (Math.random() * this.max) + this.min;
		return r;
		
	}

}
