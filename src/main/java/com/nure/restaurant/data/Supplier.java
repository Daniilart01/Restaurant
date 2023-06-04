package com.nure.restaurant.data;

public record Supplier(int id, String name) {
    @Override
    public String toString() {
        return id+". "+name;
    }
}
