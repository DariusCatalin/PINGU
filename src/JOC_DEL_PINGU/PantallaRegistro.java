package JOC_DEL_PINGU;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class PantallaRegistro {

    @FXML private TextField     userField;
    @FXML private PasswordField passField;
    @FXML private PasswordField passConfirmField;
    @FXML private Label         lblMensaje;

    // ==========================================
    // REGISTRAR NUEVO USUARIO
    // ==========================================
    @FXML
    private void handleRegistrar(ActionEvent event) {
        String username = userField.getText().trim();
        String password = passField.getText();
        String confirm  = passConfirmField.getText();

        // --- Validaciones locales ---
        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            mostrarError("⚠ Todos los campos son obligatorios.");
            return;
        }
        if (username.length() < 3) {
            mostrarError("⚠ El nombre de usuario debe tener al menos 3 caracteres.");
            return;
        }
        if (password.length() < 4) {
            mostrarError("⚠ La contraseña debe tener al menos 4 caracteres.");
            return;
        }
        if (!password.equals(confirm)) {
            mostrarError("⚠ Las contraseñas no coinciden.");
            passField.clear();
            passConfirmField.clear();
            return;
        }

        // --- Intento de registro en BBDD ---
        GestorBBDD gestor = new GestorBBDD();
        gestor.iniciarConexionGUI();
        String resultado = gestor.registrarUsuario(username, password);
        gestor.cerrarConexion();

        if (resultado == null) {
            // ✅ Registro exitoso → login automático, ir directamente al menú principal
            mostrarExito("✅ ¡Cuenta creada! Entrando al juego...");
            // Espera breve para que el usuario vea el mensaje, luego navega
            javafx.animation.PauseTransition pausa = new javafx.animation.PauseTransition(
                    javafx.util.Duration.seconds(1.2));
            pausa.setOnFinished(e -> irAlMenuPrincipal(event));
            pausa.play();

        } else if ("DUPLICATE".equals(resultado)) {
            // ❌ Username ya existe
            mostrarError("❌ El nombre de usuario \"" + username + "\" ya está en uso. Elige otro.");
            userField.clear();
            passField.clear();
            passConfirmField.clear();
            userField.requestFocus();

        } else {
            // ❌ Otro error de BD (conexión, procedimiento no encontrado, etc.)
            String detalle = resultado.startsWith("ERROR:") ? resultado.substring(6) : resultado;
            mostrarError("❌ Error al registrar: " + detalle);
            System.err.println("Error registro BD: " + detalle);
        }
    }

    // ==========================================
    // VOLVER AL LOGIN
    // ==========================================
    @FXML
    private void handleVolver(ActionEvent event) {
        irAlLogin(event);
    }

    /** Después del registro exitoso va directamente al menú principal (auto-login). */
    private void irAlMenuPrincipal(ActionEvent event) {
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
            mostrarError("❌ Error al cargar el menú: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void irAlLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PantallaMenu.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            try { scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm()); } catch (Exception ignored) {}

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("El Juego del Pingüino - Login");
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.show();

        } catch (Exception e) {
            mostrarError("❌ Error al volver al login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==========================================
    // AUXILIARES
    // ==========================================
    private void mostrarError(String mensaje) {
        if (lblMensaje != null) {
            lblMensaje.getStyleClass().remove("success-label");
            if (!lblMensaje.getStyleClass().contains("error-label")) {
                lblMensaje.getStyleClass().add("error-label");
            }
            lblMensaje.setText(mensaje);
            lblMensaje.setVisible(true);
            lblMensaje.setManaged(true);
        }
    }

    private void mostrarExito(String mensaje) {
        if (lblMensaje != null) {
            lblMensaje.getStyleClass().remove("error-label");
            if (!lblMensaje.getStyleClass().contains("success-label")) {
                lblMensaje.getStyleClass().add("success-label");
            }
            lblMensaje.setText(mensaje);
            lblMensaje.setVisible(true);
            lblMensaje.setManaged(true);
        }
    }
}
