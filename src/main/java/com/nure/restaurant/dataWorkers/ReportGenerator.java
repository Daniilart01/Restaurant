package com.nure.restaurant.dataWorkers;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReportGenerator {
    public static void generateWriteOff(Date dateFrom, Date dateTo){
        generateWriteOffReport(dateFrom, dateTo, "");
    }
    public static void generateWriteOffByReason(Date dateFrom, Date dateTo, String reason){
        String SQLReason = " AND LOWER(WRITEOFF_REASON) LIKE '%"+reason+"%'";
        generateWriteOffReport(dateFrom, dateTo, SQLReason);
    }
    public static void generatePendingOrders(){
        String sql = "SELECT O.ID, O.ORDER_DATE, O.STATUS, O.TOTAL_COST FROM ORDERS O WHERE LOWER(STATUS) = 'pending'";
        generateOrdersReport(sql, "Pending orders");
    }
    public static void generateCompletedOrders(Date dateFrom, Date dateTo){
        String sql = "SELECT O.ID, O.ORDER_DATE, O.STATUS, O.TOTAL_COST FROM ORDERS O WHERE O.ORDER_DATE >= TO_DATE('"+dateFrom+"','YYYY-MM-DD') AND O.ORDER_DATE < TO_DATE('"+Date.valueOf(dateTo.toLocalDate().plusDays(1))+"','YYYY-MM-DD') AND LOWER(STATUS) = 'completed'";
        generateOrdersReport(sql,"Completed"+dateFrom+"-"+dateTo);
    }
    private static void generateWriteOffReport(Date dateFrom, Date dateTo, String reason){
        String sql = "SELECT P.PRODUCT_NAME, W.QUANTITY, W.MEASUREMENT, W.WRITEOFF_DATE, W.WRITEOFF_REASON, P.PRICE FROM WRITEOFF W INNER JOIN PRODUCT P ON W.product_id = P.id WHERE W.writeoff_date >= TO_DATE('"+dateFrom+"','YYYY-MM-DD') AND W.writeoff_date < TO_DATE('"+Date.valueOf(dateTo.toLocalDate().plusDays(1))+"','YYYY-MM-DD')"+reason;
        try (PreparedStatement preparedStatement = DBUtil.getConnection().prepareStatement(sql)){
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Double> sums = new ArrayList<>();
            List<List<String>> data = new ArrayList<>();
            data.add(List.of("Назва товару", "Кільість", "Одиниця вимірювання", "Дата списання", "Причина списання", "Вартість товару"));
            data.add(new ArrayList<>());
            while (resultSet.next()){
                double sum = resultSet.getDouble(6)*resultSet.getDouble(2);
                data.add(List.of(resultSet.getString(1), resultSet.getString(2),resultSet.getString(3),
                        resultSet.getString(4),resultSet.getString(5),String.valueOf(sum)));
                        sums.add(sum);
            }
            data.add(new ArrayList<>());
            data.add(List.of("","","","","Загальна сума: ",String.valueOf(sums.stream().mapToDouble(Double::doubleValue).sum())));
            ExcelWorker.generateReport(data, "Write-Off "+dateFrom+"-"+dateTo);
        }
        catch (SQLException e){
            System.err.println("Error creating write-off report");
        }
    }
    private static void generateOrdersReport(String sql, String name){
        try (ResultSet resultSet = DBUtil.getConnection().createStatement().executeQuery(sql)){
            List<List<String>> data = new ArrayList<>();
            data.add(List.of("№","Date", "Status"));
            data.add(new ArrayList<>());
            while (resultSet.next()){
                data.add(List.of(resultSet.getString(1),resultSet.getString(2), resultSet.getString(3)));
                ResultSet itemsSet = DBUtil.getConnection().createStatement().executeQuery("SELECT P.PRODUCT_NAME, P.PRICE, OI.QUANTITY, P.MEASUREMENT FROM PRODUCT P INNER JOIN ORDERITEMS OI ON P.ID = OI.PRODUCT_ID WHERE OI.ORDER_ID ="+resultSet.getString(1));
                while (itemsSet.next()){
                    data.add(new ArrayList<>(Arrays.asList("",itemsSet.getString(1),itemsSet.getString(2),itemsSet.getString(3),itemsSet.getString(4))));
                }
                data.get(data.size()-2).addAll(List.of("","Вартість замовлення"));
                data.get(data.size()-1).addAll(List.of("",resultSet.getString(4),"UAH"));
            }
            ExcelWorker.generateReport(data, name);
        }
        catch (SQLException e){
            System.err.println("Error creating orders report");
        }
    }
    public static void generateSuppliersReport(){
        String sql = "SELECT ID, SUPPLIER_NAME,PHONE_NUMBER FROM SUPPLIER";
        try (ResultSet resultSet = DBUtil.getConnection().createStatement().executeQuery(sql)){
            List<List<String>> data = new ArrayList<>();
            data.add(List.of("Ідентифікатор","Ім'я","Номер телефону"));
            data.add(new ArrayList<>());
            while (resultSet.next()){
                data.add(List.of(resultSet.getString(1), resultSet.getString(2), "+38"+resultSet.getString(3)));
            }
            ExcelWorker.generateReport(data, "Suppliers");
        }
        catch (SQLException e){
            System.err.println("Error creating suppliers report");
        }
    }

}
