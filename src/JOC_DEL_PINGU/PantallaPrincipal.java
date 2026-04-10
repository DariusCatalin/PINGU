package JOC_DEL_PINGU;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Alert;
import java.util.ArrayList;
import java.util.Optional;

public class PantallaPrincipal {

    @FXML private Button btnNuevaPartida;
    @FXML private Button btnCargarPartida;
    @FXML private Button btnSalir;

    @FXML
    private void handleNuevaPartida(ActionEvent event) {
        try {
            System.out.println("Abriendo configuración de nueva partida...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PantallaConfig.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            try { scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm()); } catch(Exception ignored){}
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("El Juego del Pingüino - Nueva Partida");
            stage.show();
            javafx.application.Platform.runLater(() -> stage.setMaximized(true));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
                    
                    PantallaPartida controller = loader.getController();
                    // Pasarle la partida directamente al controlador del tablero
                    controller.setPartidaCargada(p);

                    Scene scene = new Scene(root);
                    try { scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm()); } catch(Exception ignored){}
                    
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.setScene(scene);
                    stage.setTitle("El Juego del Pingüino - ID Partida: " + idPartida);
                    stage.show();
                    javafx.application.Platform.runLater(() -> stage.setMaximized(true));
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

    @FXML
    private void handleCerrarSesion(ActionEvent event) {
        try {
            System.out.println("Cerrando sesión...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PantallaMenu.fxml")); // Asumiendo este nombre para el login
            Parent root = loader.load();
            Scene scene = new Scene(root);
            try { scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm()); } catch(Exception ignored){}
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("El Juego del Pingüino - Login");
            stage.show();
            javafx.application.Platform.runLater(() -> stage.setMaximized(true));
        } catch (Exception e) {
            // Si el nombre es diferente, intentamos con el recurso correcto.
            // A veces el login se llama de otra forma en FXML.
            System.out.println("Error volviendo al login: " + e.getMessage());
        }
    }
}
