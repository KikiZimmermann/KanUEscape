module at.ac.hcw.kanuescape {

    requires javafx.controls;
    requires javafx.graphics;
    requires java.desktop;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;


    opens at.ac.hcw.kanuescape to javafx.fxml;
    exports at.ac.hcw.kanuescape;
}