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
            
        // 1-5 OBJECTES: Pierde un turno
        } else if (cantidadObjetos > 0 && cantidadObjetos <= 5) {
            j.aplicarPenalizacion();
            if (ge != null) {
                ge.registrar(j.getNombre() + " pisa hielo frágil con cuidado y pierde un turno.");
            }
            
        // 0 OBJECTES: Cap penalitzacio
        } else {
            if (ge != null) {
                ge.registrar(j.getNombre() + " pasa sin problemas por el hielo fragil (sin objetos).");
            }
        }
    }
}