package JOC_DEL_PINGU;

import java.util.ArrayList;

public class Inventario {
	
	private ArrayList<Item> lista; //Creamos la ArrayList donde se guardarán los items
	
	public Inventario() { //Constructor que llama a la ArrayList
		this.lista = new ArrayList<>();
	}
	

	public boolean añadirItem(Item nuevoItem) {
		int contadorDados = 0;
		int contadorPeces = 0;
		int contadorBolas = 0;
		
		// Contamos lo que ya hay en la mochila
		
		for(Item i : lista) {
			if(i.getNombre().toLowerCase().contains("dado")) contadorDados++;
			else if(i.getNombre().equalsIgnoreCase("Pez")) contadorPeces++;
			else if(i.getNombre().equalsIgnoreCase("Bola de nieve")) contadorBolas++;
		}
		
		// Comprobamos los límites antes de meter nada
		
		if(nuevoItem.getNombre().toLowerCase().contains("dado") && contadorDados >= 3) {
			System.out.println("Mochila llena de Dados. No caben más.");
			return false;
		}
		if(nuevoItem.getNombre().equalsIgnoreCase("Pez") && contadorPeces >= 2) {
			System.out.println("Mochila llena de Peces. No caben más.");
			return false;
		}
		if(nuevoItem.getNombre().equalsIgnoreCase("Bola de nieve") && contadorBolas >= 6) {
			System.out.println("Mochila llena de Bolas de nieve. No caben más.");
			return false;
		}
		
		// Si pasa los controles, lo añadimos
		
		lista.add(nuevoItem);
		return true;
	}
	
	//Getters y Setters
	
	public ArrayList<Item> getLista() {
		return lista;
	}

	public void setLista(ArrayList<Item> lista) {
		this.lista = lista;
	}
}