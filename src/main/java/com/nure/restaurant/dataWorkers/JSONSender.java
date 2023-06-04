package com.nure.restaurant.dataWorkers;

import java.io.*;
import java.net.*;

public class JSONSender {
    public static void sendSms(String number, String text) {
        try {
            URL url = new URL("https://sms-fly.ua/api/v2/api.php");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String jsonInputString = String.format("""
                    {
                     "auth": {
                     "key": "t6GEcX6sZAu2YpLHM0eQSpJAqMYDcP7B"
                     },
                     "action": "SENDMESSAGE",
                     "data": {
                     "recipient": "%s",
                     "channels": [
                     "sms"
                     ],
                     "sms": {
                     "source": "Fast&Easy",
                     "ttl": 300,
                     "flash": 0,
                     "text": "%s"
                     }
                     }
                     }
                    """, number, text);
            OutputStream os = connection.getOutputStream();
            os.write(jsonInputString.getBytes());
            os.flush();
            os.close();

            String responseMessage = connection.getResponseMessage();
            int responseCode = connection.getResponseCode();
            System.out.print("Response code: " + responseCode);
            System.out.println(", "+responseMessage);


            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            System.out.println("Response body: " + response);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
