package com.nure.restaurant.dataWorkers;

import java.io.*;

public class BinaryUtil {
    public static void writeSettings(int[] settings) {
        try {
            File dir = new File("data");
            if(!dir.exists()){
                if(!dir.mkdir()){
                    System.err.println("Error creating directory");
                    return;
                }            }
            File file = new File("data/data.dat");
            DataOutputStream output = new DataOutputStream(new FileOutputStream(file));
            output.writeInt(settings[0]);
            output.writeInt(settings[1]);
            output.writeInt(settings[2]);
            output.writeInt(settings[3]);
        } catch (IOException e) {
            System.err.println("Error writing file");
        }
    }

    public static int[] readSettings() {
        try (DataInputStream input = new DataInputStream(new FileInputStream("data/data.dat"))) {
            int expiry_date_policy = input.readInt();
            int order_details_delete = input.readInt();
            int order_history_delete = input.readInt();
            int usage_history_delete = input.readInt();
            System.out.println("File successfully read");
            return new int[]{expiry_date_policy, order_details_delete, order_history_delete, usage_history_delete};
        } catch (IOException ex) {
            System.err.println("Error reading file");
            return new int[]{0,0,0,0};
        }
    }
}
