package JOC_DEL_PINGU;

import java.util.ArrayList;

public class Inventario {
	
	private ArrayList<Item> lista; //Creamos la ArrayList donde se guardarán los items
	
	public Inventario() { //Constructor que llama a la ArrayList
		this.lista = new ArrayList<>();
	}
	
	//Getters y Setters

	public ArrayList<Item> getLista() {
		return lista;
	}

	public void setLista(ArrayList<Item> lista) {
		this.lista = lista;
	}
	
}
