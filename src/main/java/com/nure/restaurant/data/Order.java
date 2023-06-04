package com.nure.restaurant.data;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Date;

public class Order {
    private final SimpleIntegerProperty id;
    private final SimpleObjectProperty<Date> orderDate;
    private final SimpleDoubleProperty totalCost;
    private final SimpleIntegerProperty supplierID;
    private final ObservableList<OrderProduct> products;

    public Order(int id, Date orderDate, double totalCost, int supplierID) {
        this.id = new SimpleIntegerProperty(id);
        this.orderDate = new SimpleObjectProperty<>(orderDate);
        this.totalCost = new SimpleDoubleProperty(totalCost);
        this.supplierID = new SimpleIntegerProperty(supplierID);
        this.products = FXCollections.observableArrayList();
    }

    public int getId() {
        return id.get();
    }

    public Date getOrderDate() {
        return orderDate.get();
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate.set(orderDate);
    }

    public double getTotalCost() {
        return totalCost.get();
    }

    public int getSupplierID() {
        return supplierID.get();
    }

    public ObservableList<OrderProduct> getProducts() {
        return products;
    }
}
