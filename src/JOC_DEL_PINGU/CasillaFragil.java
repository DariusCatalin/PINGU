package JOC_DEL_PINGU;

import java.util.Random;

public class CasillaFragil extends Casilla {
    
    public CasillaFragil(int posicion) {
        super(posicion);
    }
    
    @Override
    public void realizarAccion(Partida p, Jugador j) {
        if (j == null || j.getInventario() == null) {
            return;
        }
        
        Inventario invJugador = j.getInventario();
        int cantidadObjetos = invJugador.getLista().size();
        GestorEventos ge = p.getGestorEventos();
        
        // MES DE 5 OBJECTES
        if (cantidadObjetos > 5) {
            j.moverPosicion(0);
            if (ge != null) {
                ge.registrar(j.getNombre() + " pesa demasiado. ¡Rompe el hielo y cae al inicio!");
            }
            
        // 1-5 OBJECTES: Penalitzacio aleatoria
        } else if (cantidadObjetos > 0 && cantidadObjetos <= 5) {
            Random rand = new Random();
            int opcion = rand.nextInt(2); // 0 o 1
            
            if (opcion == 0) {
                // Opcio A: Perdre un torn
                j.aplicarPenalizacion();
                if (ge != null) {
                    ge.registrar(j.getNombre() + " pisa hielo fragil con cuidado. Pierde un turno.");
                }
            } else {
                // Opcio B: Perdre un objecte aleatori
                j.perderObjetoAleatorio();
                if (ge != null) {
                    ge.registrar(j.getNombre() + " el hielo cruje. Pierde un objeto del inventario.");
                }
            }
            
        // 0 OBJECTES: Cap penalitzacio
        } else {
            if (ge != null) {
                ge.registrar(j.getNombre() + " pasa sin problemas por el hielo fragil (sin objetos).");
            }
        }
    }
}