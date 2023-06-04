package com.nure.restaurant;

import com.nure.restaurant.data.*;
import com.nure.restaurant.dataWorkers.BinaryUtil;
import com.nure.restaurant.dataWorkers.DBUtil;
import com.nure.restaurant.dataWorkers.JSONSender;
import com.nure.restaurant.dataWorkers.ReportGenerator;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.codec.digest.DigestUtils;

import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

public class MainController {
    public static String username;
    public static MainController mainController;
    public WarehouseProduct selectedWarehouseProduct;
    @FXML
    public TabPane tabPane;
    @FXML
    public BorderPane borderPane;
    @FXML
    public TableView<WarehouseProduct> tableViewWarehouse;
    @FXML
    public TableView<CartProduct> cartView;
    @FXML
    public TableView<WriteOffProduct> tableViewWriteOff;
    private ArrayList<WarehouseProduct> warehouseProducts;
    private ArrayList<WriteOffProduct> writeoffProducts;
    private ArrayList<Product> productsToOrder;
    private ArrayList<Supplier> suppliers;
    private ArrayList<CartProduct> cart;
    private ArrayList<Order> orders;
    @FXML
    private PasswordField newPassword;
    @FXML
    private PasswordField repeatPassword;
    @FXML
    private Label errorPasswordLabel;
    @FXML
    private Button changePasswordButton;
    @FXML
    private ChoiceBox<String> orderDetailsChoiceBox;
    @FXML
    private ChoiceBox<String> orderHistoryChoiceBox;
    @FXML
    private ChoiceBox<String> usageHistoryChoiceBox;
    @FXML
    private PasswordField eraseAdminInputField;
    @FXML
    private VBox expiryDatePolicyBox;
    @FXML
    private VBox databaseClearPolicyBox;
    @FXML
    private DatePicker datePickerFrom;
    @FXML
    private DatePicker datePickerTo;
    @FXML
    private TextField warehouseSearchField;
    @FXML
    private Label incorrectDatesReportsLabel;
    @FXML
    private TextField writeOffSearchField;
    @FXML
    private ComboBox<Product> productComboBox;
    @FXML
    private ChoiceBox<Supplier> supplierBox;
    @FXML
    private Label createOrderErorLabel;
    @FXML
    private TextField quantityTextField;
    @FXML
    private Label measurementLabel;
    @FXML
    private Label orderSumLabel;

    public void initialize() {
        mainController = this;
        RestaurantApplication.settings = BinaryUtil.readSettings();
        initSettings();
        warehouseProducts = new ArrayList<>();
        initWarehouseProducts();
        initWarehouse();
        initContextMenuWarehouse();
        initWriteOffView();
        tableViewWarehouse.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                selectedWarehouseProduct = tableViewWarehouse.getSelectionModel().getSelectedItem();
            }
        });
        suppliersInit();
        productsToOrderInit();
        initCreateOrderProductComboBox();
        initCart();
        ordersInit();
    }

    private void ordersInit() {
        orders = new ArrayList<>();
        String ordersSQL = "SELECT ID, ORDER_DATE,TOTAL_COST, SUPPLIER_ID FROM ORDERS WHERE STATUS = 'Pending'";
        String orderItemsSQL = "SELECT P.ID, P.PRODUCT_NAME, P.PRICE, P.MEASUREMENT, OI.QUANTITY FROM ORDERITEMS OI INNER JOIN PRODUCT P ON OI.product_id = P.id WHERE OI.order_id=?";
        try (ResultSet resultSet = DBUtil.getConnection().createStatement().executeQuery(ordersSQL)) {
            while (resultSet.next()) {
                PreparedStatement preparedStatement = DBUtil.getConnection().prepareStatement(orderItemsSQL);
                preparedStatement.setInt(1, resultSet.getInt(1));
                ResultSet items = preparedStatement.executeQuery();
                Order order = new Order(resultSet.getInt(1), resultSet.getDate(2), resultSet.getDouble(3), resultSet.getInt(4));
                while (items.next()) {
                    order.getProducts().add(new OrderProduct(items.getInt(1),items.getString(2), items.getDouble(3), items.getString(4), items.getDouble(5)));
                }
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error getting pending orders from DB");
        }
    }

    private void initCart() {
        cart = new ArrayList<>();
        ObservableList<CartProduct> list = FXCollections.observableList(cart);

        TableColumn<CartProduct, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(cellData -> {
            CartProduct cartProduct = cellData.getValue();
            int id = cartProduct.id();
            return new SimpleIntegerProperty(id).asObject();
        });
        TableColumn<CartProduct, String> nameColumn = new TableColumn<>("Name");

        nameColumn.setCellValueFactory(cellData -> {
            CartProduct cartProduct = cellData.getValue();
            String name = cartProduct.name();
            return new SimpleStringProperty(name);
        });
        TableColumn<CartProduct, Double> priceColumn = new TableColumn<>("Price");
        priceColumn.setCellValueFactory(cellData -> {
            CartProduct cartProduct = cellData.getValue();
            double price = cartProduct.price();
            return new SimpleDoubleProperty(price).asObject();
        });

        TableColumn<CartProduct, String> quantityColumn = new TableColumn<>("Quantity");
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<CartProduct, String> measurementColumn = new TableColumn<>("Measurement");
        measurementColumn.setCellValueFactory(cellData -> {
            CartProduct cartProduct = cellData.getValue();
            String measurement = cartProduct.measurement();
            return new SimpleStringProperty(measurement);
        });
        TableColumn<CartProduct, Double> sumColumn = new TableColumn<>("Sum");
        sumColumn.setCellValueFactory(cellData -> {
            CartProduct cartProduct = cellData.getValue();
            double sum = (Math.round(cartProduct.price() * cartProduct.getQuantity() * 100.0) / 100.0);
            return new SimpleDoubleProperty(sum).asObject();
        });
        cartView.setRowFactory(tv -> {
            TableRow<CartProduct> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();
            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.setOnAction(event -> {
                CartProduct cartProduct = row.getItem();
                if (cartProduct != null) {
                    cartView.getItems().remove(cartProduct);
                    double currentSum = Double.parseDouble(orderSumLabel.getText());
                    orderSumLabel.setText(String.valueOf(currentSum-cartProduct.getQuantity()*cartProduct.price()));
                }
            });
            contextMenu.getItems().addAll(deleteItem);
            row.setContextMenu(contextMenu);
            return row;
        });
        cartView.getColumns().addAll(idColumn, nameColumn, priceColumn, quantityColumn, measurementColumn, sumColumn);
        cartView.setItems(list);
    }

    private void suppliersInit() {
        suppliers = new ArrayList<>();
        try (ResultSet resultSet = DBUtil.getConnection().createStatement().executeQuery("SELECT ID, SUPPLIER_NAME FROM SUPPLIER")) {
            while (resultSet.next()) {
                suppliers.add(new Supplier(resultSet.getInt(1), resultSet.getString(2)));
            }
            supplierBox.setItems(FXCollections.observableList(suppliers));
            supplierBox.setValue(supplierBox.getItems().get(0));
            supplierBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (supplierBox.getItems().size() != 0) {
                    productComboBox.setItems(FXCollections.observableList(productsToOrder.stream().filter(p -> p.supplier() == supplierBox.getValue().id()).toList()));
                }
                cart.clear();
                cartView.refresh();
                orderSumLabel.setText("0");
                productComboBox.getSelectionModel().clearSelection();
                productComboBox.getEditor().clear();
            });

        } catch (SQLException e) {
            System.err.println("Error getting suppliers");
        }
    }

    private void productsToOrderInit() {
        productsToOrder = new ArrayList<>();
        try (ResultSet resultSet = DBUtil.getConnection().createStatement().executeQuery("SELECT * FROM PRODUCT")) {
            while (resultSet.next()) {
                productsToOrder.add(new Product(resultSet.getInt(1), resultSet.getString(2),
                        resultSet.getDouble(3), resultSet.getString(4), resultSet.getInt(5)));
            }
            productComboBox.setItems(FXCollections.observableList(productsToOrder.stream().filter(p -> p.supplier() == supplierBox.getValue().id()).toList()));
        } catch (SQLException e) {
            System.err.println("Error creating productsToOrder list");
        }
    }

    private void initCreateOrderProductComboBox() {
        ChangeListener<String> comboBoxListener = (observable, oldValue, newValue) -> {
            if (newValue.equals(oldValue) || newValue.equals("")) {
                productComboBox.getSelectionModel().clearSelection();
                productComboBox.getEditor().clear();
                productComboBox.setItems(FXCollections.observableList(productsToOrder.stream().filter(p -> p.supplier() == supplierBox.getValue().id()).toList()));
                return;
            }
            String filter = newValue.toLowerCase();
            ObservableList<Product> filteredItems = FXCollections.observableArrayList();
            for (Product item : productsToOrder.stream().filter(p -> p.supplier() == supplierBox.getValue().id()).toList()) {
                if (item.name().toLowerCase().contains(filter)) {
                    filteredItems.add(item);
                }
            }
            productComboBox.hide();
            productComboBox.setItems(filteredItems);
            productComboBox.show();
        };
        productComboBox.setOnAction(event -> {
            productComboBox.hide();
            try {
                measurementLabel.setText(productComboBox.getSelectionModel().getSelectedItem().measurement());
            } catch (ClassCastException ignored) {

            } catch (NullPointerException ignored) {
                measurementLabel.setText("");
            }
        });
        productComboBox.getEditor().textProperty().addListener(comboBoxListener);
    }


    private void initSettings() {
        orderDetailsChoiceBox.valueProperty().addListener((observable, oldValue, newValue) -> RestaurantApplication.settings[1] = orderDetailsChoiceBox.getSelectionModel().getSelectedIndex());
        orderHistoryChoiceBox.valueProperty().addListener((observable, oldValue, newValue) -> RestaurantApplication.settings[2] = orderHistoryChoiceBox.getSelectionModel().getSelectedIndex());
        usageHistoryChoiceBox.valueProperty().addListener((observable, oldValue, newValue) -> RestaurantApplication.settings[3] = usageHistoryChoiceBox.getSelectionModel().getSelectedIndex());
        ToggleGroup toggleGroup = new ToggleGroup();
        int counter = 0;
        for (Node child : expiryDatePolicyBox.getChildren()) {
            if (child instanceof RadioButton radioButton) {
                radioButton.setToggleGroup(toggleGroup);
                if (counter == RestaurantApplication.settings[0]) {
                    radioButton.setSelected(true);
                }
                counter++;
            }
        }
        counter = 1;
        for (Node child : databaseClearPolicyBox.getChildren()) {
            if (child instanceof ChoiceBox<?>) {
                ChoiceBox<String> choiceBox = (ChoiceBox<String>) child;
                choiceBox.setValue(choiceBox.getItems().get(RestaurantApplication.settings[counter]));
                counter++;
            }
        }
        clearDatabaseByExpiryDates();

        clearDatabaseByOptimizationPolicy();
    }

    private void clearDatabaseByOptimizationPolicy() {
        switch (RestaurantApplication.settings[1]) {
            case 1 -> clearOrderDetails(LocalDate.now().minusMonths(3));
            case 2 -> clearOrderDetails(LocalDate.now().minusMonths(6));
            case 3 -> clearOrderDetails(LocalDate.now().minusMonths(12));
        }
        switch (RestaurantApplication.settings[2]) {
            case 1 -> clearOrderHistory(LocalDate.now().minusMonths(3));
            case 2 -> clearOrderHistory(LocalDate.now().minusMonths(6));
            case 3 -> clearOrderHistory(LocalDate.now().minusMonths(12));
        }
        switch (RestaurantApplication.settings[3]) {
            case 1 -> clearUsageHistory(LocalDate.now().minusMonths(3));
            case 2 -> clearUsageHistory(LocalDate.now().minusMonths(6));
            case 3 -> clearUsageHistory(LocalDate.now().minusMonths(12));
        }
    }

    private void clearOrderDetails(LocalDate minDate) {
        String sql = "Select id from orders where order_date < TO_DATE('" + minDate + "', 'YYYY-MM-DD')";
        try (ResultSet resultSet = DBUtil.getConnection().createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                DBUtil.getConnection().createStatement().execute("DELETE FROM ORDERITEMS WHERE order_id=" + resultSet.getInt("id"));
            }
        } catch (SQLException e) {
            System.err.println("Error clearing order details");
        }
    }

    private void clearOrderHistory(LocalDate minDate) {
        String sql = "Select id from Orders where order_date < TO_DATE('" + minDate + "', 'YYYY-MM-DD')";
        try (ResultSet resultSet = DBUtil.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE).executeQuery(sql)) {
            while (resultSet.next()) {
                resultSet.deleteRow();
            }
        } catch (SQLException e) {
            System.err.println("Error clearing order history");
        }
    }

    private void clearUsageHistory(LocalDate minDate) {
        String sql = "Select id from used where date_using < TO_DATE('" + minDate + "', 'YYYY-MM-DD')";
        try (ResultSet resultSet = DBUtil.getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE).executeQuery(sql)) {
            while (resultSet.next()) {
                resultSet.deleteRow();
            }
        } catch (SQLException e) {
            System.err.println("Error clearing usage history");
        }
    }

    private void clearDatabaseByExpiryDates() {
        ArrayList<WarehouseProduct> warehouseProducts = findExpiredDatabaseItem();
        if (RestaurantApplication.settings[0] == 1) {
            if (warehouseProducts.size() == 0) {
                return;
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initStyle(StageStyle.TRANSPARENT);
            alert.setHeaderText("This product are expired, so they was write-offed");
            for (WarehouseProduct warehouseProduct : warehouseProducts) {
                writeoffExpiredDatabaseItem(warehouseProduct);
                alert.setContentText(alert.getContentText() + (warehouseProduct.getName() + ", " + warehouseProduct.getQuantity() + " " + warehouseProduct.getMeasurement() + ". Expiry date: " + warehouseProduct.getExpiry_date() + "\n"));
            }
            alert.showAndWait();
        } else if (RestaurantApplication.settings[0] == 2) {
            for (WarehouseProduct warehouseProduct : warehouseProducts) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.initStyle(StageStyle.TRANSPARENT);
                alert.setHeaderText("This product is expired, write-off?");
                alert.setContentText(warehouseProduct.getName() + ", " + warehouseProduct.getQuantity() + " " + warehouseProduct.getMeasurement() + ". Expiry date: " + warehouseProduct.getExpiry_date());
                Optional<ButtonType> button = alert.showAndWait();
                if (button.isPresent() && button.get() == ButtonType.OK) {
                    writeoffExpiredDatabaseItem(warehouseProduct);
                }
            }
        } else if (RestaurantApplication.settings[0] == 3) {
            for (WarehouseProduct warehouseProduct : warehouseProducts) {
                writeoffExpiredDatabaseItem(warehouseProduct);
            }
        }
    }

    public void writeoffExpiredDatabaseItem(WarehouseProduct warehouseProduct) {
        String sql = "DELETE FROM WAREHOUSE WHERE ID=?";
        try (PreparedStatement preparedStatement = DBUtil.getConnection().prepareStatement(sql)) {
            preparedStatement.setInt(1, warehouseProduct.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting expired database item");
        }

    }

    public ArrayList<WarehouseProduct> findExpiredDatabaseItem() {
        ArrayList<WarehouseProduct> list = new ArrayList<>();
        String sql = "SELECT W.*, P.product_name, P.price FROM Warehouse W inner join Product P ON W.product_id = P.id WHERE expiry_date < " +
                "TO_DATE('" + LocalDate.now() + "', 'YYYY-MM-DD')";
        try (ResultSet resultSet = DBUtil.getConnection().createStatement().executeQuery(sql)) {
            while (resultSet.next()) {
                list.add(new WarehouseProduct(resultSet.getInt("id"), resultSet.getInt("product_id"),
                        resultSet.getDouble("quantity"), resultSet.getString("measurement"),
                        resultSet.getString("product_name"), resultSet.getDouble("price"),
                        resultSet.getDate("expiry_date")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error getting expired date products");
        }
        return list;
    }

    private void initWriteOffView() {
        tableViewWriteOff.getItems().clear();
        tableViewWriteOff.getColumns().clear();
        writeoffProducts = new ArrayList<>();
        String sql = "Select W.id as Writeoff_id, W.Writeoff_date, P.ID, P.product_name, W.quantity, W.measurement, W.Writeoff_reason FROM WRITEOFF W inner join product P on W.PRODUCT_ID = P.ID";
        try (Statement statement = DBUtil.getConnection().createStatement()) {
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                writeoffProducts.add(new WriteOffProduct(resultSet.getInt("Writeoff_id"),
                        resultSet.getInt("ID"), resultSet.getDouble("quantity"),
                        resultSet.getString("measurement"), resultSet.getString("product_name"),
                        resultSet.getDate("Writeoff_date"), resultSet.getString("Writeoff_reason")));
            }
            ObservableList<WriteOffProduct> list = FXCollections.observableList(writeoffProducts);

            TableColumn<WriteOffProduct, String> nameColumn = new TableColumn<>("Name");
            nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

            TableColumn<WriteOffProduct, Double> quantityColumn = new TableColumn<>("Quantity");
            quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

            TableColumn<WriteOffProduct, String> measurementColumn = new TableColumn<>("Measurement");
            measurementColumn.setCellValueFactory(new PropertyValueFactory<>("measurement"));
            measurementColumn.setPrefWidth(10);

            TableColumn<WriteOffProduct, LocalDate> expiryDateColumn = new TableColumn<>("Write-off Date");
            expiryDateColumn.setCellValueFactory(new PropertyValueFactory<>("writeoff_date"));

            TableColumn<WriteOffProduct, String> reasonColumn = new TableColumn<>("Write-Off Reason");
            reasonColumn.setCellValueFactory(new PropertyValueFactory<>("reason"));

            tableViewWriteOff.getColumns().addAll(nameColumn, quantityColumn, measurementColumn, expiryDateColumn, reasonColumn);
            tableViewWriteOff.getColumns().get(0).setStyle("-fx-alignment: Center_left;");
            tableViewWriteOff.getColumns().get(tableViewWriteOff.getColumns().size() - 1).setStyle("-fx-alignment: Center_left;");
            tableViewWriteOff.setItems(list);

        } catch (SQLException exception) {
            exception.printStackTrace();
            System.err.println("Error getting write-off data!");
        }
    }

    private void initContextMenuWarehouse() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem writeOff = new MenuItem("Write-off");
        writeOff.setOnAction(event -> {
            if (selectedWarehouseProduct != null) {
                initWriteOff();
            }
        });
        contextMenu.getItems().add(writeOff);

        MenuItem use = new MenuItem("Use");
        use.setOnAction(event -> {
            if (selectedWarehouseProduct != null) {
                initUse();
            }
        });
        contextMenu.getItems().add(use);

        contextMenu.getStyleClass().add("my-context-menu");
        contextMenu.getItems().forEach(item -> {
            item.setStyle("-fx-text-fill:#F4EEE0;");
            item.getStyleClass().add("my-context-menu-item");
        });
        tableViewWarehouse.setContextMenu(contextMenu);
    }

    private void initUse() {
        Popup popup = initPopUp();
        VBox vBox = (VBox) popup.getContent().get(0);
        Button button = (Button) vBox.getChildren().get(vBox.getChildren().size() - 1);
        Slider slider = (Slider) vBox.getChildren().get(0);
        button.setOnAction(event -> {
            double sliderValue;
            if (selectedWarehouseProduct.getMeasurement().equals("pcs")) {
                sliderValue = (Math.round(slider.getValue()));
            } else {
                sliderValue = (Math.round(slider.getValue() * 10)) / 10.0;
            }
            if (sliderValue == slider.getMax()) {
                String deleteWarehouseSQL = "DELETE FROM WAREHOUSE WHERE ID=?";
                try (PreparedStatement deleteWarehouse = DBUtil.getConnection().prepareStatement(deleteWarehouseSQL)) {
                    deleteWarehouse.setInt(1, selectedWarehouseProduct.getId());
                    deleteWarehouse.executeUpdate();
                    warehouseProducts.remove(selectedWarehouseProduct);
                    DBUtil.getConnection().createStatement().execute("DELETE FROM WRITEOFF WHERE id = (SELECT MAX(id) FROM WRITEOFF)");
                    searchFieldWarehouseAction();
                    addProductToUsed(sliderValue);
                } catch (SQLException e) {
                    System.err.println("Error using");
                }
            } else if (sliderValue != 0) {
                String sql = "UPDATE WAREHOUSE SET QUANTITY =? WHERE id=?";
                try (PreparedStatement preparedStatement = DBUtil.getConnection().prepareStatement(sql)) {
                    preparedStatement.setDouble(1, slider.getMax() - sliderValue);
                    preparedStatement.setInt(2, selectedWarehouseProduct.getId());
                    preparedStatement.executeUpdate();
                    selectedWarehouseProduct.setQuantity(slider.getMax() - sliderValue);
                    addProductToUsed(sliderValue);
                } catch (SQLException e) {
                    System.err.println("Error using part");
                }
            }
            popup.hide();
            tableViewWarehouse.refresh();
        });
    }

    private void addProductToUsed(double value) {
        String sql = "INSERT INTO USED (product_id, quantity, measurement, Date_using) VALUES (?,?,?,?)";
        try (PreparedStatement preparedStatement = DBUtil.getConnection().prepareStatement(sql)) {
            preparedStatement.setInt(1, selectedWarehouseProduct.getProduct_id());
            preparedStatement.setDouble(2, value);
            preparedStatement.setString(3, selectedWarehouseProduct.getMeasurement());
            preparedStatement.setDate(4, Date.valueOf(LocalDate.now()));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding product to used");
        }
    }

    private void initWriteOff() {
        Popup popup = initPopUp();
        VBox vBox = (VBox) popup.getContent().get(0);
        Button button = (Button) vBox.getChildren().get(vBox.getChildren().size() - 1);
        Slider slider = (Slider) vBox.getChildren().get(0);
        button.setOnAction(event -> {
            double sliderValue;
            if (selectedWarehouseProduct.getMeasurement().equals("pcs")) {
                sliderValue = (Math.round(slider.getValue()));
            } else {
                sliderValue = (Math.round(slider.getValue() * 10)) / 10.0;
            }
            if (sliderValue == slider.getMax()) {
                String sql = "DELETE FROM WAREHOUSE WHERE ID=?";
                try (PreparedStatement preparedStatement = DBUtil.getConnection().prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE)) {
                    String reason = reasonAwait(popup);
                    if (reason == null) {
                        return;
                    }
                    preparedStatement.setInt(1, selectedWarehouseProduct.getId());
                    preparedStatement.executeUpdate();
                    warehouseProducts.remove(selectedWarehouseProduct);
                    String alterSql = "UPDATE WRITEOFF SET WRITEOFF_REASON=? WHERE id=(SELECT MAX(ID) FROM WRITEOFF)";
                    PreparedStatement updateStatement = DBUtil.getConnection().prepareStatement(alterSql);
                    updateStatement.setString(1, reason);
                    updateStatement.executeUpdate();
                    searchFieldWarehouseAction();
                    initWriteOffView();
                } catch (SQLException e) {
                    System.err.println("Error write-off");
                }
            } else if (sliderValue != 0) {
                String sql = "Select quantity from Warehouse Where id=?";
                try (PreparedStatement preparedStatement = DBUtil.getConnection().prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE)) {
                    preparedStatement.setInt(1, selectedWarehouseProduct.getId());
                    ResultSet resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        String reason = reasonAwait(popup);
                        if (reason == null) {
                            return;
                        }
                        resultSet.updateDouble(1, resultSet.getDouble(1) - sliderValue);
                        resultSet.updateRow();
                        warehouseProducts.stream().filter(e -> e.getId() == selectedWarehouseProduct.getId()).findFirst().get().setQuantity(selectedWarehouseProduct.getQuantity() - sliderValue);
                        String insertSQL = "INSERT INTO WRITEOFF(product_id, expiry_date, quantity, measurement, writeoff_reason, " +
                                "writeoff_date) VALUES(?,?,?,?,?,?)";
                        PreparedStatement insertStatement = DBUtil.getConnection().prepareStatement(insertSQL);
                        insertStatement.setInt(1, selectedWarehouseProduct.getProduct_id());
                        insertStatement.setDate(2, Date.valueOf(selectedWarehouseProduct.getExpiry_date()));
                        insertStatement.setDouble(3, sliderValue);
                        insertStatement.setString(4, selectedWarehouseProduct.getMeasurement());
                        insertStatement.setString(5, reason);
                        insertStatement.setDate(6, Date.valueOf(LocalDate.now()));
                        insertStatement.executeUpdate();
                        initWriteOffView();
                    }
                } catch (SQLException e) {
                    System.err.println("Error write-off part");
                }
            }
            popup.hide();
            tableViewWarehouse.refresh();
        });
    }

    private String reasonAwait(Popup popup) {
        TextInputDialog textInputDialog = new TextInputDialog();
        textInputDialog.initStyle(StageStyle.TRANSPARENT);
        textInputDialog.setHeaderText("Input reason of write-off");
        Optional<String> enteredReason = textInputDialog.showAndWait();
        if (enteredReason.isEmpty() || enteredReason.get().equals("")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initStyle(StageStyle.TRANSPARENT);
            alert.setHeaderText("You should enter reason of write-off");
            alert.showAndWait();
            popup.hide();
            return null;
        }
        String reason = enteredReason.get();
        return reason;
    }

    private Popup initPopUp() {
        Slider slider = new Slider();
        slider.setMin(0);
        slider.setMax(selectedWarehouseProduct.getQuantity());
        slider.setValue(0);
        slider.getStylesheets().add(String.valueOf(MainController.class.getResource("styles.css")));

        Label valueLabel = new Label("Value: " + slider.getValue());
        valueLabel.setStyle("-fx-text-fill: #f4eee0; -fx-font-weight: bold;");

        Button applyButton = new Button("Apply");
        applyButton.getStylesheets().add(String.valueOf(MainController.class.getResource("styles.css")));
        applyButton.getStyleClass().add("applyValueButton");

        VBox container = new VBox(10);
        container.setPadding(new Insets(10));
        container.setAlignment(Pos.CENTER);
        container.setBackground(new Background(new BackgroundFill(Color.valueOf("393646"), new CornerRadii(10), null)));


        Popup popup = new Popup();
        container.getChildren().addAll(slider, valueLabel, applyButton);
        popup.getContent().add(container);

        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (selectedWarehouseProduct.getMeasurement().equals("pcs")) {
                valueLabel.setText("Value: " + (Math.round(newValue.doubleValue())));
            } else {
                valueLabel.setText("Value: " + (Math.round(newValue.doubleValue() * 10)) / 10.0);
            }
        });
        popup.show(borderPane.getScene().getWindow(), MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y);
        popup.setAutoHide(true);
        return popup;
    }

    private void initWarehouseProducts() {
        String sql = "Select W.id as Warehouse_id,P.ID, P.product_name, P.price, W.quantity, W.measurement, W.expiry_date " +
                "from Warehouse W inner join product P on W.PRODUCT_ID = P.ID";
        try (Statement statement = DBUtil.getConnection().createStatement()) {
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                warehouseProducts.add(new WarehouseProduct(resultSet.getInt("Warehouse_id"),
                        resultSet.getInt("ID"), resultSet.getDouble("quantity"),
                        resultSet.getString("measurement"), resultSet.getString("Product_Name"),
                        resultSet.getDouble("price"), resultSet.getDate("expiry_date")));
            }
        } catch (SQLException exception) {
            System.err.println("Error getting warehouse data!");
        }
    }

    public void initWarehouse() {
        tableViewWarehouse.getItems().clear();
        tableViewWarehouse.getColumns().clear();
        ObservableList<WarehouseProduct> list = FXCollections.observableList(warehouseProducts);
        TableColumn<WarehouseProduct, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<WarehouseProduct, Double> priceColumn = new TableColumn<>("Price");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<WarehouseProduct, Double> quantityColumn = new TableColumn<>("Quantity");
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<WarehouseProduct, String> measurementColumn = new TableColumn<>("Measurement");
        measurementColumn.setCellValueFactory(new PropertyValueFactory<>("measurement"));

        TableColumn<WarehouseProduct, LocalDate> expiryDateColumn = new TableColumn<>("Expiry Date");
        expiryDateColumn.setCellValueFactory(new PropertyValueFactory<>("expiry_date"));

        tableViewWarehouse.getColumns().addAll(nameColumn, priceColumn, quantityColumn, measurementColumn, expiryDateColumn);
        tableViewWarehouse.getColumns().get(0).setStyle("-fx-alignment: Center_left;");
        tableViewWarehouse.setItems(list);

    }

    @FXML
    public void exitButtonPressed() {
        BinaryUtil.writeSettings(RestaurantApplication.settings);
        System.exit(0);
    }

    @FXML
    public void passwordEntered() {
        String password = newPassword.getText();
        String repeatPassword = this.repeatPassword.getText();
        if (!password.equals(repeatPassword)) {
            errorPasswordLabel.setText("Password mismatch");
        } else if (password.toCharArray().length < 5 || password.toCharArray().length > 16) {
            errorPasswordLabel.setText("5-16 characters");
        } else {
            errorPasswordLabel.setText("");
        }
    }

    @FXML
    public void updatePassword() {
        if (!errorPasswordLabel.getText().equals("") || newPassword.getText().equals("")) {
            new animatefx.animation.Wobble(changePasswordButton).play();
            return;
        }
        String password = newPassword.getText();
        TextInputDialog textInputDialog = new TextInputDialog();
        textInputDialog.initStyle(StageStyle.UTILITY);
        textInputDialog.setHeaderText("Input code from SMS:");
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            code.append(Math.round(Math.random() * 10));
        }
        String sql = "select user_number, user_password from DBUSERS where username =?";
        try (PreparedStatement preparedStatement = DBUtil.getConnection().prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE)) {
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                JSONSender.sendSms(resultSet.getString(1), ("Your code: " + code));
            }
            Optional<String> enteredCode = textInputDialog.showAndWait();
            if (enteredCode.isPresent() && enteredCode.get().contentEquals(code)) {
                resultSet.updateString(2, DigestUtils.sha256Hex(password));
                resultSet.updateRow();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.initStyle(StageStyle.TRANSPARENT);
                alert.setHeaderText("Password successfully changed!");
                alert.show();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initStyle(StageStyle.TRANSPARENT);
                alert.setHeaderText("Incorrect code");
                alert.show();
            }
            newPassword.clear();
            repeatPassword.clear();
        } catch (SQLException exception) {
            System.err.println("Error changing password");
        }

    }

    @FXML
    public void expiryDatePolicyChanged() {
        int counter = 0;
        for (Node child : expiryDatePolicyBox.getChildren()) {
            if (child instanceof RadioButton radioButton) {
                if (radioButton.isSelected()) {
                    RestaurantApplication.settings[0] = counter;
                }
                counter++;
            }
        }
    }

    @FXML
    public void eraseButtonPressed() {
        String sql = "select user_password, user_number from dbusers where username = 'admin'";
        try (ResultSet resultSet = DBUtil.getConnection().createStatement().executeQuery(sql)) {
            if (resultSet.next()) {
                if (resultSet.getString(1).equals(DigestUtils.sha256Hex(eraseAdminInputField.getText()))) {
                    StringBuilder code = new StringBuilder();
                    for (int i = 0; i < 4; i++) {
                        code.append(Math.round(Math.random() * 10));
                    }
                    TextInputDialog textInputDialog = new TextInputDialog();
                    textInputDialog.initStyle(StageStyle.UTILITY);
                    textInputDialog.setHeaderText("Enter code");
                    textInputDialog.setContentText("To admin`s number(" + resultSet.getString(2) + ") was sent code, input this code bellow ");
                    JSONSender.sendSms(resultSet.getString(2), "Your secure-code to erase DB data: " + code);
                    Optional<String> enteredCode = textInputDialog.showAndWait();
                    if (enteredCode.isPresent() && enteredCode.get().contentEquals(code)) {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.initStyle(StageStyle.TRANSPARENT);
                        alert.setHeaderText("Are you sure you want erase all data?");
                        alert.getDialogPane().setStyle("-fx-background-color: red; -fx-font-weight: bold;");
                        Optional<ButtonType> buttonType = alert.showAndWait();
                        if (buttonType.isPresent() && buttonType.get() == ButtonType.OK) {
                            doErase();
                        }
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.initStyle(StageStyle.TRANSPARENT);
                        alert.setHeaderText("Invalid secure-code");
                        alert.show();
                    }
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.initStyle(StageStyle.TRANSPARENT);
                    alert.setHeaderText("Invalid password");
                    alert.show();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error getting admin data");
        }
        eraseAdminInputField.clear();
    }

    private void doErase() {
        try {
            DBUtil.getConnection().createStatement().execute("DELETE FROM WAREHOUSE");
            DBUtil.getConnection().createStatement().execute("DELETE FROM WRITEOFF");
            DBUtil.getConnection().createStatement().execute("DELETE FROM USED");
            DBUtil.getConnection().createStatement().execute("DELETE FROM ORDERITEMS");
            DBUtil.getConnection().createStatement().execute("DELETE FROM ORDERS");
            DBUtil.getConnection().createStatement().execute("DELETE FROM PRODUCT");
            DBUtil.getConnection().createStatement().execute("DELETE FROM SUPPLIER");
            System.exit(0);
        } catch (SQLException e) {
            System.err.println("Error erasing data");
        }
    }

    @FXML
    public void searchFieldWarehouseAction() {
        if (warehouseSearchField.getText().equals("")) {
            tableViewWarehouse.setItems(FXCollections.observableList(warehouseProducts));
            tableViewWarehouse.refresh();
            return;
        }
        FilteredList<WarehouseProduct> filteredData = new FilteredList<>(FXCollections.observableList(warehouseProducts), warehouseProduct ->
                warehouseProduct.getName().toLowerCase().contains(warehouseSearchField.getText().toLowerCase()));

        tableViewWarehouse.setItems(filteredData);
        tableViewWarehouse.refresh();
    }

    @FXML
    public void searchFieldWriteOffAction() {
        if (writeOffSearchField.getText().equals("")) {
            tableViewWriteOff.setItems(FXCollections.observableList(writeoffProducts));
            tableViewWriteOff.refresh();
            return;
        }
        FilteredList<WriteOffProduct> filteredData = new FilteredList<>(FXCollections.observableList(writeoffProducts), writeOffProduct ->
                writeOffProduct.getName().toLowerCase().contains(writeOffSearchField.getText().toLowerCase()));

        tableViewWriteOff.setItems(filteredData);
        tableViewWriteOff.refresh();
    }

    @FXML
    public void clearReportErrorLabel() {
        incorrectDatesReportsLabel.setVisible(false);
    }

    @FXML
    public void writeOffedProductsReportByReasonPressed() {
        if (reportDatesCheck()) {
            TextInputDialog textInputDialog = new TextInputDialog();
            textInputDialog.setTitle("Reason choose");
            textInputDialog.setHeaderText("Input reason to generate report:");
            Optional<String> input = textInputDialog.showAndWait();
            if (input.isEmpty() || input.get().equals("")) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initStyle(StageStyle.TRANSPARENT);
                alert.setHeaderText("Input correct reason");
                alert.show();
            } else {
                ReportGenerator.generateWriteOffByReason(Date.valueOf(datePickerFrom.getValue()), Date.valueOf(datePickerTo.getValue()), input.get());
            }
        }
    }

    @FXML
    public void writeOffedProductsReportPressed() {
        if (reportDatesCheck()) {
            ReportGenerator.generateWriteOff(Date.valueOf(datePickerFrom.getValue()), Date.valueOf(datePickerTo.getValue()));
        }
    }

    @FXML
    public void completedOrdersReportPressed() {
        if (reportDatesCheck()) {
            ReportGenerator.generateCompletedOrders(Date.valueOf(datePickerFrom.getValue()), Date.valueOf(datePickerTo.getValue()));
        }
    }

    @FXML
    public void suppliersReportPressed() {
        ReportGenerator.generateSuppliersReport();
    }

    @FXML
    public void pendingOrdersReportPressed() {
        ReportGenerator.generatePendingOrders();
    }

    private boolean reportDatesCheck() {
        if (datePickerFrom.getValue() == null || datePickerTo == null || datePickerTo.getValue().isBefore(datePickerFrom.getValue())) {
            incorrectDatesReportsLabel.setVisible(true);
            return false;
        } else return true;
    }

    @FXML
    public void addToCartPressed() {
        if (productComboBox.getSelectionModel().getSelectedItem() == null) {
            createOrderErorLabel.setText("Chose product");
            return;
        }
        try {
            double quantity = Double.parseDouble(quantityTextField.getText());
            Optional<Product> optionalProduct = productsToOrder.stream().filter(p -> p.supplier() == supplierBox.getValue().id() && p.name().equals(productComboBox.getEditor().getText())).findFirst();
            if (optionalProduct.isEmpty()) {
                createOrderErorLabel.setText("Chose product");
                return;
            }
            Product product = optionalProduct.get();
            double value = Double.parseDouble(orderSumLabel.getText());
            value += (Math.round(product.price() * quantity * 100.0) / 100.0);
            if (cart.stream().anyMatch(p -> p.id() == product.id())) {
                CartProduct existingProduct = cart.stream().filter(p -> p.id() == product.id()).findFirst().get();
                existingProduct.setQuantity(existingProduct.getQuantity() + quantity);
            } else {
                cart.add(new CartProduct(product.id(), product.name(), product.price(), product.measurement(), supplierBox.getSelectionModel().getSelectedItem().id(), quantity));
            }
            orderSumLabel.setText(String.valueOf(value));
            cartView.refresh();
            productComboBox.getSelectionModel().clearSelection();
            productComboBox.getEditor().clear();
            quantityTextField.clear();
            createOrderErorLabel.setText("");
        } catch (NumberFormatException | NullPointerException e) {
            createOrderErorLabel.setText("Incorrect quantity");
        }
    }

    @FXML
    public void addSupplierButtonPressed() {
        Stage newStage = new Stage();

        String windowStyle = "-fx-background-color: lightblue;";
        String labelStyle = "-fx-font-weight: bold;";
        String textFieldStyle = "-fx-background-color: white;";

        Label nameLabel = new Label("Name:");
        Label phoneLabel = new Label("Phone number:");
        TextField nameTextField = new TextField();
        TextField phoneTextField = new TextField();
        Button button = new Button("Save");

        nameLabel.setStyle(labelStyle);
        phoneLabel.setStyle(labelStyle);
        nameTextField.setStyle(textFieldStyle);
        phoneTextField.setStyle(textFieldStyle);

        button.setOnAction(event -> {
            if (!nameTextField.getText().equals("") && !phoneTextField.getText().equals("")) {
                addSupplier(nameTextField.getText(), phoneTextField.getText());
                try (ResultSet resultSet = DBUtil.getConnection().createStatement().executeQuery("SELECT MAX(id) FROM SUPPLIER")) {
                    if (resultSet.next()) {
                        Supplier supplier = new Supplier(resultSet.getInt(1), nameTextField.getText());
                        supplierBox.setItems(FXCollections.observableArrayList());
                        suppliers.add(supplier);
                        supplierBox.setItems(FXCollections.observableList(suppliers));
                        supplierBox.setValue(supplierBox.getItems().get(supplierBox.getItems().size() - 1));
                    }
                } catch (SQLException e) {
                    System.err.println("Error getting last id");
                }
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.initStyle(StageStyle.TRANSPARENT);
                alert.setHeaderText("Supplier added");
                alert.show();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initStyle(StageStyle.TRANSPARENT);
                alert.setHeaderText("Fill all fields");
                alert.show();
            }
            newStage.close();
        });

        VBox vbox = new VBox(nameLabel, nameTextField, phoneLabel, phoneTextField, button);
        vbox.setPadding(new Insets(10));
        vbox.setStyle(windowStyle);
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(4);

        Scene scene = new Scene(vbox);
        newStage.setScene(scene);

        newStage.setTitle("Add supplier");
        newStage.initOwner(supplierBox.getScene().getWindow());
        newStage.initStyle(StageStyle.UTILITY);
        newStage.initModality(Modality.APPLICATION_MODAL);
        newStage.show();
    }

    private void addSupplier(String name, String phone) {
        String sql = "INSERT INTO SUPPLIER(supplier_name, phone_number) VALUES('" + name + "','" + phone + "')";
        try {
            DBUtil.getConnection().createStatement().executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("Error adding supplier");
        }
    }

    @FXML
    public void addProductButtonPressed() {
        String windowStyle = "-fx-background-color: lightblue;";
        String textFieldStyle = "-fx-background-color: white;";

        Stage stage = new Stage();
        stage.setTitle("Add product");

        Label titleLabel = new Label("Title:");
        titleLabel.setStyle("-fx-font-weight: bold;");
        TextField titleTextField = new TextField();

        Label priceLabel = new Label("Price:");
        priceLabel.setStyle("-fx-font-weight: bold;");
        TextField priceTextField = new TextField();

        Label unitLabel = new Label("Unit:");
        unitLabel.setStyle("-fx-font-weight: bold;");
        ChoiceBox<String> unitChoiceBox = new ChoiceBox<>();
        unitChoiceBox.getItems().addAll("pcs", "kg");

        Label supplierLabel = new Label("Supplier:");
        supplierLabel.setStyle("-fx-font-weight: bold;");
        ChoiceBox<Supplier> supplierChoiceBox = new ChoiceBox<>();
        supplierChoiceBox.getItems().addAll(suppliers);

        Button addButton = new Button("Add");
        addButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        addButton.setOnAction(event -> {
            if (titleTextField.getText().equals("") || priceTextField.getText().equals("") || unitChoiceBox.getValue() == null || supplierChoiceBox.getValue() == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initStyle(StageStyle.TRANSPARENT);
                alert.setHeaderText("Fill all fields");
                alert.show();
            } else {
                try {
                    double price = Double.parseDouble(priceTextField.getText());
                    addProduct(titleTextField.getText(), price, unitChoiceBox.getValue(), supplierChoiceBox.getValue().id());
                    try (ResultSet resultSet = DBUtil.getConnection().createStatement().executeQuery("SELECT MAX(id) FROM PRODUCT")) {
                        if (resultSet.next()) {
                            Product product = new Product(resultSet.getInt(1), titleTextField.getText(), price, unitChoiceBox.getValue(), supplierChoiceBox.getValue().id());
                            productsToOrder.add(product);
                            if (supplierBox.getValue().id() == supplierChoiceBox.getValue().id()) {
                                productComboBox.setItems(FXCollections.observableList(productsToOrder.stream().filter(p -> p.supplier() == supplierBox.getValue().id()).toList()));
                            }
                        }
                    } catch (SQLException SQLException) {
                        System.err.println("Error getting max_id of product");
                    }
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.initStyle(StageStyle.TRANSPARENT);
                    alert.setHeaderText("Product added");
                    alert.show();
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.initStyle(StageStyle.TRANSPARENT);
                    alert.setHeaderText("Incorrect price value");
                    alert.show();
                }
            }
            stage.close();
        });
        titleTextField.setStyle(textFieldStyle);
        priceTextField.setStyle(textFieldStyle);

        VBox vbox = new VBox(4, titleLabel, titleTextField, priceLabel, priceTextField, supplierLabel, supplierChoiceBox, unitLabel, unitChoiceBox, addButton);
        vbox.setPadding(new Insets(10));
        vbox.setAlignment(Pos.CENTER);
        vbox.setStyle(windowStyle);

        Scene scene = new Scene(vbox);
        stage.setScene(scene);
        stage.initStyle(StageStyle.UTILITY);
        stage.show();
    }

    private void addProduct(String title, Double price, String unit, int supplier_id) {
        String sql = "INSERT INTO PRODUCT(product_name, price, measurement, supplier_id) VALUES('" + title + "'," + price + ",'" + unit + "'," + supplier_id + ")";
        try {
            DBUtil.getConnection().createStatement().executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("Error adding product");
        }
    }

    @FXML
    public void placeOrderPressed() {
        if (cart.size() != 0) {
            double totalCost = cart.stream().mapToDouble(p -> (Math.round(p.getQuantity() * p.price() * 100.0) / 100.0)).sum();
            int supplier_id = supplierBox.getValue().id();
            Date date = Date.valueOf(LocalDate.now());
            String orderSQL = "INSERT INTO ORDERS (order_date, total_cost, supplier_id) VALUES (TO_DATE(?, 'YYYY-MM-DD'),?,?)";
            String orderItemSQL = "INSERT INTO OrderItems (order_id, product_id, quantity) VALUES (?, ?, ?)";
            String maxIdSQL = "SELECT MAX(id) FROM ORDERS";
            try (PreparedStatement ordersStatement = DBUtil.getConnection().prepareStatement(orderSQL)) {
                ordersStatement.setString(1, date.toString());
                ordersStatement.setDouble(2, totalCost);
                ordersStatement.setInt(3, supplier_id);
                ordersStatement.executeUpdate();
                ResultSet maxIDSet = DBUtil.getConnection().createStatement().executeQuery(maxIdSQL);
                int maxID = 0;
                if (maxIDSet.next()) {
                    maxID = maxIDSet.getInt(1);
                }
                for (CartProduct cartProduct : cart) {
                    PreparedStatement orderItemsStatement = DBUtil.getConnection().prepareStatement(orderItemSQL);
                    orderItemsStatement.setInt(1, maxID);
                    orderItemsStatement.setInt(2, cartProduct.id());
                    orderItemsStatement.setDouble(3, cartProduct.getQuantity());
                    orderItemsStatement.executeUpdate();
                }
                cart.clear();
                cartView.refresh();
                orderSumLabel.setText("0");
                measurementLabel.setText("");
                productComboBox.getEditor().clear();
                productComboBox.getSelectionModel().clearSelection();
                quantityTextField.clear();
                createOrderErorLabel.setText("");

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.initStyle(StageStyle.TRANSPARENT);
                alert.setHeaderText("Order Created!");
                alert.show();

                ordersInit();
            } catch (SQLException e) {
                System.err.println("Error creating order");
            }
        }
    }

    @FXML
    public void pendingOrdersShow() {

        Stage newStage = new Stage();

        TableView<Order> tableView = new TableView<>();
        TableColumn<Order, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Order, Integer> orderDateColumn = new TableColumn<>("Order Date");
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        TableColumn<Order, Double> totalCostColumn = new TableColumn<>("Total Cost");
        totalCostColumn.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        TableColumn<Order, Integer> supplierIDColumn = new TableColumn<>("Supplier ID");
        supplierIDColumn.setCellValueFactory(new PropertyValueFactory<>("supplierID"));
        tableView.getColumns().addAll(idColumn, orderDateColumn, totalCostColumn, supplierIDColumn);

        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Cancel");
        deleteItem.setOnAction(event -> {
            Order selectedOrder = tableView.getSelectionModel().getSelectedItem();
            if (selectedOrder != null) {
                try {
                    DBUtil.getConnection().createStatement().executeUpdate("UPDATE ORDERS SET STATUS = 'Canceled' WHERE ID = " + selectedOrder.getId());
                    orders.remove(selectedOrder);
                    tableView.refresh();
                } catch (SQLException e) {
                    System.err.println("Error canceling order");
                }
            }
        });
        MenuItem acceptItem = new MenuItem("Accept");
        acceptItem.setOnAction(event -> {
            Order selectedOrder = tableView.getSelectionModel().getSelectedItem();
            if (selectedOrder != null) {
                try {
                    DBUtil.getConnection().setAutoCommit(false);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                for (OrderProduct orderProduct : selectedOrder.getProducts()) {
                    Stage popupStage = new Stage();
                    popupStage.initModality(Modality.APPLICATION_MODAL);
                    popupStage.initOwner(tableView.getScene().getWindow());

                    Label label = new Label("Enter " + orderProduct.getName() + ", " + orderProduct.getQuantity() + orderProduct.getMeasurement() + " expiry date:");
                    DatePicker datePicker = new DatePicker();

                    Button button = new Button("Accept");
                    button.setOnAction(e -> {
                        if (datePicker.getValue() != null) {
                            try(PreparedStatement preparedStatement = DBUtil.getConnection().prepareStatement("INSERT INTO Warehouse (product_id, expiry_date, quantity) VALUES (? , TO_DATE(?, 'YYYY-MM-DD'), ?)")) {
                                preparedStatement.setInt(1, orderProduct.getID());
                                preparedStatement.setString(2, String.valueOf(Date.valueOf(datePicker.getValue())));
                                preparedStatement.setDouble(3, orderProduct.getQuantity());
                                preparedStatement.executeUpdate();
                            }
                            catch (SQLException ex){
                                System.err.println("Error adding product to warehouse!");
                            }
                            selectedOrder.getProducts().remove(orderProduct);
                            tableView.refresh();
                        }
                        popupStage.close();
                        if(selectedOrder.getProducts().size()==0){
                            try {
                                DBUtil.getConnection().commit();
                                DBUtil.getConnection().setAutoCommit(true);
                                DBUtil.getConnection().createStatement().executeUpdate("UPDATE ORDERS SET STATUS = 'Completed' WHERE ID = "+selectedOrder.getId());
                                orders.remove(selectedOrder);
                                warehouseProducts.clear();
                                initWarehouseProducts();
                                tableViewWarehouse.refresh();
                                tableView.refresh();
                            } catch (SQLException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    });
                    VBox popupRoot = new VBox();
                    popupRoot.setPadding(new Insets(20));
                    popupRoot.setSpacing(10);
                    popupRoot.setAlignment(Pos.CENTER);

                    popupRoot.getChildren().addAll(label, datePicker, button);

                    Scene popupScene = new Scene(popupRoot);
                    popupStage.setScene(popupScene);
                    popupStage.initStyle(StageStyle.TRANSPARENT);
                    popupStage.show();
                }
            }
        });
        contextMenu.getItems().addAll(deleteItem, acceptItem);

        tableView.setContextMenu(contextMenu);

        TableColumn<Order, Void> productsColumn = new TableColumn<>("Products");
        productsColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                if (getTableRow() == null) {
                    return;
                }
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    Order order = getTableRow().getItem();
                    if (order != null) {
                        Accordion accordion = new Accordion();
                        ObservableList<OrderProduct> products = FXCollections.observableArrayList(order.getProducts());
                        TitledPane titledPane = new TitledPane("Products", new ListView<>(products));
                        accordion.getPanes().add(titledPane);
                        setGraphic(accordion);
                    } else {
                        setGraphic(null);
                    }
                }
                setPrefWidth(300);
            }
        });
        tableView.getColumns().add(productsColumn);
        VBox vbox = new VBox(tableView);
        vbox.getStylesheets().add(String.valueOf(this.getClass().getResource("styles.css")));
        tableView.getStyleClass().add("pending-orders-table-view");

        ObservableList<Order> ordersToShow = FXCollections.observableList(orders);


        tableView.setItems(ordersToShow);

        tableView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(tableView, event.getScreenX(), event.getScreenY());
            }
        });

        Scene scene = new Scene(vbox, 570, 400);
        newStage.setScene(scene);

        newStage.setTitle("Pending orders");
        newStage.initOwner(supplierBox.getScene().getWindow());
        newStage.initStyle(StageStyle.UTILITY);
        newStage.initModality(Modality.APPLICATION_MODAL);
        newStage.show();
    }
}