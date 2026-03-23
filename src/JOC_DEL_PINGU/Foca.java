package JOC_DEL_PINGU;

public class Foca extends Jugador {
    // Atributos
    private boolean soborno;

    // Constructor
    public Foca(int posicion, String nombre, String color, boolean soborno) {
        super(posicion, nombre, color);
        this.soborno = soborno;
    }

    // --- Getters y Setters ---

    public boolean isSoborno() {
        return soborno;
    }

    public void setSoborno(boolean soborno) {
        this.soborno = soborno;
    }
}
