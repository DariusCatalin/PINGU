package JOC_DEL_PINGU;

public class Pinguino extends Jugador {
    
    // ==================== CONSTRUCTOR ====================
    
   
    public Pinguino(int posicion, String nombre, String color) {
        super(posicion, nombre, color);
        // L'inventari es crea al constructor de Jugador
    }
    
    // ==================== MÈTODES D'INVENTARI ====================
    
   
    public boolean añadirItem(Item i) {
        if (i == null) {
            System.out.println("Error: L'item no pot ser null");
            return false;
        }
        
        // Verifiquem els límits abans d'afegir
        if (!this.getInventario().puedeAgregarItem(i)) {
            System.out.println("Error: No es pot afegir " + i.getNombre() + 
                             " (s'ha assolit el màxim)");
            return false;
        }
        
        boolean añadido = this.getInventario().agregarItem(i);
        if (añadido) {
            System.out.println("Objecte " + i.getNombre() + 
                             " guardat a la motxilla de " + this.getNombre());
        }
        return añadido;
    }
    
    
    public void quitarItem(Item i) {
        if (i != null) {
            boolean eliminat = this.getInventario().eliminarItem(i);
            if (eliminat) {
                System.out.println("Objecte " + i.getNombre() + 
                                 " eliminat de la motxilla de " + this.getNombre());
            }
        }
    }
    
   
    public void usarItem(Item i) {
        if (i != null) {
            System.out.println(this.getNombre() + 
                             " estira l'aleta i usa l'objecte: " + i.getNombre());
        }
    }
    
    // ==================== MÈTODES DE COMBAT ====================
    
  
    public void gestionarBatalla(Pinguino p) {
        // Aquest mètode és per al Nivell IMPOSSIBLE
        System.out.println("¡El pingüí " + this.getNombre() + 
                         " xoca panxes contra el pingüí " + p.getNombre() + "!");
        // La lògica completa es gestiona a Partida.verificarColisiones()
    }
    
 
}