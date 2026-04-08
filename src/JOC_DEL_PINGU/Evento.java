package JOC_DEL_PINGU;

import java.util.Random;


public class Evento extends Casilla {
    
    // ==================== CONSTRUCTOR ====================
    public Evento(int posicion) {
        super(posicion);
    }
    
    // ==================== ACCIÓ DE LA CASELLA ====================
    
    @Override
    public void realizarAccion(Partida p, Jugador j) {
        if (j == null || j.getInventario() == null) {
            return;
        }
        
        GestorEventos ge = p.getGestorEventos();
        Random rand = new Random();
        
        // Generar número random per a la probabilitat (1 a 20)
        int i = rand.nextInt(20) + 1;
        
        // ==================== 1-7: DADO LENTO  ====================
        if (i <= 7) {
            // Verificar límit de dados (màxim 3)
            if (j.puedeAgregarItem(new Dado("Dado Lento", 1, 3, 1))) {
                if (ge != null) ge.registrar("¡" + j.getNombre() + " encuentra un dado lento!");
                j.getInventario().agregarItem(new Dado("Dado Lento", 1, 3, 1));
            } else {
                if (ge != null) ge.registrar(j.getNombre() + " encuentra un dado lento, pero su inventario está lleno.");
            }
            
        // ==================== 8-12: BOLA DE NIEVE  ====================
        } else if (i <= 12) {
            int cantidadBolas = rand.nextInt(3) + 1; // 1-3 bolas
            int añadidas = 0;
            
            for (int n = 0; n < cantidadBolas; n++) {
                if (j.puedeAgregarItem(new BolaDeNieve("Bola de nieve", 1))) {
                    j.getInventario().agregarItem(new BolaDeNieve("Bola de nieve", 1));
                    añadidas++;
                }
            }
            
            if (ge != null) {
                ge.registrar(j.getNombre() + " recoge " + añadidas + " bolas de nieve (de " + cantidadBolas + " posibles).");
            }
            
        // ==================== 13-15: PEZ  ====================
        } else if (i <= 15) {
            // Verificar límit de peixos (màxim 2)
            if (j.puedeAgregarItem(new Pez("Pez", 1))) {
                if (ge != null) ge.registrar("¡" + j.getNombre() + " ha pescado un pez!");
                j.getInventario().agregarItem(new Pez("Pez", 1));
            } else {
                if (ge != null) ge.registrar(j.getNombre() + " pesca un pez, pero no puede llevar más.");
            }
            
        // ==================== 16-20: DADO RÁPIDO  ====================
        } else {
            // Verificar límit de dados (màxim 3)
            // Dado Rápido: 5-10 caselles (segons PDF)
            if (j.puedeAgregarItem(new Dado("Dado Rápido", 1, 10, 5))) {
                if (ge != null) ge.registrar("¡" + j.getNombre() + " ha tenido suerte y encuentra un Dado Rápido!");
                j.getInventario().agregarItem(new Dado("Dado Rápido", 1, 10, 5));
            } else {
                if (ge != null) ge.registrar(j.getNombre() + " encuentra un dado rápido, pero su inventario está lleno.");
            }
        }
    }
}