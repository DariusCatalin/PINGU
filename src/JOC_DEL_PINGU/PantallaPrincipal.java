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

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
 
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
        mostrarVentanaPartidas(event);
    }

    /**
     * Abre una ventana con tabla mostrando todas las partidas guardadas.
     * Permite cargar o eliminar partidas individualmente.
     */
    private void mostrarVentanaPartidas(ActionEvent event) {
        GestorBBDD gestor = new GestorBBDD();
        gestor.iniciarConexionGUI();
        ArrayList<String[]> partidas = gestor.obtenerListaPartidasCompleta();

        if (partidas == null || partidas.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Cargar Partida");
            alert.setHeaderText("No hay partidas guardadas");
            alert.setContentText("Actualmente no hay ninguna partida guardada en la base de datos.");
            alert.showAndWait();
            gestor.cerrarConexion();
            return;
        }

        // Crear el diálogo con tabla
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Gestor de Partidas Guardadas");
        dialog.setHeaderText("Selecciona una partida para cargarla o eliminarla");

        // Tabla con datos de partidas
        TableView<PartidaInfo> tabla = new TableView<>();
        tabla.setPrefSize(700, 400);

        TableColumn<PartidaInfo, String> colId = new TableColumn<>("ID Partida");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(100);

        TableColumn<PartidaInfo, String> colCreacion = new TableColumn<>("Fecha Creación");
        colCreacion.setCellValueFactory(new PropertyValueFactory<>("creacion"));
        colCreacion.setPrefWidth(150);

        TableColumn<PartidaInfo, String> colModificacion = new TableColumn<>("Última Modificación");
        colModificacion.setCellValueFactory(new PropertyValueFactory<>("modificacion"));
        colModificacion.setPrefWidth(150);

        TableColumn<PartidaInfo, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colEstado.setPrefWidth(100);

        TableColumn<PartidaInfo, String> colGanador = new TableColumn<>("Ganador");
        colGanador.setCellValueFactory(new PropertyValueFactory<>("ganador"));
        colGanador.setPrefWidth(150);

        tabla.getColumns().add(colId);
        tabla.getColumns().add(colCreacion);
        tabla.getColumns().add(colModificacion);
        tabla.getColumns().add(colEstado);
        tabla.getColumns().add(colGanador);

        ObservableList<PartidaInfo> data = FXCollections.observableArrayList();
        for (String[] fila : partidas) {
            data.add(new PartidaInfo(fila[0], fila[1], fila[2], fila[3], fila[4]));
        }
        tabla.setItems(data);

        // Botones personalizados
        Button btnCargar = new Button("🎮 Cargar partida");
        Button btnEliminar = new Button("🗑️ Eliminar partida");
        Button btnRefrescar = new Button("🔄 Refrescar");
        btnCargar.setDisable(true);
        btnEliminar.setDisable(true);

        // Habilitar botones solo cuando hay selección
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean haySeleccion = (newSel != null);
            btnCargar.setDisable(!haySeleccion);
            btnEliminar.setDisable(!haySeleccion);
        });

        // Acción del botón Eliminar
        btnEliminar.setOnAction(e -> {
            PartidaInfo seleccionada = tabla.getSelectionModel().getSelectedItem();
            if (seleccionada == null) return;

            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar eliminación");
            confirmacion.setHeaderText("¿Eliminar partida " + seleccionada.getId() + "?");
            confirmacion.setContentText("Esta acción no se puede deshacer.");

            Optional<ButtonType> resp = confirmacion.showAndWait();
            if (resp.isPresent() && resp.get() == ButtonType.OK) {
                try {
                    int idEliminar = Integer.parseInt(seleccionada.getId());
                    boolean exito = gestor.eliminarPartida(idEliminar);
                    if (exito) {
                        data.remove(seleccionada);
                        Alert ok = new Alert(Alert.AlertType.INFORMATION);
                        ok.setTitle("Eliminada");
                        ok.setHeaderText("Partida " + idEliminar + " eliminada correctamente.");
                        ok.showAndWait();
                    } else {
                        Alert err = new Alert(Alert.AlertType.ERROR);
                        err.setTitle("Error");
                        err.setHeaderText("No se pudo eliminar la partida.");
                        err.showAndWait();
                    }
                } catch (NumberFormatException ex) {
                    System.err.println("ID inválido: " + seleccionada.getId());
                }
            }
        });

        // Acción del botón Refrescar
        btnRefrescar.setOnAction(e -> {
            ArrayList<String[]> nuevas = gestor.obtenerListaPartidasCompleta();
            data.clear();
            for (String[] fila : nuevas) {
                data.add(new PartidaInfo(fila[0], fila[1], fila[2], fila[3], fila[4]));
            }
        });

        // Layout
        HBox botones = new HBox(10, btnCargar, btnEliminar, btnRefrescar);
        botones.setPadding(new Insets(10, 0, 0, 0));

        VBox contenido = new VBox(10, new Label("Partidas guardadas:"), tabla, botones);
        contenido.setPadding(new Insets(10));

        dialog.getDialogPane().setContent(contenido);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // Prevenir ClassCastException al cerrar con la X o el botón CLOSE
        dialog.setResultConverter(dialogButton -> {
            return null;
        });

        // Acción del botón Cargar (cierra el diálogo y carga)
        btnCargar.setOnAction(e -> {
            PartidaInfo seleccionada = tabla.getSelectionModel().getSelectedItem();
            if (seleccionada == null) return;
            dialog.setResult(new String[] { seleccionada.getId() });
            dialog.close();
        });

        Optional<String[]> resultado = dialog.showAndWait();

        // Si se eligió cargar una partida, cargarla
        resultado.ifPresent(arr -> {
            try {
                int idPartida = Integer.parseInt(arr[0]);
                Partida p = gestor.cargarBBDD(idPartida);
                if (p != null) {
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
                    controller.setIdPartidaCargada(idPartida); // ⭐ Pasar el ID para que se actualice al guardar
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error de carga");
                    alert.setContentText("No se pudo regenerar la partida " + arr[0] + " desde la BBDD.");
                    alert.showAndWait();
                }
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error al Cargar");
                alert.setHeaderText("Error cargando la vista de la partida");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
                ex.printStackTrace();
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

    /**
     * Clase interna para representar las filas de la tabla de partidas.
     * Necesita getters públicos para que JavaFX TableView los lea.
     */
    public static class PartidaInfo {
        private final SimpleStringProperty id;
        private final SimpleStringProperty creacion;
        private final SimpleStringProperty modificacion;
        private final SimpleStringProperty estado;
        private final SimpleStringProperty ganador;

        public PartidaInfo(String id, String creacion, String modificacion, String estado, String ganador) {
            this.id = new SimpleStringProperty(id != null ? id : "");
            this.creacion = new SimpleStringProperty(creacion != null ? creacion : "-");
            this.modificacion = new SimpleStringProperty(modificacion != null ? modificacion : "-");
            this.estado = new SimpleStringProperty(estado != null ? estado : "-");
            this.ganador = new SimpleStringProperty(ganador != null ? ganador : "-");
        }

        public String getId() { return id.get(); }
        public String getCreacion() { return creacion.get(); }
        public String getModificacion() { return modificacion.get(); }
        public String getEstado() { return estado.get(); }
        public String getGanador() { return ganador.get(); }
    }
}