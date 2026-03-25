package JOC_DEL_PINGU;

public class Pinguino extends Jugador {
	
	public Pinguino(int posicion, String nombre, String color) { // Constructor Llamando al constructor de la clase Jugador
		super(posicion, nombre, color);
		// Eliminamos la inicialización doble porque Jugador.java ya le crea un inventario por defecto.
	}
	
	public void gestionarBatalla(Pinguino p) { // Lógica de batalla
		System.out.println("¡El pingüino " + this.getNombre() + " choca barrigas contra el pingüino " + p.getNombre() + "!");
	}
	
	public void usarItem(Item i) { // Muestra el nombre del item utilizado
		System.out.println(this.getNombre() + " estira la aleta y usa el objeto: " + i.getNombre());
	}
	
	public void añadirItem(Item i) { 
		boolean añadido = this.getInventario().añadirItem(i);
		if (añadido) {
			System.out.println("Objeto " + i.getNombre() + " guardado en la mochila de " + this.getNombre());
		}
	}
	
	public void quitarItem(Item i) { // Quita item del inventario heredado
		this.getInventario().getLista().remove(i);
	}
}
