module at.ac.hcw.kanuescape {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires java.xml;
    requires javafx.graphics;
    requires at.ac.hcw.kanuescape;


    exports at.ac.hcw.kanuescape;
    opens at.ac.hcw.kanuescape.controller to javafx.fxml;
    opens at.ac.hcw.kanuescape.tiled to com.fasterxml.jackson.databind;
}
