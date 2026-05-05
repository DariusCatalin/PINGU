package JOC_DEL_PINGU;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.ArrayList;

/**
 * Controlador de la pantalla de Ranking y Estadísticas.
 * Muestra todas las funciones PL/SQL del módulo del juego:
 *  - max_wins() → récord global
 *  - media_de_wins() → media de victorias
 *  - jugadores_con_record() → quién tiene el récord
 *  - jugadores_sobre_la_media_wins() → quién está por encima de la media
 *  - ranking → tabla ordenada por partidas jugadas
 */
public class PantallaRanking {

    // ==================== FXML ====================
    @FXML private Label lblRecord;
    @FXML private Label lblMedia;
    @FXML private Label lblTotalJugadores;
    @FXML private Label lblRecordistas;
    @FXML private Label lblSobreMedia;

    @FXML private TableView<JugadorRanking> tablaRanking;
    @FXML private TableColumn<JugadorRanking, String> colPosicion;
    @FXML private TableColumn<JugadorRanking, String> colNombre;
    @FXML private TableColumn<JugadorRanking, String> colPartidas;
    @FXML private TableColumn<JugadorRanking, String> colGanadas;

    // ==================== INICIALIZACIÓN ====================
    @FXML
    private void initialize() {
        // Configurar columnas de la tabla
        colPosicion.setCellValueFactory(new PropertyValueFactory<>("posicion"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPartidas.setCellValueFactory(new PropertyValueFactory<>("partidasJugadas"));
        colGanadas.setCellValueFactory(new PropertyValueFactory<>("partidasGanadas"));

        // Cargar datos al abrir la pantalla
        cargarEstadisticas();
    }

    // ==================== CARGA DE DATOS ====================
    private void cargarEstadisticas() {
        GestorBBDD gestor = new GestorBBDD();
        try {
            gestor.iniciarConexionGUI();

            if (gestor.getConexion() == null) {
                lblRecord.setText("⚠️ Sin conexión a la BBDD");
                return;
            }

            // 1. Récord global
            int record = gestor.obtenerRecord();
            lblRecord.setText("🥇 Récord global: " + record + " partidas ganadas");

            // 2. Media de victorias
            double media = gestor.obtenerMediaWins();
            lblMedia.setText("📈 Media de victorias: " + media + " por jugador");

            // 3. Total de jugadores registrados (llama a function TOTAL_JUGADORES de Oracle)
            int total = gestor.obtenerTotalJugadores();
            lblTotalJugadores.setText("👥 Jugadores registrados: " + total);

            // 4. Ranking por partidas jugadas
            ArrayList<String[]> ranking = gestor.obtenerRanking();
            ObservableList<JugadorRanking> data = FXCollections.observableArrayList();
            int pos = 1;
            for (String[] fila : ranking) {
                String simbolo;
                switch (pos) {
                    case 1: simbolo = "🥇"; break;
                    case 2: simbolo = "🥈"; break;
                    case 3: simbolo = "🥉"; break;
                    default: simbolo = "" + pos;
                }
                data.add(new JugadorRanking(simbolo, fila[0], fila[1], fila[2]));
                pos++;
            }
            tablaRanking.setItems(data);

            // 5. Jugadores con el récord actual
            if (record > 0) {
                ArrayList<String[]> recordistas = gestor.obtenerJugadoresConRecord(record);
                StringBuilder sb = new StringBuilder();
                for (String[] j : recordistas) {
                    if (sb.length() > 0) sb.append(", ");
                    sb.append("🏆 ").append(j[0]).append(" (").append(j[1]).append(" victorias)");
                }
                lblRecordistas.setText(sb.length() > 0 ? sb.toString() : "Aún no hay récords");
            } else {
                lblRecordistas.setText("Aún no se ha jugado ninguna partida.");
            }

            // 6. Jugadores sobre la media
            ArrayList<String[]> sobreMedia = gestor.obtenerJugadoresSobreMedia();
            StringBuilder sb2 = new StringBuilder();
            for (String[] j : sobreMedia) {
                if (sb2.length() > 0) sb2.append("\n");
                sb2.append("• ").append(j[0]).append(": ").append(j[1]).append(" victorias");
            }
            lblSobreMedia.setText(sb2.length() > 0 ? sb2.toString() : "Nadie supera la media todavía.");

            gestor.cerrarConexion();
            System.out.println("✅ Estadísticas cargadas correctamente.");

        } catch (Exception e) {
            System.err.println("❌ Error cargando estadísticas: " + e.getMessage());
            try { gestor.cerrarConexion(); } catch (Exception ignored) {}
        }
    }

    // ==================== BOTONES ====================
    @FXML
    private void handleRefrescar(ActionEvent event) {
        cargarEstadisticas();
    }

    @FXML
    private void handleVolver(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaPrincipal.fxml"));
            Parent root = loader.load();
            Rectangle2D screen = Screen.getPrimary().getBounds();
            Scene scene = new Scene(root, screen.getWidth(), screen.getHeight());
            try {
                scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            } catch (Exception ignored) {}

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("El Juego del Pingüino - Menú Principal");
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==================== CLASE INTERNA PARA LA TABLA ====================
    public static class JugadorRanking {
        private final SimpleStringProperty posicion;
        private final SimpleStringProperty nombre;
        private final SimpleStringProperty partidasJugadas;
        private final SimpleStringProperty partidasGanadas;

        public JugadorRanking(String posicion, String nombre, String partidasJugadas, String partidasGanadas) {
            this.posicion = new SimpleStringProperty(posicion != null ? posicion : "");
            this.nombre = new SimpleStringProperty(nombre != null ? nombre : "-");
            this.partidasJugadas = new SimpleStringProperty(partidasJugadas != null ? partidasJugadas : "0");
            this.partidasGanadas = new SimpleStringProperty(partidasGanadas != null ? partidasGanadas : "0");
        }

        public String getPosicion() { return posicion.get(); }
        public String getNombre() { return nombre.get(); }
        public String getPartidasJugadas() { return partidasJugadas.get(); }
        public String getPartidasGanadas() { return partidasGanadas.get(); }
    }
}
