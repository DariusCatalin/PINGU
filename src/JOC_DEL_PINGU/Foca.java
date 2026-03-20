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
        System.out.println("¡" + this.getNombre() + " lanza su enorme peso y aplasta a " + p.getNombre() + "!");
        System.out.println(p.getNombre() + " sale despedido por los aires hasta la casilla de salida.");
        p.moverPosicion(0);
    }

    public void golpearJugador(Pinguino p) {
        System.out.println("¡" + this.getNombre() + " le ha dado un fuerte aletazo a " + p.getNombre() + "!");
        System.out.println(p.getNombre() + " se queda aturdido y pierde su próximo turno.");
        p.aplicarPenalizacion();
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
