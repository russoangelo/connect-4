module com.connectfour.connect4 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.connectfour.connect4 to javafx.fxml;
    exports com.connectfour.connect4;
}