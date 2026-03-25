package JOC_DEL_PINGU;

import java.util.ArrayList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.paint.ImagePattern;
import javafx.scene.image.Image;

public class PantallaPartida {

    // --- INTERFAZ (FXML) ---
    @FXML private MenuItem newGame;
    @FXML private MenuItem saveGame;
    @FXML private MenuItem loadGame;
    @FXML private MenuItem quitGame;

    @FXML private Button dado;
    @FXML private Button rapido;
    @FXML private Button lento;
    @FXML private Button peces;
    @FXML private Button nieve;

    @FXML private Text dadoResultText;
    @FXML private Text eventos; // Aquí mostraremos lo que pasa en el juego

    @FXML private GridPane tablero;
    @FXML private Circle P1; // Pingüino Jugador
    @FXML private Circle P2; // Foca CPU
    @FXML private Circle P3;
    @FXML private Circle P4;

    // --- LÓGICA DEL JUEGO (EL CEREBRO) ---
    private Partida partida;
    
    // Un gestor de eventos simple para mostrar textos en la pantalla
    private GestorEventos gestorUI = new GestorEventos() {
        @Override
        public void registrar(String mensaje) {
            // Actualiza el texto de la pantalla con lo que pasa
            System.out.println(mensaje);
            if (eventos != null) {
                eventos.setText(mensaje);
            }
        }
    };

    @FXML
    private void initialize() {
        System.out.println("Cargando Pantalla Partida...");
        cargarSkins();
        
        // 1. INICIALIZAMOS LA PARTIDA
        partida = new Partida();
        partida.setGestorEventos(gestorUI);
        
        // 2. CREAMOS LOS JUGADORES (Ejemplo: 1 Pingüino y 1 Foca CPU)
        Pinguino jugador1 = new Pinguino(0, "Pingu", "Azul");
        Foca cpuFoca = new Foca(0, "Focabrón", "Rojo");
        
        partida.getJugadores().add(jugador1);
        partida.getJugadores().add(cpuFoca);
        
        gestorUI.registrar("¡Partida iniciada! Turno de " + partida.getJugadorActual().getNombre());
    }

    // ==========================================
    // ACCIÓN PRINCIPAL: TIRAR EL DADO NORMAL
    // ==========================================
    @FXML
    private void tirarDadoNormal(ActionEvent event) {
        if (partida.isFinalizada()) return;

        Jugador actual = partida.getJugadorActual();
        
        // Verificamos si es un Pingüino (el jugador humano)
        if (actual instanceof Pinguino) {
            
        }
    }
    
 // ==========================================
    // USO DE OBJETOS DEL INVENTARIO
    // ==========================================

    @FXML
    private void tirarDadoRapido(ActionEvent event) {
        usarDadoEspecial("Dado Rápido", 4, 6); // El dado rápido tira entre 4 y 6
    }

    @FXML
    private void tirarDadoLento(ActionEvent event) {
        usarDadoEspecial("Dado Lento", 1, 3); // El dado lento tira entre 1 y 3
    }

    // Método auxiliar para no repetir código en los dados
    private void usarDadoEspecial(String nombreDado, int min, int max) {
        if (partida.isFinalizada()) return;
        Jugador actual = partida.getJugadorActual();

        if (actual instanceof Pinguino) {
            if (consumirObjeto(actual, nombreDado)) {
                int tirada = (int)(Math.random() * (max - min + 1)) + min;
                actual.avanzarCasillas(tirada);
                dadoResultText.setText("Has sacado un: " + tirada + " (" + nombreDado + ")");
                gestorUI.registrar(actual.getNombre() + " usa " + nombreDado + " y avanza a la casilla " + actual.getPosicion());
                
                ejecutarLogicaCasilla(actual);
                partida.pasarTurno();
                actualizarPosicionVisual(actual, P1);

                if (partida.getJugadorActual() instanceof Foca) {
                    jugarTurnoCPU((Foca) partida.getJugadorActual(), P2);
                }
            } else {
                gestorUI.registrar("¡No tienes ningún " + nombreDado + " en la mochila!");
            }
        }
    }

    @FXML
    private void usarPez(ActionEvent event) {
        Jugador actual = partida.getJugadorActual();
        if (actual instanceof Pinguino) {
            // El pez no gasta turno al usarse desde el botón, simplemente avisa que lo tienes preparado
            if (tieneObjeto(actual, "Pez")) {
                gestorUI.registrar("¡Tienes un Pez listo! Si la Foca te ataca, se lo darás automáticamente.");
            } else {
                gestorUI.registrar("No tienes Peces. ¡Huye de la Foca!");
            }
        }
    }

    @FXML
    private void usarBolaNieve(ActionEvent event) {
        Jugador actual = partida.getJugadorActual();
        if (actual instanceof Pinguino) {
            // Igual que el pez, es un objeto pasivo para las batallas
            int cantidad = contarBolas(actual);
            gestorUI.registrar("Tienes " + cantidad + " Bolas de Nieve listas para la guerra.");
        }
    }

    // ==========================================
    // MÉTODOS AUXILIARES DE INVENTARIO
    // ==========================================

    // Comprueba si tiene el objeto y, si lo tiene, lo borra (lo gasta)
    private boolean consumirObjeto(Jugador j, String nombreObjeto) {
        for (Item item : j.getInventario().getLista()) {
            if (item.getNombre().equalsIgnoreCase(nombreObjeto)) {
                j.getInventario().getLista().remove(item);
                actualizarTextosInventario(j); // Actualizamos los numeritos de la pantalla
                return true;
            }
        }
        return false;
    }

    // Solo comprueba si lo tiene (sin gastarlo)
    private boolean tieneObjeto(Jugador j, String nombreObjeto) {
        for (Item item : j.getInventario().getLista()) {
            if (item.getNombre().equalsIgnoreCase(nombreObjeto)) {
                return true;
            }
        }
        return false;
    }

    // Cuenta cuántas bolas de nieve tiene
    private int contarBolas(Jugador j) {
        int count = 0;
        for (Item item : j.getInventario().getLista()) {
            if (item.getNombre().equalsIgnoreCase("Bola de nieve")) {
                count++;
            }
        }
        return count;
    }

    // Actualiza los Text de JavaFX con la cantidad de objetos que te quedan
    private void actualizarTextosInventario(Jugador j) {
        int dadosRapidos = 0, dadosLentos = 0, peces = 0, bolas = 0;
        
        for (Item item : j.getInventario().getLista()) {
            switch (item.getNombre().toLowerCase()) {
                case "dado rápido": dadosRapidos++; break;
                case "dado lento": dadosLentos++; break;
                case "pez": peces++; break;
                case "bola de nieve": bolas++; break;
            }
        }
        
        // Asumiendo que tus Text de la interfaz se llaman así:
        if (rapido_t != null) rapido_t.setText("x" + dadosRapidos);
        if (lento_t != null) lento_t.setText("x" + dadosLentos);
        if (peces_t != null) peces_t.setText("x" + peces);
        if (nieve_t != null) nieve_t.setText("x" + bolas);
    }
}