package JOC_DEL_PINGU;
import java.util.ArrayList;
public class Tablero { // Se Crea la clase Tablero
	
	private ArrayList<Casilla> casillas; // Creamos ArrayList para guardar las casillas
	
	public Tablero() { // Constructor que llama al ArrayList creado
		this.casillas = new ArrayList<>();
	}
	
	//GETTERS/SETTERS

	public ArrayList<Casilla> getCasillas() { 
		return casillas;
	}

	public void setCasillas(ArrayList<Casilla> casillas) {
		this.casillas = casillas;
	}
	
	public void actualizarTablero() { // método que actualiza el tablero.
		System.out.println("==== DIBUJANDO MAPA ====");
		
		if(casillas.isEmpty()) {
			System.out.println("¡El tablero no tiene casillas generadas!");
		} else {
			
			// Usamos StringBuilder para ir pintando el camino en la misma línea
			StringBuilder mapa = new StringBuilder();
			
			for (int i = 0; i < casillas.size(); i++) {
				Casilla c = casillas.get(i);
				// Extraemos el nombre de la clase (Ej: CasillaFragil -> Fragil)
				String nombreTipo = c.getClass().getSimpleName();
				
				mapa.append("[").append(nombreTipo).append("]");
				
				// Saltos de línea para que las 50 casillas no se vayan del borde de la pantalla
				if ((i + 1) % 10 == 0) {
					mapa.append("\n");
				} else if (i < casillas.size() - 1) {
					mapa.append(" - ");
				}
			}
			
			System.out.println(mapa.toString());
			System.out.println("========================");
		}
	}

}
