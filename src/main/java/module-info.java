module org.iad.mlp {
    requires javafx.controls;
    requires javafx.fxml;

    opens org.iad.mlp to javafx.fxml;
    exports org.iad.mlp;
}