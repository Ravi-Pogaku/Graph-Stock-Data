module com.example.graphwebdata {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.graphwebdata to javafx.fxml;
    exports com.example.graphwebdata;
}