package JOC_DEL_PINGU;
 
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
 
public class Main extends Application {
 
    // Tamaño de diseño base (el de los FXML)
    public static final double BASE_WIDTH_MENU      = 1170;
    public static final double BASE_HEIGHT_MENU     = 658;
    public static final double BASE_WIDTH_PRINCIPAL = 1330;
    public static final double BASE_HEIGHT_PRINCIPAL= 850;
 
    // ==================== MÈTODE START ====================
 
    @Override
    public void start(Stage primaryStage) {
        try {
            // Tamaño real de la pantalla
            Rectangle2D screen = Screen.getPrimary().getBounds();
            double sw = screen.getWidth();
            double sh = screen.getHeight();
 
            // Cargamos el menú de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/PantallaMenu.fxml"));
            Parent root = loader.load();
 
            // Escalamos el contenedor interno al tamaño de pantalla
            escalar(root, BASE_WIDTH_MENU, BASE_HEIGHT_MENU, sw, sh);
 
            Scene scene = new Scene(root, sw, sh);
            try {
                scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            } catch (Exception ignored) {}
 
            primaryStage.setTitle("El Joc del Pingu");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.setMaximized(true);
            primaryStage.setFullScreen(true);
            primaryStage.setFullScreenExitHint("");
            primaryStage.show();
 
            System.out.println("✅ Aplicació iniciada correctament!");
 
        } catch (Exception e) {
            System.out.println("❌ Error al carregar la finestra:");
            e.printStackTrace();
        }
    }
 
    // ==================== MÈTODE ESCALAR ====================
 
    /**
     * Busca el AnchorPane con fx:id="contenedor" dentro del StackPane raíz
     * y le aplica un scale para que ocupe toda la pantalla manteniendo proporciones.
     */
    public static void escalar(Parent root, double baseW, double baseH,
                                double screenW, double screenH) {
        if (!(root instanceof StackPane)) return;
        StackPane stack = (StackPane) root;
 
        // Busca el AnchorPane "contenedor"
        stack.getChildren().stream()
             .filter(n -> n instanceof AnchorPane && "contenedor".equals(n.getId()))
             .findFirst()
             .ifPresent(node -> {
                 double scaleX = screenW / baseW;
                 double scaleY = screenH / baseH;
                 // Usamos el menor para no deformar, o ambos para ocupar todo
                 // Cambia a Math.min(scaleX, scaleY) si prefieres letterbox sin deformar
                 node.setScaleX(scaleX);
                 node.setScaleY(scaleY);
             });
    }
 
    // ==================== MÈTODE MAIN ====================
 
    public static void main(String[] args) {
        launch(args);
    }
}