package com.nure.restaurant.data;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class OrderProduct {
    private final int id;
    private final SimpleStringProperty name;
    private final SimpleDoubleProperty price;
    private final SimpleStringProperty measurement;
    private final SimpleDoubleProperty quantity;

    public OrderProduct(int id, String name, double price, String measurement, double quantity) {
        this.id = id;
        this.name = new SimpleStringProperty(name);
        this.price = new SimpleDoubleProperty(price);
        this.measurement = new SimpleStringProperty(measurement);
        this.quantity = new SimpleDoubleProperty(quantity);
    }

    public int getID() {
        return id;
    }

    public String getName() {
        return name.get();
    }

    public double getPrice() {
        return price.get();
    }

    public String getMeasurement() {
        return measurement.get();
    }

    public double getQuantity() {
        return quantity.get();
    }

    @Override
    public String toString() {
        return name.get() + ", " + quantity.get() + measurement.get() + ", " + (Math.round(quantity.get() * price.get() * 100.0) / 100.0) + "UAH";
    }
}