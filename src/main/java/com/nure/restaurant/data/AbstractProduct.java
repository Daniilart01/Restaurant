package com.nure.restaurant.data;

public abstract class AbstractProduct {
    private int id;
    private int product_id;
    private double quantity;
    private String measurement;

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProduct_id() {
        return product_id;
    }

    public void setProduct_id(int product_id) {
        this.product_id = product_id;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getMeasurement() {
        return measurement;
    }

    public void setMeasurement(String measurement) {
        this.measurement = measurement;
    }

    public AbstractProduct(int id, int product_id, double quantity, String measurement, String name) {
        this.id = id;
        this.product_id = product_id;
        this.quantity = quantity;
        this.measurement = measurement;
        this.name = name;
    }
}
