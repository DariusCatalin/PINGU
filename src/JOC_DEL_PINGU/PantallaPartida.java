package JOC_DEL_PINGU; // Adaptado a tu estructura de paquetes

import java.util.ArrayList;
import java.util.Random;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class PantallaPartida {

    // --- Menu items ---
    @FXML private MenuItem newGame;
    @FXML private MenuItem saveGame;
    @FXML private MenuItem loadGame;
    @FXML private MenuItem quitGame;

    // --- Botones (Daus y objetos) ---
    @FXML private Button dado;
    @FXML private Button rapido;
    @FXML private Button lento;
    @FXML private Button peces;
    @FXML private Button nieve;

    // --- Textos ---
    @FXML private Text dadoResultText;
    @FXML private Text rapido_t;
    @FXML private Text lento_t;
    @FXML private Text peces_t;
    @FXML private Text nieve_t;
    @FXML private Text eventos;

    // --- Tablero y Fichas ---
    @FXML private GridPane tablero;
    @FXML private Circle P1;
    @FXML private Circle P2;
    @FXML private Circle P3;
    @FXML private Circle P4;

    private GestorPartida gestorPartida;
    private static final int COLUMNS = 5;
    private static final String TAG_CASILLA_TEXT = "CASILLA_TEXT";

    @FXML
    private void initialize() {
        eventos.setText("¡El juego ha comenzado!");

        gestorPartida = new GestorPartida();
        
        // 1. Crear jugadores usando tu constructor real (posicion, nombre, color)
        ArrayList<Jugador> jugadores = new ArrayList<>();
        Pinguino p1 = new Pinguino(0, "Jugador1", "Azul");
        
        // Añadir el dado normal al inventario (máximo 3 daus permitidos según el PDF)
        Dado dadoEstandar = new Dado("Dado Normal", 1, 1, 6);
        p1.getInventario().getLista().add(dadoEstandar);
        
        jugadores.add(p1);

        // 2. Crear un tablero (asumiendo que tienes un constructor vacío en Tablero)
        Tablero tableroModelo = new Tablero();
        
        // 3. Iniciar la partida usando tu método real
        gestorPartida.nuevaPartida(jugadores, tableroModelo);

        // Mostrar info del tablero (requiere que Tablero tenga getCasillas() poblado)
        if(gestorPartida.getPartida().getTablero().getCasillas() != null && 
           !gestorPartida.getPartida().getTablero().getCasillas().isEmpty()) {
            mostrarTiposDeCasillasEnTablero(gestorPartida.getPartida().getTablero());
        }
    }

    private void mostrarTiposDeCasillasEnTablero(Tablero t) {
        tablero.getChildren().removeIf(node -> TAG_CASILLA_TEXT.equals(node.getUserData()));

        for (int i = 0; i < t.getCasillas().size(); i++) {
            Casilla casilla = t.getCasillas().get(i);

            if (i > 0 && i < 49) {
                String tipo = casilla.getClass().getSimpleName();
                Text texto = new Text(tipo);
                texto.setUserData(TAG_CASILLA_TEXT);
                texto.getStyleClass().add("cell-type");

                int row = i / COLUMNS;
                int col = i % COLUMNS;

                GridPane.setRowIndex(texto, row);
                GridPane.setColumnIndex(texto, col);
                tablero.getChildren().add(texto);
            }
        }
    }

    // ==========================================
    // ACCIONES DE MENÚ
    // ==========================================
    @FXML
    private void handleNewGame() {
        System.out.println("Nueva partida.");
        // Podrías reiniciar la lista de jugadores y llamar a initialize()
    }

    @FXML
    private void handleSaveGame() {
        gestorPartida.guardarPartida();
    }

    @FXML
    private void handleLoadGame() {
        // Ejemplo genérico, deberías pedir el ID al usuario
        gestorPartida.cargarPartida(1); 
    }

    @FXML
    private void handleQuitGame() {
        System.exit(0);
    }

    // ==========================================
    // ACCIONES DE BOTONES
    // ==========================================
    @FXML
    private void handleDado(ActionEvent event) {
        // Obtenemos el jugador actual desde la partida
        Jugador pinguActual = gestorPartida.getPartida().getJugadorActual();
        
        // Guardamos su posición vieja para la animación
        int posPrevia = pinguActual.getPosicion();
        
        // Buscamos si tiene un dado en el inventario
        Dado dadoAUsar = null;
        if (!pinguActual.getInventario().getLista().isEmpty()) {
            // Asumiendo que el primer objeto es un dado
            dadoAUsar = (Dado) pinguActual.getInventario().getLista().get(0);
        }
        
        // Tiramos el dado usando tu método (si dadoAUsar es null, tu método ya usa el estándar)
        int resultado = gestorPartida.tirarDado(pinguActual, dadoAUsar);
        
        // Guardamos la nueva posición que ha calculado tu lógica
        int posNueva = pinguActual.getPosicion();

        dadoResultText.setText("Ha salido: " + resultado);

        // Movemos la ficha visualmente usando las posiciones reales del modelo
        moveP1(posPrevia, posNueva);
    }

    // He adaptado el método del profe para que reciba la posición de origen y destino reales
    private void moveP1(int oldPosition, int newPosition) {
        dado.setDisable(true);

        // Posición vieja
        int oldRow = oldPosition / COLUMNS;
        int oldCol = oldPosition % COLUMNS;

        // Posición nueva
        int newRow = newPosition / COLUMNS;
        int newCol = newPosition % COLUMNS;

        // Tamaño de celda aproximado
        double cellWidth = tablero.getWidth() / COLUMNS;
        double cellHeight = tablero.getHeight() / 10;

        double dx = (newCol - oldCol) * cellWidth;
        double dy = (newRow - oldRow) * cellHeight;

        TranslateTransition slide = new TranslateTransition(Duration.millis(350), P1);
        slide.setByX(dx);
        slide.setByY(dy);

        slide.setOnFinished(e -> {
            // Reset translation
            P1.setTranslateX(0);
            P1.setTranslateY(0);

            // Set real position in grid
            GridPane.setRowIndex(P1, newRow);
            GridPane.setColumnIndex(P1, newCol);

            dado.setDisable(false);
            
            // Aquí podrías comprobar si hay que ejecutar la casilla
            // gestorPartida.getGestorTablero().ejecutarCasilla(...);
        });

        slide.play();
    }

    @FXML
    private void handleRapido() { System.out.println("Dado Rápido."); }

    @FXML
    private void handleLento() { System.out.println("Dado Lento."); }

    @FXML
    private void handlePeces() { System.out.println("Usar Pez."); }

    @FXML
    private void handleNieve() { 
        System.out.println("Lanzar Boles de neu."); 
        // En tu turno, puedes tirar bolas de nieve [cite: 206]
    }

    public void setGestorPartida(GestorPartida gestorPartida) {
        this.gestorPartida = gestorPartida;
    }
}