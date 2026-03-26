package JOC_DEL_PINGU;


public abstract class Casilla {
    
    // ==================== ATRIBUTS ====================
    private int posicion;
    private String tipo; // Per identificar el tipus de casella fàcilment
    
    // ==================== CONSTRUCTOR ====================
    public Casilla(int posicion) {
        // Validem que la posició sigui vàlida (0-49+)
        if (posicion < 0) {
            throw new IllegalArgumentException("La posició no pot ser negativa");
        }
        this.posicion = posicion;
        this.tipo = this.getClass().getSimpleName();
    }
    
    // ==================== MÈTODE ABSTRACTE ====================
    
 
    public abstract void realizarAccion(Partida partida, Jugador jugador);
    
    // ==================== GETTERS I SETTERS ====================
    
    public int getPosicion() {
        return posicion;
    }
    
    public void setPosicion(int posicion) {
        if (posicion < 0) {
            throw new IllegalArgumentException("La posició no pot ser negativa");
        }
        this.posicion = posicion;
    }
    
    public String getTipo() {
        return tipo;
    }
    
    // ==================== MÈTODE AUXILIAR ====================
  
    @Override
    public String toString() {
        return tipo + " (Posició: " + posicion + ")";
    }
}