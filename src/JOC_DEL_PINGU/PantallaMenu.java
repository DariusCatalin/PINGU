package JOC_DEL_PINGU; // Adaptado a tu paquete real

import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
    @FXML private TextField userField;
    @FXML private PasswordField passField;

    // --- Botones ---
    @FXML private Button loginButton;
    @FXML private Button registerButton;

    // Si más adelante necesitas conectar a la base de datos para validar el login:
    // private GestorBBDD gestorBBDD = new GestorBBDD();

    @FXML
    private void initialize() {
        System.out.println("¡PantallaMenu inicializada, lista para jugar tt!");
    }

    // ==========================================
    // ACCIONES DEL MENÚ SUPERIOR
    // ==========================================
    @FXML
    private void handleNewGame() {
        System.out.println("Nueva partida seleccionada en el menú.");
        // TODO: Lógica adicional si quieres que el menú superior también inicie juego
    }

    @FXML
    private void handleSaveGame() {
        System.out.println("Guardar partida...");
    }

    @FXML
    private void handleLoadGame() {
        System.out.println("Cargar partida...");
    }

    @FXML
    private void handleQuitGame() {
        System.out.println("Saliendo del juego. ¡Nos vemos tt!");
        System.exit(0);
    }
    
    // ==========================================
    // ACCIONES DE LOGIN Y REGISTRO
    // ==========================================
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = userField.getText();
        String password = passField.getText();

        System.out.println("Intento de Login -> Usuario: " + username + " / Pass: " + password);

        // Comprobación básica (Aquí en el futuro llamarás a gestorBBDD para validar)
        if (username != null && !username.trim().isEmpty() && password != null && !password.trim().isEmpty()) {
            try {
                // OJO: Esta ruta debe coincidir con dónde tienes guardado el diseño de la partida
                // Si PantallaJuego.fxml y PantallaMenu.fxml están en la misma carpeta, quítale la barra inicial:
                // getClass().getResource("PantallaJuego.fxml")
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/PantallaJuego.fxml"));
                Parent pantallaJuegoRoot = loader.load();
                Scene pantallaJuegoScene = new Scene(pantallaJuegoRoot);

                // Capturamos la ventana actual a través del botón que acabamos de pulsar
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                
                // Cambiamos a la pantalla de la partida
                stage.setScene(pantallaJuegoScene);
                stage.setTitle("Joc d'en Pingu - Partida Activa");
                stage.show();
                
            } catch (Exception e) {
                System.out.println("¡Error al intentar cargar la vista del juego!");
                e.printStackTrace();
            }
        } else {
            System.out.println("Faltan datos: Por favor, introduce tu usuario y contraseña.");
        }
    }

    @FXML
    private void handleRegister() {
        System.out.println("Botón de Registro pulsado.");
        // TODO: Aquí recogerías userField y passField para hacer un INSERT en tu Base de Datos
    }
}
