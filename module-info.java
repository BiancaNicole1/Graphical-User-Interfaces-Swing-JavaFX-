module org.example.hygyhgvkju {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens org.example.hygyhgvkju to javafx.fxml;
    exports org.example.hygyhgvkju;
}