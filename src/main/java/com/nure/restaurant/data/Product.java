package com.nure.restaurant.data;

public class Product {
    private final int id;
    private final String name;
    private final double price;
    private final String measurement;
    private final int supplier;

    public Product(int id, String name, double price, String measurement, int supplier) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.measurement = measurement;
        this.supplier = supplier;
    }
    @Override
    public String toString() {
        return name;
    }

    public int id() {
        return id;
    }

    public String name() {
        return name;
    }

    public double price() {
        return price;
    }

    public String measurement() {
        return measurement;
    }

    public int supplier() {
        return supplier;
    }

}
