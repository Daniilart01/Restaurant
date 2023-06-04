package com.nure.restaurant.dataWorkers;

import java.sql.Connection;

import java.sql.DriverManager;
import java.sql.SQLException;
public final class DBUtil {
    private static Connection connection;
    private static boolean isDriverLoaded = false;
    static{
        try{
            Class.forName("oracle.jdbc.driver.OracleDriver");
            isDriverLoaded = true;
        }catch(ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    private final static String name = "ORCL";
    private final static String url="jdbc:oracle:thin:@localhost:1521:"+name;
    private final static String user="admin";
    private final static String password="admin";


    public static Connection getConnection() throws SQLException{
        if(connection != null){
            return connection;
        }
        if(isDriverLoaded){
            connection = DriverManager.getConnection(url,user,password);
            System.out.println("Connection established");
            return connection;
        }
        System.err.println("No driver loaded!");
        throw new SQLException();
    }
}