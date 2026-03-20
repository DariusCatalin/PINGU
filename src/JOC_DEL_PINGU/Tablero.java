package JOC_DEL_PINGU;
import java.util.ArrayList;
import java.util.Random;

public class Tablero { // Se Crea la clase Tablero
	
	private ArrayList<Casilla> casillas; // Creamos ArrayList para guardar las casillas
	
	public Tablero() { // Constructor que llama al ArrayList creado
		this.casillas = new ArrayList<>();
		generarCasillasAleatorias();
	}
	
	private void generarCasillasAleatorias() {
		Random rand = new Random();

		// Casilla 0 (Inicio): Siempre vacía/normal
		this.casillas.add(new CasillaNormal(0));

		// Bucle para añadir casillas aleatorias hasta antes de la meta
		for (int i = 1; i < 49; i++) {
			int tipo = rand.nextInt(7); // Agregamos un peso más alto para que también aparezcan casillas normales en medio

			Casilla c;
			switch (tipo) {
				case 0: c = new Oso(i); break;
				case 1: c = new Trineo(i); break;
				case 2: c = new Agujero(i); break;
				case 3: c = new Evento(i); break;
				case 4: c = new CasillaFragil(i); break;
				default: c = new CasillaNormal(i); // Casos 5 y 6 darán casillas normales para equilibrar el juego
			}
			this.casillas.add(c);
		}

		// Casilla 49 (Meta): Siempre vacía/normal
		this.casillas.add(new CasillaNormal(49));
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
