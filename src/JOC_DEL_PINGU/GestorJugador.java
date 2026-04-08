package JOC_DEL_PINGU;

import java.util.Random;


public class GestorJugador {
    
    // ==================== CONSTRUCTOR ====================
   
    public GestorJugador() {
        // No cal inicialització addicional
    }
    
    // ==================== MÈTODES DE GESTIÓ D'ITEMS ====================
    
 
    public void jugadorUsaItem(Jugador j, String nombreItem) {
        if (j == null || j.getInventario() == null || j.getInventario().getLista() == null) {
            System.out.println("Error: Inventari no vàlid");
            return;
        }
        
        Item itemAUsar = null;
        
        // Busquem si té l'item
        for (Item item : j.getInventario().getLista()) {
            if (item != null && item.getNombre().equalsIgnoreCase(nombreItem)) {
                itemAUsar = item;
                break;
            }
        }
        
        if (itemAUsar != null) {
            // L'eliminem de l'inventari en usar-lo
            j.getInventario().eliminarItem(itemAUsar);
            
            // Si és un Pingüí, cridem al seu mètode intern
            if (j instanceof Pinguino) {
                ((Pinguino) j).usarItem(itemAUsar);
            } else {
                System.out.println(j.getNombre() + " usa l'objecte: " + itemAUsar.getNombre());
            }
        } else {
            System.out.println(j.getNombre() + " no té l'objecte " + nombreItem + " al seu inventari.");
        }
    }
    
    // ==================== MÈTODES DE MOVIMENT ====================
    
  
    public void jugadorSeMueve(Jugador j, int passos, Tablero t) {
        if (j == null) {
            System.out.println("Error: Jugador null a jugadorSeMueve()");
            return;
        }
        
        int novaPos = j.getPosicion() + passos;
        
        // Obtenim la mida real del taulell (si existeix)
        int maxPos = 49;
        if (t != null && t.getCasillas() != null && !t.getCasillas().isEmpty()) {
            maxPos = t.getCasillas().size() - 1;
        }
        
        // Verifiquem límits
        if (novaPos > maxPos) {
            novaPos = maxPos;
        }
        
        if (novaPos < 0) {
            novaPos = 0;
        }
        
        j.setPosicion(novaPos);
        System.out.println(j.getNombre() + " s'ha mogut a la casella " + novaPos);
    }
    
    
    public void jugadorMoureAPosicio(Jugador j, int posicio, Tablero t) {
        if (j == null) {
            return;
        }
        
        int maxPos = 49;
        if (t != null && t.getCasillas() != null && !t.getCasillas().isEmpty()) {
            maxPos = t.getCasillas().size() - 1;
        }
        
        // Verifiquem límits
        if (posicio < 0) {
            posicio = 0;
        }
        if (posicio > maxPos) {
            posicio = maxPos;
        }
        
        j.setPosicion(posicio);
        System.out.println(j.getNombre() + " s'ha mogut a la casella " + posicio);
    }
    
    // ==================== MÈTODES DE FINALITZACIÓ DE TORN ====================
    
  
    public void jugadorFinalizaTurno(Jugador j) {
        if (j == null) {
            return;
        }
        
        if (j.getTurnosPenalizados() > 0) {
            System.out.println(j.getNombre() + " finalitza el seu torn. Li queden " + 
                             j.getTurnosPenalizados() + " torns de penalització.");
        } else {
            System.out.println(j.getNombre() + " ha finalitzat el seu torn correctament sense penalitzacions.");
        }
    }
    
    // ==================== MÈTODES D'ESDEVENIMENTS ====================
    
   
    public void piguinoEvento(Pinguino p) {
        if (p == null) {
            return;
        }
        System.out.println("¡El pingüí " + p.getNombre() + " ha activat un esdeveniment sorpresa!");
        // Aquí es podria delegar la lògica a una Casilla de Evento
    }
    
    // ==================== MÈTODES DE GUERRA  ====================
    
   
    public void pingüinoGuerra(Pinguino p1, Pinguino p2, GestorEventos ge) {
        // MECÀNICA DESACTIVADA 
        if (ge != null) {
            ge.registrar(p1.getNombre() + " i " + p2.getNombre() + " es saluden amigablement.");
        }
        System.out.println("[Nivell IMPOSSIBLE] Guerra entre jugadors desactivada.");
    }
    
    // ==================== MÈTODES AUXILIARS ====================
    
  
    private int contarItem(Jugador j, String nom) {
        int count = 0;
        if (j != null && j.getInventario() != null) {
            for (Item item : j.getInventario().getLista()) {
                if (item != null && item.getNombre().equalsIgnoreCase(nom)) {
                    count++;
                }
            }
        }
        return count;
    }
    
   
    private void vaciarItem(Jugador j, String nom) {
        if (j != null && j.getInventario() != null) {
            j.getInventario().getLista().removeIf(item -> 
                item != null && item.getNombre().equalsIgnoreCase(nom)
            );
        }
    }
    
    
    private void retrocederPinguino(Jugador j, int caselles) {
        if (j == null) {
            return;
        }
        
        int novaPos = j.getPosicion() - caselles;
        if (novaPos < 0) {
            novaPos = 0;
        }
        j.setPosicion(novaPos);
    }
    
    // ==================== MÈTODES DE FOCA ====================
   
    public void focaInteractua(Pinguino p, Foca f, Tablero t, GestorEventos ge) {
        // MECÀNICA DESACTIVADA 
        if (ge != null) {
            ge.registrar("La foca " + f.getNombre() + " passa de llarg.");
        }
        System.out.println("[Nivell IMPOSSIBLE] Interacció Foca-Pingüí desactivada.");
    }
    
   
    public void focaRebasaJugador(Pinguino p, Foca f, GestorEventos ge) {
        // MECÀNICA DESACTIVADA
        System.out.println("[Nivell IMPOSSIBLE] Foca rebasa jugador desactivat.");
    }
    
   
    public void jugadorPerdObjecteAleatori(Jugador j) {
        if (j == null) {
            return;
        }
        j.perderObjetoAleatorio();
    }
    
   
    public void aplicarPenalizacion(Jugador j, int torns) {
        if (j == null) {
            return;
        }
        
        for (int i = 0; i < torns; i++) {
            j.aplicarPenalizacion();
        }
        
        System.out.println(j.getNombre() + " ha estat penalitzat " + torns + " torns.");
    }
}