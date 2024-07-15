module org.example.examdemo {
    requires javafx.controls;
    requires javafx.fxml;
    requires jdk.jdi;
    requires java.sql;


    opens org.example.examdemo to javafx.fxml;
    exports org.example.examdemo;
}