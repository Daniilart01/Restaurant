package com.nure.restaurant.data;

import java.sql.Date;
import java.time.LocalDate;

public class WarehouseProduct extends AbstractProduct {
    private double price;
    private LocalDate expiry_date;


    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public LocalDate getExpiry_date() {
        return expiry_date;
    }

    public void setExpiry_date(LocalDate expiry_date) {
        this.expiry_date = expiry_date;
    }

    public WarehouseProduct(int id, int product_id, double quantity, String measurement, String name, double price, Date expiry_date) {
        super(id, product_id, quantity, measurement, name);
        this.price = price;
        this.expiry_date = LocalDate.ofEpochDay(expiry_date.getTime()/1000/60/60/24+1);

    }

    @Override
    public String toString() {
        return this.getName();
    }
}
