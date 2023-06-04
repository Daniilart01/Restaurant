module com.nure.restaurant {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires ojdbc10;
    requires org.apache.commons.codec;
    requires AnimateFX;
    requires java.desktop;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    opens com.nure.restaurant to javafx.fxml;
    exports com.nure.restaurant;
    exports com.nure.restaurant.data;
    opens com.nure.restaurant.data to javafx.fxml;
    exports com.nure.restaurant.dataWorkers;
    opens com.nure.restaurant.dataWorkers to javafx.fxml;
}