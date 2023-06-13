module com.example.demo_extract {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.swing;
    requires org.apache.pdfbox;
    requires itextpdf;


    opens com.example.demo_extract to javafx.fxml;
    exports com.example.demo_extract;
}