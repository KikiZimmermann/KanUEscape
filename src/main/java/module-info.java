module com.demo.kanuescape {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.demo.kanuescape to javafx.fxml;
    exports com.demo.kanuescape;
}