package com.nure.restaurant.data;

public class CartProduct extends Product {
    private double quantity;

    public CartProduct(int id, String name, double price, String measurement, int supplier, double quantity) {
        super(id, name, price, measurement, supplier);
        this.quantity = quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getQuantity() {
        return quantity;
    }
}

