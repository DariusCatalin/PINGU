package JOC_DEL_PINGU;

import java.util.ArrayList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class PantallaConfig {

    // ==================== FXML ====================
    @FXML private Button btn2J;
    @FXML private Button btn3J;
    @FXML private Button btn4J;
    @FXML private CheckBox chkCPU;

    @FXML private HBox filaJ1;
    @FXML private HBox filaJ2;
    @FXML private HBox filaJ3;
    @FXML private HBox filaJ4;

    @FXML private TextField nombreJ1;
    @FXML private TextField nombreJ2;
    @FXML private TextField nombreJ3;
    @FXML private TextField nombreJ4;

    @FXML private ComboBox<String> colorJ1;
    @FXML private ComboBox<String> colorJ2;
    @FXML private ComboBox<String> colorJ3;
    @FXML private ComboBox<String> colorJ4;

    @FXML private Label lblCPU;
    @FXML private Label lblError;

    // ==================== ESTADO ====================
    private int numJugadores = 2;

    private static final String[] COLORES = {"Azul", "Rojo", "Verde", "Amarillo"};

    // ==================== INICIALIZACIÓN ====================
    @FXML
    private void initialize() {
        // Rellenar los ComboBox de color
        colorJ1.getItems().addAll(COLORES);
        colorJ2.getItems().addAll(COLORES);
        colorJ3.getItems().addAll(COLORES);
        colorJ4.getItems().addAll(COLORES);

        // Valores por defecto (colores distintos)
        colorJ1.setValue("Azul");
        colorJ2.setValue("Rojo");
        colorJ3.setValue("Verde");
        colorJ4.setValue("Amarillo");

        // Por defecto: 2 jugadores
        actualizarFilas();
    }

    // ==================== SELECCIÓN Nº JUGADORES ====================
    @FXML
    private void seleccionar2(ActionEvent event) {
        numJugadores = 2;
        actualizarToggleButtons();
        actualizarFilas();
    }

    @FXML
    private void seleccionar3(ActionEvent event) {
        numJugadores = 3;
        actualizarToggleButtons();
        actualizarFilas();
    }

    @FXML
    private void seleccionar4(ActionEvent event) {
        numJugadores = 4;
        actualizarToggleButtons();
        actualizarFilas();
    }

    private void actualizarToggleButtons() {
        btn2J.getStyleClass().remove("toggle-active");
        btn3J.getStyleClass().remove("toggle-active");
        btn4J.getStyleClass().remove("toggle-active");

        switch (numJugadores) {
            case 2: btn2J.getStyleClass().add("toggle-active"); break;
            case 3: btn3J.getStyleClass().add("toggle-active"); break;
            case 4: btn4J.getStyleClass().add("toggle-active"); break;
        }
    }

    private void actualizarFilas() {
        setFilaActiva(filaJ1, true);
        setFilaActiva(filaJ2, true);
        setFilaActiva(filaJ3, numJugadores >= 3);
        setFilaActiva(filaJ4, numJugadores >= 4);
    }

    private void setFilaActiva(HBox fila, boolean activa) {
        fila.setDisable(!activa);
        if (activa) {
            fila.getStyleClass().remove("player-row-disabled");
        } else {
            if (!fila.getStyleClass().contains("player-row-disabled")) {
                fila.getStyleClass().add("player-row-disabled");
            }
        }
    }

    // ==================== CPU (FOCA) ====================
    @FXML
    private void toggleCPU(ActionEvent event) {
        boolean cpuActivada = chkCPU.isSelected();
        lblCPU.setVisible(cpuActivada);
        lblCPU.setManaged(cpuActivada);
    }

    // ==================== VALIDAR Y EMPEZAR ====================
    @FXML
    private void handleEmpezar(ActionEvent event) {
        // Recoger los datos
        TextField[] nombres = {nombreJ1, nombreJ2, nombreJ3, nombreJ4};
        ComboBox<String>[] colores = new ComboBox[]{colorJ1, colorJ2, colorJ3, colorJ4};

        // Validar que todos los jugadores activos tienen nombre y color
        for (int i = 0; i < numJugadores; i++) {
            String nombre = nombres[i].getText().trim();
            String color  = colores[i].getValue();

            if (nombre.isEmpty()) {
                mostrarError("El Jugador " + (i + 1) + " necesita un nombre.");
                return;
            }
            if (color == null || color.isEmpty()) {
                mostrarError("El Jugador " + (i + 1) + " necesita elegir un color.");
                return;
            }
        }

        // Validar colores únicos
        for (int i = 0; i < numJugadores; i++) {
            for (int j = i + 1; j < numJugadores; j++) {
                if (colores[i].getValue().equals(colores[j].getValue())) {
                    mostrarError("Dos jugadores no pueden tener el mismo color.");
                    return;
                }
            }
        }

        // Construir la lista de jugadores
        ArrayList<Jugador> jugadores = new ArrayList<>();
        for (int i = 0; i < numJugadores; i++) {
            String nombre = nombres[i].getText().trim();
            String color  = colores[i].getValue();
            jugadores.add(new Pinguino(0, nombre, color));
        }

        // Añadir CPU si está marcada
        if (chkCPU.isSelected()) {
            jugadores.add(new Foca(0, "Foca CPU", "Gris"));
        }

        // Pasar jugadores a PantallaPartida y lanzar el juego
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PantallaJuego.fxml"));
            Parent root = loader.load();

            PantallaPartida controlador = loader.getController();
            controlador.setJugadores(jugadores);

            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("El Juego del Pingüino - Partida");
            stage.show();
        } catch (Exception e) {
            mostrarError("Error al cargar el juego: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== VOLVER AL MENÚ ====================
    @FXML
    private void handleVolver(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PantallaPrincipal.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("El Juego del Pingüino - Menú Principal");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==================== AUXILIAR ====================
    private void mostrarError(String mensaje) {
        lblError.setText("⚠ " + mensaje);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }
}
