package JOC_DEL_PINGU;

/**
 * ============================================================
 * CLASE: CasillaFragil  (extiende Casilla)
 * ============================================================
 * Casilla de hielo frágil. Su efecto depende del número de
 * objetos que lleva el jugador en el inventario.
 *
 * LÓGICA PRINCIPAL (realizarAccion):
 *   - > 5 objetos  → moverPosicion(0): el hielo se rompe,
 *                    el jugador vuelve al inicio.
 *   - 1–5 objetos  → aplicarPenalizacion(): pierde un turno.
 *   - 0 objetos    → pasa sin penalización.
 *
 * EVENTO ADICIONAL ALEATORIO (aplicarEventAdicionalAleatorio):
 *   - 20% → pierde un turno adicional (total 2 si tenía 1–5 obj).
 *   - 20% → pierde un objeto aleatorio del inventario.
 *   - 60% → nada extra.
 *   Solo se aplica si el jugador NO ha vuelto al inicio.
 *
 * ANIMACIONES VINCULADAS (en PantallaPartida):
 *   encolarAnimacionFragil(j) → muestra imagen y texto del efecto.
 *   encolarSaltoDirecto(j, 0) → si volvió al inicio.
 * ============================================================
 */

import java.util.ArrayList;
import java.util.Random;

public class CasillaFragil extends Casilla {

    private static final Random rnd = new Random();

    public CasillaFragil(int posicion) {
        super(posicion);
    }

    @Override
    public void realizarAccion(Partida p, Jugador j) {
        if (j != null && j.getInventario() != null) {
            Inventario invJugador = j.getInventario();
            int cantidadObjetos = invJugador.getLista().size();
            GestorEventos ge = p.getGestorEventos();

            // ===== LÒGICA PRINCIPAL =====
            if (cantidadObjetos > 5) {
                // MÉS DE 5 OBJECTES → cau a l'inici
                j.moverPosicion(0);
                if (ge != null) {
                    ge.registrar(j.getNombre() + " pesa demasiado. ¡Rompe el hielo y cae al inicio!");
                }
            } else {
                if (cantidadObjetos > 0) {
                    // 1-5 OBJECTES → perd un torn
                    j.aplicarPenalizacion();
                    if (ge != null) {
                        ge.registrar(j.getNombre() + " pisa hielo frágil con cuidado y pierde un turno.");
                    }
                } else {
                    // 0 OBJECTES → cap penalització
                    if (ge != null) {
                        ge.registrar(j.getNombre() + " pasa sin problemas por el hielo frágil (sin objetos).");
                    }
                }

                // ===== EVENTS ADDICIONALS ALEATORIS =====
                // Probabilitat: 20% perdre un torn extra · 20% perdre un objecte · 60% res
                aplicarEventAdicionalAleatorio(j, ge);
            }
        }
    }

    /**
     * Aplica un event addicional aleatori en la casella de gel fràgil:
     *  - 20% → Perd un torn addicional.
     *  - 20% → Perd un objecte aleatori de l'inventari.
     *  - 60% → Cap event addicional.
     */
    private void aplicarEventAdicionalAleatorio(Jugador j, GestorEventos ge) {
        int tirada = rnd.nextInt(100); // [0, 99]

        if (tirada < 20) {
            // EVENT A: Perd un torn addicional
            j.aplicarPenalizacion();
            if (ge != null) {
                ge.registrar("💨 El gel cruje bajo los pies de " + j.getNombre()
                        + ". ¡Evento adicional: pierde un turno extra!");
            }
        } else if (tirada < 40) {
            // EVENT B: Perd un objecte aleatori de l'inventari
            ArrayList<Item> lista = j.getInventario().getLista();
            if (!lista.isEmpty()) {
                int indiceAleatorio = rnd.nextInt(lista.size());
                Item perdido = lista.remove(indiceAleatorio);
                if (ge != null) {
                    ge.registrar("❄️ ¡El hielo cede! " + j.getNombre()
                            + " pierde \"" + perdido.getNombre() + "\" del inventario.");
                }
            } else {
                if (ge != null) {
                    ge.registrar("❄️ El hielo tiembla, pero " + j.getNombre()
                            + " no lleva nada que perder.");
                }
            }
        }
        // [40-99] → cap event addicional (silenciós)
    }
}