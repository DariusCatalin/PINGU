package JOC_DEL_PINGU;

/**
 * ============================================================
 * CLASE: Evento  (extiende Casilla)
 * ============================================================
 * Casilla de evento aleatorio. Al caer en ella, se genera
 * un número del 1 al 100 y se ejecuta uno de 7 efectos posibles.
 *
 * EFECTOS Y PROBABILIDADES (método realizarAccion):
 *   1–35  (35%): Dado Lento al inventario (avanza 1-3).
 *   36–55 (20%): 1 a 3 Bolas de Nieve al inventario.
 *   56–75 (20%): Pez al inventario (protección vs Oso/Foca).
 *   76–85 (10%): Perder un turno (penalización +1).
 *   86–90  (5%): Perder un objeto aleatorio del inventario.
 *   91–95  (5%): Moto de nieve → teletransporte a la siguiente
 *                casilla Trineo del tablero.
 *   96–100 (5%): Dado Rápido al inventario (avanza 5-10).
 *
 * VALIDACIONES:
 *   - puedeAgregarItem() se comprueba antes de añadir cualquier
 *     ítem para respetar los límites del inventario.
 *   - Si el inventario está lleno del tipo dado, muestra mensaje
 *     informativo pero no penaliza al jugador.
 * ============================================================
 */

import java.util.Random;


public class Evento extends Casilla {
    
    // ==================== CONSTRUCTOR ====================
    public Evento(int posicion) {
        super(posicion);
    }
    
    // ==================== ACCIÓ DE LA CASELLA ====================
    
    @Override
    public void realizarAccion(Partida p, Jugador j) {
        if (j != null && j.getInventario() != null) {
        
        GestorEventos ge = p.getGestorEventos();
        Random rand = new Random();
        
        // Generar número random del 1 al 100
        int i = rand.nextInt(100) + 1;
        
        // ==================== 1-35 (35%): DADO LENTO  ====================
        if (i <= 35) {
            if (j.puedeAgregarItem(new Dado("Dado Lento", 1, 3, 1))) {
                if (ge != null) ge.registrar("¡Evento! " + j.getNombre() + " encuentra un dado lento.");
                j.getInventario().agregarItem(new Dado("Dado Lento", 1, 3, 1));
            } else {
                if (ge != null) ge.registrar("¡Evento! " + j.getNombre() + " halla un dado lento, pero no tiene nivel.");
            }
            
        // ==================== 36-55 (20%): BOLA DE NIEVE  ====================
        } else if (i <= 55) {
            int cantidadBolas = rand.nextInt(3) + 1; // 1-3 bolas
            int añadidas = 0;
            for (int n = 0; n < cantidadBolas; n++) {
                if (j.puedeAgregarItem(new BolaDeNieve("Bola de nieve", 1))) {
                    j.getInventario().agregarItem(new BolaDeNieve("Bola de nieve", 1));
                    añadidas++;
                }
            }
            if (ge != null) {
                ge.registrar("¡Evento! " + j.getNombre() + " recoge " + añadidas + " bolas de nieve.");
            }
            
        // ==================== 56-75 (20%): PEZ  ====================
        } else if (i <= 75) {
            if (j.puedeAgregarItem(new Pez("Pez", 1))) {
                if (ge != null) ge.registrar("¡Evento! " + j.getNombre() + " ha pescado un pez.");
                j.getInventario().agregarItem(new Pez("Pez", 1));
            } else {
                if (ge != null) ge.registrar("¡Evento! " + j.getNombre() + " pesca un pez, pero va lleno.");
            }
            
        // ==================== 76-85 (10%): PERDER TURNO  ====================
        } else if (i <= 85) {
            j.aplicarPenalizacion();
            if (ge != null) ge.registrar("¡Evento! " + j.getNombre() + " se distrae y pierde un turno.");

        // ==================== 86-90 (5%): PERDER OBJETO  ====================
        } else if (i <= 90) {
            if (j.getInventario().tamañoInventario() > 0) {
                j.perderObjetoAleatorio();
                if (ge != null) ge.registrar("¡Evento! " + j.getNombre() + " ha perdido un objeto sin querer.");
            } else {
                if (ge != null) ge.registrar("¡Evento! " + j.getNombre() + " se tropieza pero no tenía objetos que perder.");
            }

        // ==================== 91-95 (5%): MOTOS DE NIEVE  ====================
        } else if (i <= 95) {
            Tablero t = p.getTablero();
            boolean encontrado = false;
            for (int k = j.getPosicion() + 1; k < t.getTotalCasillas() && !encontrado; k++) {
                if (t.getCasilla(k) instanceof Trineo) {
                    int avanzadas = k - j.getPosicion();
                    j.moverPosicion(k);
                    if (ge != null) ge.registrar("¡Evento! " + j.getNombre() + " encuentra una moto de nieve y vuela " + avanzadas + " casillas.");
                    encontrado = true;
                }
            }
            if (!encontrado) {
                if (ge != null) ge.registrar("¡Evento! " + j.getNombre() + " halla piezas de moto inútiles.");
            }

        // ==================== 96-100 (5%): DADO RÁPIDO  ====================
        } else {
            if (j.puedeAgregarItem(new Dado("Dado Rápido", 1, 10, 5))) {
                if (ge != null) ge.registrar("¡Evento Increíble! " + j.getNombre() + " encuentra un Dado Rápido.");
                j.getInventario().agregarItem(new Dado("Dado Rápido", 1, 10, 5));
            } else {
                if (ge != null) ge.registrar("¡Evento! " + j.getNombre() + " halla un dado rápido pero no tiene espacio.");
            }
        }
        } // fin if (j != null && j.getInventario() != null)
    }
}