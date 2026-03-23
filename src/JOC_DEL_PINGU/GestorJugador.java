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

    public void pingüinoGuerra(Pinguino p1, Pinguino p2, GestorEventos ge) {
        // MECÁNICA ELIMINADA (Nivel IMPOSSIBLE)
        ge.registrar(p1.getNombre() + " y " + p2.getNombre() + " se saludan amigablemente.");
    }

    private int contarItem(Jugador j, String nombre) {
        int count = 0;
        if (j.getInventario() != null) {
            for (Item item : j.getInventario().getLista()) {
                if (item.getNombre().equalsIgnoreCase(nombre)) {
                    count++;
                }
            }
        }
        return count;
    }

    private void vaciarItem(Jugador j, String nombre) {
        if (j.getInventario() != null) {
            j.getInventario().getLista().removeIf(item -> item.getNombre().equalsIgnoreCase(nombre));
        }
    }

    private void retrocederPinguino(Jugador j, int casillas) {
        int nuevaPos = j.getPosicion() - casillas;
        if (nuevaPos < 0) nuevaPos = 0;
        j.setPosicion(nuevaPos);
    }

    public void focaInteractua(Pinguino p, Foca f, Tablero t, GestorEventos ge) {
        // MECÁNICA ELIMINADA (Nivel IMPOSSIBLE)
        ge.registrar("La foca " + f.getNombre() + " pasa de largo.");
    }

    public void focaRebasaJugador(Pinguino p, Foca f, GestorEventos ge) {
        // MECÁNICA ELIMINADA (Nivel IMPOSSIBLE)
    }

    private int buscarAgujeroAnterior(int posicionActual, Tablero t) {
        if (t == null || t.getCasillas() == null) return 0;
        
        // Buscamos hacia atrás desde la casilla anterior a la actual
        for (int i = posicionActual - 1; i >= 0; i--) {
            if (t.getCasillas().get(i) instanceof Agujero) {
                return i;
            }
        }
        return 0; // Si no hay agujeros previos, vuelve al inicio
    }
}
