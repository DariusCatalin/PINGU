package JOC_DEL_PINGU;

public class GestorJugador {

    // Se añadió el parámetro 'Jugador j' ya que es necesario para saber qué inventario usar.
    public void jugadorUsaItem(Jugador j, String nombreItem) {
        if (j.getInventario() != null && j.getInventario().getLista() != null) {
            Item itemAUsar = null;
            // Buscamos si tiene el item
            for (Item item : j.getInventario().getLista()) {
                if (item.getNombre().equalsIgnoreCase(nombreItem)) {
                    itemAUsar = item;
                    break;
                }
            }
            
            if (itemAUsar != null) {
                // Lo borramos del inventario al usarlo
                j.getInventario().getLista().remove(itemAUsar);
                
                // Si es un Pingüino, llamamos a su método interno
                if (j instanceof Pinguino) {
                    ((Pinguino) j).usarItem(itemAUsar);
                } else {
                    System.out.println(j.getNombre() + " usa el objeto: " + itemAUsar.getNombre());
                }
            } else {
                System.out.println(j.getNombre() + " no tiene el objeto " + nombreItem + " en su inventario.");
            }
        }
    }

    public void jugadorSeMueve(Jugador j, int pasos, Tablero t) {
    	int nuevaPos = j.getPosicion() + pasos;
    	
        // Obtenemos el tamaño real del tablero (si existe)
        int maxPos = 49;
        if (t != null && t.getCasillas() != null && !t.getCasillas().isEmpty()) {
            maxPos = t.getCasillas().size() - 1;
        }

    	if(nuevaPos > maxPos) {
    		nuevaPos = maxPos;
    	}
    	
    	if (nuevaPos < 0 ) {
    		nuevaPos = 0;
    	}
    	
    	j.setPosicion(nuevaPos);
        System.out.println(j.getNombre() + " se ha movido a la casilla " + nuevaPos);
    }

    public void jugadorFinalizaTurno(Jugador j) {
        if (j.getTurnosPenalizados() > 0) {
            System.out.println(j.getNombre() + " finaliza su turno. Le quedan " + j.getTurnosPenalizados() + " turnos de penalización.");
        } else {
            System.out.println(j.getNombre() + " ha finalizado su turno correctamente sin penalizaciones.");
        }
    }

    public void piguinoEvento(Pinguino p) {
        System.out.println("¡El pingüino " + p.getNombre() + " ha activado un evento sorpresa!");
        // Aquí se podría delegar la lógica a una Casilla de Evento
    }

    public void pingüinoGuerra(Pinguino p1, Pinguino p2) {
        System.out.println("¡Ha empezado una guerra entre " + p1.getNombre() + " y " + p2.getNombre() + "!");
        // Utilizamos el método propio de Pinguino
        p1.gestionarBatalla(p2);
    }

    public void focaInteractua(Pinguino p, Foca f) {
        // Comprobamos si la Foca está sobornada
        if (f.isSoborno()) {
            System.out.println("La foca " + f.getNombre() + " está sobornada y amigable. Decide ignorar a " + p.getNombre());
        } else {
            // Interacción aleatoria agresiva
            System.out.println("¡Cuidado! La foca " + f.getNombre() + " no está sobornada y ataca a " + p.getNombre() + "!");
            if (Math.random() > 0.5) {
                f.aplastarJugador(p);
            } else {
                f.golpearJugador(p);
            }
        }
    }
}
