package JOC_DEL_PINGU;

import java.util.ArrayList;
import java.util.Random;


public class Tablero {
    
    // ==================== ATRIBUTOS ====================
    private ArrayList<Casilla> casillas;
    private static final int TOTAL_CASILLAS = 50; // 0-49
    private static final int POSICION_META = 49;
    
    // ==================== CONSTRUCTOR ====================
    public Tablero() {
        this.casillas = new ArrayList<Casilla>();
        generarCasillasAleatorias();
    }
    
    // ==================== GENERACIÓN DEL TABLERO ====================
    
    /**
     * Genera las 50 casillas del tablero.
     * - Casilla 0: Inicio (siempre normal)
     * - Casillas 1-48: Aleatorias con diferentes tipos
     * - Casilla 49: Meta (siempre normal)
     */
    private void generarCasillasAleatorias() {
        Random rand = new Random();

        // Casilla 0 (Inicio): Siempre vacía/normal
        this.casillas.add(new CasillaNormal(0));
        System.out.println("Casilla 0 (Inicio) creada.");

        // Bucle para añadir casillas aleatorias (posiciones 1-48)
        for (int i = 1; i < POSICION_META; i++) {
            Casilla c = null;
            int intentos = 0;

            while (intentos < 20) {
                int roll = rand.nextInt(100); // 0-99

                // Probabilidades: Normal 60%, Oso 10%, Trineo 10%, Agujero 10%, CasillaFragil 10%
                String tipoCandidato;
                if (roll < 60) {
                    tipoCandidato = "Normal";
                } else if (roll < 70) {
                    tipoCandidato = "Oso";
                } else if (roll < 80) {
                    tipoCandidato = "Trineo";
                } else if (roll < 90) {
                    tipoCandidato = "Agujero";
                } else {
                    tipoCandidato = "CasillaFragil";
                }

                // Las casillas normales no tienen restricción de repetición
                if (tipoCandidato.equals("Normal")) {
                    c = new CasillaNormal(i);
                    break;
                }

                // Comprobar que el mismo tipo especial no aparece en ninguna de las 3 casillas anteriores
                boolean bloqueada = false;
                for (int retro = 1; retro <= 3 && (i - retro) >= 0; retro++) {
                    if (this.casillas.get(i - retro).getClass().getSimpleName().equals(tipoCandidato)) {
                        bloqueada = true;
                        break;
                    }
                }

                if (!bloqueada) {
                    switch (tipoCandidato) {
                        case "Oso":          c = new Oso(i);          break;
                        case "Trineo":       c = new Trineo(i);       break;
                        case "Agujero":      c = new Agujero(i);      break;
                        case "CasillaFragil": c = new CasillaFragil(i); break;
                    }
                    break;
                }

                intentos++;
            }

            // Fallback tras 20 intentos sin éxito: casilla normal
            if (c == null) {
                c = new CasillaNormal(i);
            }

            this.casillas.add(c);
        }

        // Casilla 49 (Meta): Siempre vacía/normal
        this.casillas.add(new CasillaNormal(POSICION_META));
        System.out.println("Casilla 49 (Meta) creada.");

        System.out.println("Tablero generado con " + casillas.size() + " casillas.");
    }
    
    // ==================== MÉTODOS DE UTILIDAD ====================
    
 
    public Casilla getCasilla(int posicion) {
        if (posicion >= 0 && posicion < casillas.size()) {
            return casillas.get(posicion);
        }
        return null;
    }
    
    
    public boolean esMeta(int posicion) {
        return posicion >= POSICION_META;
    }
    
 
    public void actualizarTablero() {
        System.out.println("==== DIBUJANDO MAPA ====");
        
        if (casillas.isEmpty()) {
            System.out.println("¡El tablero no tiene casillas generadas!");
            return;
        }
        
        StringBuilder mapa = new StringBuilder();
        
        for (int i = 0; i < casillas.size(); i++) {
            Casilla c = casillas.get(i);
            String nombreTipo = c.getClass().getSimpleName();
            
            mapa.append("[").append(nombreTipo).append("] ");
            
            // Saltos de línea cada 10 casillas para mejor visualización
            if ((i + 1) % 10 == 0) {
                mapa.append("\n");
            } else if (i < casillas.size() - 1) {
                mapa.append(" - ");
            }
        }
        
        System.out.println(mapa.toString());
        System.out.println("========================");
    }
    
    // ==================== GETTERS Y SETTERS ====================
    
    public ArrayList<Casilla> getCasillas() {
        return casillas;
    }
    
    public void setCasillas(ArrayList<Casilla> casillas) {
        this.casillas = casillas;
    }
    
    public int getTotalCasillas() {
        return casillas.size();
    }
    
    public int getPosicionMeta() {
        return POSICION_META;
    }
}