# Fast&Easy
JavaFX App that allows manage and write-off products in restaurant system

You need Java18 instaled to run this application

First init: 

1. Create tablespace and user danil identified by "danil" in OracleDB
2. Execute script Script.sql to create demo user and test-date
3. Start Release/Fast&Easy.exe manualy

* Data/data.dat stores settings and can be deleted. In case of deletion it will be generated with default settings in app directory after next initialization.
* Screenshots available in directory "Photos"
* For sms authentication used service sms-fly with personal API-key. Messages to be sent on behalf of the application Fasy$Easy (registered alpha-name).
* Messages will be sent to the number specified in the database, change number "XXXXXXXXXXXX" in SQL-script to yours in format "380...."
* At the publishing stage, 20 messages are available on the balance of the sms-fly service.
* For changing API-KEY input new key in file "JsonSender.java"
* In case of troubles or for more info contact author: daniilart01@gmail.com
