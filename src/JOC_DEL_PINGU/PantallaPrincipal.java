package JOC_DEL_PINGU;
 
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.stage.Screen;
import javafx.stage.Stage;
 
import java.util.ArrayList;
import java.util.Optional;
 
public class PantallaPrincipal {
 
    @FXML private Button btnNuevaPartida;
    @FXML private Button btnCargarPartida;
    @FXML private Button btnVolverLogin;
 
    // ==========================================
    // AUXILIAR: obtener pantalla y stage
    // ==========================================
    private Rectangle2D getScreen() {
        return Screen.getPrimary().getBounds();
    }
 
    private Stage getStage(ActionEvent event) {
        return (Stage) ((Node) event.getSource()).getScene().getWindow();
    }
 
    private void aplicarFullScreen(Stage stage, String title) {
        stage.setTitle(title);
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        stage.show();
    }
 
    // ==========================================
    // ACCIÓN: NUEVA PARTIDA
    // ==========================================
    @FXML
    private void handleNuevaPartida(ActionEvent event) {
        try {
            System.out.println("Abriendo configuración de nueva partida...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PantallaConfig.fxml"));
            Parent root = loader.load();
 
            Rectangle2D screen = getScreen();
            Scene scene = new Scene(root, screen.getWidth(), screen.getHeight());
            try { scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm()); } catch (Exception ignored) {}
 
            Stage stage = getStage(event);
            stage.setScene(scene);
            aplicarFullScreen(stage, "El Juego del Pingüino - Nueva Partida");
 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    // ==========================================
    // ACCIÓN: CARGAR PARTIDA
    // ==========================================
    @FXML
    private void handleCargarPartida(ActionEvent event) {
        System.out.println("Buscando partidas en la BBDD...");
 
        GestorBBDD gestor = new GestorBBDD();
        gestor.iniciarConexionGUI();
        ArrayList<Integer> partidas = gestor.obtenerListaPartidas();
 
        if (partidas == null || partidas.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Cargar Partida");
            alert.setHeaderText("No hay partidas guardadas");
            alert.setContentText("Actualmente no hay ninguna partida guardada en la base de datos.");
            alert.showAndWait();
            gestor.cerrarConexion();
            return;
        }
 
        ChoiceDialog<Integer> dialog = new ChoiceDialog<>(partidas.get(0), partidas);
        dialog.setTitle("Cargar Partida");
        dialog.setHeaderText("Selector de Partidas Guardadas");
        dialog.setContentText("Selecciona el ID de la partida que quieres cargar:");
 
        Optional<Integer> result = dialog.showAndWait();
        result.ifPresent(idPartida -> {
            Partida p = gestor.cargarBBDD(idPartida);
            if (p != null) {
                try {
                    System.out.println("Partida " + idPartida + " cargada. Abriendo el tablero...");
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/PantallaJuego.fxml"));
                    Parent root = loader.load();
 
                    Rectangle2D screen = getScreen();
                    Scene scene = new Scene(root, screen.getWidth(), screen.getHeight());
                    try { scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm()); } catch (Exception ignored) {}
 
                    Stage stage = getStage(event);
                    stage.setScene(scene);
                    aplicarFullScreen(stage, "El Juego del Pingüino - ID Partida: " + idPartida);
                    stage.requestFocus();
 
                    PantallaPartida controller = loader.getController();
                    controller.setPartidaCargada(p);
 
                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error al Cargar");
                    alert.setHeaderText("Error cargando la vista de la partida");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                    e.printStackTrace();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error de carga");
                alert.setContentText("No se pudo regenerar la partida " + idPartida + " desde la BBDD.");
                alert.showAndWait();
            }
        });
 
        gestor.cerrarConexion();
    }
 
    // ==========================================
    // ACCIÓN: VOLVER AL LOGIN
    // ==========================================
    @FXML
    private void handleVolverLogin(ActionEvent event) {
        try {
            System.out.println("Cerrando sesión...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaMenu.fxml"));
            Parent root = loader.load();
 
            // Escalamos al tamaño de pantalla real
            Rectangle2D screen = getScreen();
            Main.escalar(root,
                    Main.BASE_WIDTH_MENU,
                    Main.BASE_HEIGHT_MENU,
                    screen.getWidth(),
                    screen.getHeight());
 
            Scene scene = new Scene(root, screen.getWidth(), screen.getHeight());
            try { scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm()); } catch (Exception ignored) {}
 
            Stage stage = getStage(event);
            stage.setScene(scene);
            aplicarFullScreen(stage, "El Juego del Pingüino - Login");
 
        } catch (Exception e) {
            System.out.println("Error volviendo al login: " + e.getMessage());
            e.printStackTrace();
        }
    }
}