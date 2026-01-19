//package at.ac.hcw.kanuescape.controller.ui;
//
//import javafx.fxml.FXML;
//import javafx.scene.layout.AnchorPane;
//
//public class MenuOverlayController {
//
//    @FXML private AnchorPane overlay;
//
//    public void show() {
//        overlay.setVisible(true);
//    }
//
//    public void hide() {
//        overlay.setVisible(false);
//    }
//
//    @FXML
//    private void onResume() {
//        hide();
//    }
//
//    @FXML
//    private void onQuit() {
//        System.exit(0);
//    }
//
//    private Runnable onShowEndScreen;
//
//    public void setOnShowEndScreen(Runnable r) {
//        this.onShowEndScreen = r;
//    }
//
//    @FXML
//    private void onShowEndScreen() {
//        if (onShowEndScreen != null) onShowEndScreen.run();
//    }
//
//}
