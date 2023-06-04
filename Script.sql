CREATE TABLE Supplier (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  supplier_name VARCHAR2(255),
  phone_number VARCHAR2(255)
);
CREATE TABLE Product (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  product_name VARCHAR2(255),
  price FLOAT,
  measurement VARCHAR2(255),
  supplier_id NUMBER,
  FOREIGN KEY (supplier_id) REFERENCES Supplier(id)ON DELETE CASCADE
);
CREATE TABLE Warehouse (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  product_id NUMBER,
  expiry_date DATE,
  quantity FLOAT,
  measurement VARCHAR2(255),
  FOREIGN KEY (product_id) REFERENCES Product(id)ON DELETE CASCADE
);
CREATE TABLE Writeoff (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  product_id NUMBER,
  expiry_date DATE,
  quantity FLOAT,
  measurement VARCHAR2(255),
  writeoff_reason VARCHAR(255) DEFAULT 'Expired',
  writeoff_date DATE
);

CREATE TABLE Orders (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  order_date DATE,
  total_cost FLOAT,
  supplier_id NUMBER,
  status VARCHAR2(255) DEFAULT 'Pending',
  FOREIGN KEY (supplier_id) REFERENCES supplier(id)
);

CREATE TABLE OrderItems (
  order_id NUMBER,
  product_id NUMBER,
  quantity NUMBER,
  PRIMARY KEY (order_id, product_id),
  FOREIGN KEY (order_id) REFERENCES Orders(id) ON DELETE CASCADE,
  FOREIGN KEY (product_id) REFERENCES Product(id) 
);
CREATE TABLE USED (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  product_id NUMBER,
  date_using DATE,
  quantity FLOAT,
  measurement VARCHAR2(255),
  FOREIGN KEY (product_id) REFERENCES Product(id) ON DELETE CASCADE
);
CREATE TABLE DBUsers(
username varchar2(255) PRIMARY KEY,
user_password varchar2(255),
user_number varchar2(255)
);

CREATE OR REPLACE TRIGGER trg_set_warehouse_measurement
BEFORE INSERT ON Warehouse
FOR EACH ROW
BEGIN
  SELECT measurement INTO :new.measurement
  FROM Product
  WHERE id = :new.product_id;
END;
/

CREATE OR REPLACE TRIGGER TRG_DELETE_WAREHOUSE
AFTER DELETE ON Warehouse
FOR EACH ROW
BEGIN
  INSERT INTO Writeoff (product_id, expiry_date, quantity, measurement, writeoff_date)
  VALUES (:OLD.product_id, :OLD.expiry_date, :OLD.quantity, :OLD.measurement, SYSDATE);
END;
/

CREATE OR REPLACE TRIGGER trg_set_order_date
BEFORE UPDATE OF status ON Orders
FOR EACH ROW
BEGIN
  IF :NEW.status = 'Completed' THEN
    :NEW.order_date := SYSDATE;
  END IF;
END;
/

INSERT INTO DBUSERS (username, user_password, user_number) values ('admin', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', '380991854927');

COMMIT;