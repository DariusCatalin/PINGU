package JOC_DEL_PINGU;

/**
 * ============================================================
 * CLASE: PantallaMenu  (controlador de PantallaMenu.fxml)
 * ============================================================
 * Controlador de la pantalla de Login. Es la primera pantalla
 * que ve el usuario al arrancar la aplicación.
 *
 * RESPONSABILIDAD:
 *   - Recoger usuario y contraseña del formulario.
 *   - Validar credenciales contra Oracle vía GestorBBDD.
 *   - Navegar al menú principal si el login es correcto.
 *   - Navegar a la pantalla de registro si el usuario pulsa
 *     "Crear cuenta".
 *
 * MÉTODOS @FXML:
 *   handleLogin(event)    → Llama a GestorBBDD.validarLogin().
 *                           Si ok → carga PantallaPrincipal.fxml.
 *                           Si falla → muestra error en lblError.
 *   handleRegister(event) → Carga PantallaRegistro.fxml.
 *
 * FLUJO DE SEGURIDAD:
 *   La contraseña nunca viaja en claro: GestorBBDD.hashPassword()
 *   la convierte a SHA-256 antes de enviarla al procedure Oracle
 *   VALIDAR_LOGIN(user, hash, OUT resultado).
 * ============================================================
 */
 
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
 
public class PantallaMenu {
 
    @FXML private TextField     userField;
    @FXML private PasswordField passField;
    @FXML private Button        loginButton;
    @FXML private Button        registerButton;
    @FXML private Label         lblError;
 
    @FXML
    private void initialize() {
        System.out.println("¡PantallaMenu inicializada, lista para jugar!");
    }
 
    // ==========================================
    // ACCIÓN: INICIAR SESIÓN
    // ==========================================
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = userField.getText().trim();
        String password = passField.getText();
 
        if (username.isEmpty() || password.isEmpty()) {
            mostrarError("⚠ Por favor, introduce tu usuario y contraseña.");
        } else {
            GestorBBDD gestor = new GestorBBDD();
            gestor.iniciarConexionGUI();
            boolean loginOk = gestor.validarLogin(username, password);
            gestor.cerrarConexion();
 
            if (!loginOk) {
                mostrarError("❌ Usuario o contraseña incorrectos.");
                passField.clear();
            } else {
                // Credenciales correctas → ir al menú principal
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaPrincipal.fxml"));
                    Parent root = loader.load();
 
                    // Escalamos al tamaño de pantalla real
                    Rectangle2D screen = Screen.getPrimary().getBounds();
                    Main.escalar(root,
                            Main.BASE_WIDTH_PRINCIPAL,
                            Main.BASE_HEIGHT_PRINCIPAL,
                            screen.getWidth(),
                            screen.getHeight());
 
                    Scene scene = new Scene(root, screen.getWidth(), screen.getHeight());
                    try { scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm()); } catch (Exception ignored) {}
 
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(scene);
                    stage.setTitle("El Juego del Pingüino - Menú Principal");
                    stage.setFullScreen(true);
                    stage.setFullScreenExitHint("");
                    stage.show();
 
                } catch (Exception e) {
                    mostrarError("❌ Error al cargar el menú principal: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
 
    // ==========================================
    // ACCIÓN: IR A REGISTRO
    // ==========================================
    @FXML
    private void handleRegister(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaRegistro.fxml"));
            Parent root = loader.load();
 
            Rectangle2D screen = Screen.getPrimary().getBounds();
            Scene scene = new Scene(root, screen.getWidth(), screen.getHeight());
            try { scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm()); } catch (Exception ignored) {}
 
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("El Juego del Pingüino - Registro");
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