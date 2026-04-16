package JOC_DEL_PINGU;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;

public class PantallaMenu {

    // --- Opciones del menú superior ---
    @FXML private MenuItem newGame;
    @FXML private MenuItem saveGame;
    @FXML private MenuItem loadGame;
    @FXML private MenuItem quitGame;

    // --- Campos de texto del Login ---
    @FXML private TextField     userField;
    @FXML private PasswordField passField;

    // --- Botones ---
    @FXML private Button loginButton;
    @FXML private Button registerButton;

    // --- Label de error ---
    @FXML private Label lblError;

    @FXML
    private void initialize() {
        System.out.println("¡PantallaMenu inicializada, lista para jugar!");
    }

    // ==========================================
    // ACCIONES DEL MENÚ SUPERIOR
    // ==========================================
    @FXML private void handleNewGame()  { System.out.println("Nueva partida seleccionada en el menú."); }
    @FXML private void handleSaveGame() { System.out.println("Guardar partida..."); }
    @FXML private void handleLoadGame() { System.out.println("Cargar partida..."); }

    @FXML
    private void handleQuitGame() {
        System.out.println("Saliendo del juego. ¡Nos vemos!");
        javafx.application.Platform.exit();
    }

    // ==========================================
    // ACCIONES DE LOGIN
    // ==========================================
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = userField.getText().trim();
        String password = passField.getText();

        // Validación de campos vacíos
        if (username.isEmpty() || password.isEmpty()) {
            mostrarError("⚠ Por favor, introduce tu usuario y contraseña.");
            return;
        }

        // Conexión y validación contra la BBDD
        GestorBBDD gestor = new GestorBBDD();
        gestor.iniciarConexionGUI();

        boolean loginOk = gestor.validarLogin(username, password);
        gestor.cerrarConexion();

        if (!loginOk) {
            mostrarError("❌ Usuario o contraseña incorrectos.");
            passField.clear();
            return;
        }

        // Credenciales correctas → ir al menú principal
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PantallaPrincipal.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            try { scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm()); } catch (Exception ignored) {}

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("El Juego del Pingüino - Menú Principal");
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.show();

        } catch (Exception e) {
            mostrarError("❌ Error al cargar el menú principal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==========================================
    // ACCIÓN DE REGISTRO → abre pantalla de registro
    // ==========================================
    @FXML
    private void handleRegister(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PantallaRegistro.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            try { scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm()); } catch (Exception ignored) {}

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("El Juego del Pingüino - Registro");
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.show();

        } catch (Exception e) {
            mostrarError("❌ Error al abrir la pantalla de registro: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==========================================
    // AUXILIAR
    // ==========================================
    private void mostrarError(String mensaje) {
        if (lblError != null) {
            lblError.setText(mensaje);
            lblError.setVisible(true);
            lblError.setManaged(true);
        }
    }
}
