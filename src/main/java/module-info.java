module org.iad.mlp {
    requires javafx.controls;
    requires javafx.fxml;
    requires univocity.parsers;

    opens org.iad.mlp to javafx.fxml;
    exports org.iad.mlp;
}