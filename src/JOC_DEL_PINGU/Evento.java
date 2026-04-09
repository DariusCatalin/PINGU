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
            for (int k = j.getPosicion() + 1; k < t.getTotalCasillas(); k++) {
                if (t.getCasilla(k) instanceof Trineo) {
                    int avanzadas = k - j.getPosicion();
                    j.moverPosicion(k);
                    if (ge != null) ge.registrar("¡Evento! " + j.getNombre() + " encuentra una moto de nieve y vuela " + avanzadas + " casillas.");
                    encontrado = true;
                    break;
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
    }
}