package com.nure.restaurant.data;

import java.sql.Date;
import java.time.LocalDate;

public class WriteOffProduct extends AbstractProduct {
    private LocalDate writeoff_date;
    private String reason;

    public WriteOffProduct(int id, int product_id, double quantity, String measurement, String name, Date writeoff_date, String reason) {
        super(id, product_id, quantity, measurement, name);
        this.reason = reason;
        this.writeoff_date = LocalDate.ofEpochDay(writeoff_date.getTime()/1000/60/60/24);
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDate getWriteoff_date() {
        return writeoff_date;
    }

    public void setWriteoff_date(LocalDate writeoff_date) {
        this.writeoff_date = writeoff_date;
    }
}
