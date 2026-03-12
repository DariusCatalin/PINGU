package JOC_DEL_PINGU;

public class Pinguino extends Jugador {
	
private Inventario inv;
	
	public Pinguino (int posicion, String nombre, String color) { // Contructor Llamando al constructor de la clase Jugador
		super(posicion, nombre, color);
		this.inv = new Inventario();
	}
	
	// Getters y Setters

	public Inventario getInv() { 
		return inv;
	}

	public void setInv(Inventario inv) {
		this.inv = inv;
	}
	
	public void gestionarBatalla(Pinguino p) { // Gestiona el inventario del jugador
		System.out.println("Gestionar inventario de " + p.getNombre());
	}
	
	public void usarItem(Item i) { // Muestra el nombre del item utilizado
		System.out.println("Usando el objeto: " + i.getNombre());
	}
	
	public void añadirItem(Item i) { // Añade item al inventario
		this.inv.getLista().add(i);
	}
	
	public void quitarItem(Item i) { // Quita item del inventario
		this.inv.getLista().remove(i);
	}


}

