package JOC_DEL_PINGU;

public class Foca extends Jugador {
    // Atributo
    private boolean soborno;

    // Constructor
    public Foca(int posicion, String nombre, String color, boolean soborno) {
        
        super(posicion, nombre, color);
        this.soborno = soborno;
    }

    public void aplastarJugador(Pinguino p) {
        System.out.println(this.getNombre() + " está intentando aplastar a " + p.getNombre());
        // Lógica del juego: restar vida al pingüino, moverlo al inicio, etc.
    }

    public void golpearJugador(Pinguino p) {
        System.out.println(this.getNombre() + " ha golpeado a " + p.getNombre());
        // Lógica del juego aquí
    }

    public void esSoborno(int cantidad) {
        if (cantidad > 0) {
            this.soborno = true;
            System.out.println("La foca ha sido sobornada.");
        } else {
            this.soborno = false;
        }
    }

    // --- Getters y Setters ---

    public boolean isSoborno() {
        return soborno;
    }

    public void setSoborno(boolean soborno) {
        this.soborno = soborno;
    }
}
