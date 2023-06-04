package com.nure.restaurant;

import com.nure.restaurant.dataWorkers.DBUtil;
import com.nure.restaurant.dataWorkers.JSONSender;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class LoginController {
    public static LoginController controller;
    @FXML
    public PasswordField password;
    @FXML
    public TextField userName;
    @FXML
    public Label label;
    @FXML
    public Label usernameLabel;
    @FXML
    public Label passwordLabel;
    @FXML
    private Button loginButton;


    public void initialize(){
        controller = this;
        userName.focusedProperty().addListener((observable, oldValue, newValue) -> {
            TranslateTransition translateTransition = new TranslateTransition(Duration.millis(200), usernameLabel);
            if(newValue){
                usernameLabel.setStyle("-fx-text-fill: #F4EEE0;");
                label.setText("");
                translateTransition.setToY(-20);
                translateTransition.setToX(-10);
            }
            else{
                if(userName.getText().isEmpty()){
                    usernameLabel.setStyle("-fx-text-fill: #6D5D6E;");
                    translateTransition.setToY(0);
                    translateTransition.setToX(0);
                }
            }
            translateTransition.play();
        });
        password.focusedProperty().addListener((observable, oldValue, newValue) -> {
            TranslateTransition translateTransition = new TranslateTransition(Duration.millis(200), passwordLabel);
            if(newValue){
                passwordLabel.setStyle("-fx-text-fill: #F4EEE0;");
                label.setText("");
                translateTransition.setToY(-20);
                translateTransition.setToX(-10);
            }
            else{
                if(password.getText().isEmpty()){
                    passwordLabel.setStyle("-fx-text-fill: #6D5D6E;");
                    translateTransition.setToY(0);
                    translateTransition.setToX(0);
                }
            }
            translateTransition.play();
        });
    }

    @FXML
    public void login() {
        if(userName.getText().isEmpty()){
            label.setText("Input username");
            new animatefx.animation.Wobble(loginButton).play();
            return;
        }
        else if(password.getText().isEmpty()){
            label.setText("Input password");
            new animatefx.animation.Wobble(loginButton).play();
            return;
        }
        try {
            String sql = "select * from DBUSERS Where username =? and user_password =?";
            PreparedStatement preparedStatement = DBUtil.getConnection().prepareStatement(sql);
            preparedStatement.setString(1,userName.getText());
            String hashed = DigestUtils.sha256Hex(password.getText());
            preparedStatement.setString(2,hashed);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                initMainView(userName.getText());
                System.out.println("Log in success!");
            }
            else{
                label.setText("Invalid password or/and username!");
                new animatefx.animation.Wobble(loginButton).play();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void initMainView(String username) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(RestaurantApplication.class.getResource("mainForm.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.setScene(scene);
        MainController.username = username;
        stage.setX(((Screen.getPrimary().getVisualBounds().getMinX() + Screen.getPrimary().getVisualBounds().getWidth() / 2) - stage.getWidth() / 2));
        stage.setY(((Screen.getPrimary().getVisualBounds().getMinY() + Screen.getPrimary().getVisualBounds().getHeight() / 2) - stage.getHeight() / 2)-100);
    }

    @FXML
    public void usernamePressed() {
        userName.requestFocus();
    }
    @FXML
    public void passwordPressed() {
        password.requestFocus();
    }

    public void closeButtonPressed() {
        System.exit(0);
    }
    @FXML
    public void forgotPasswordPressed() {
        TextInputDialog textInputDialog = new TextInputDialog(userName.getText());
        textInputDialog.initStyle(StageStyle.TRANSPARENT);
        textInputDialog.setHeaderText("Enter your username:");
        Optional<String> result = textInputDialog.showAndWait();
        if(result.isPresent()){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initStyle(StageStyle.TRANSPARENT);
            alert.setHeaderText("If you provide existing username, sms with new password will be sent to your phone number");
            alert.show();

            sendSms(result.get());
        }
    }

    public void sendSms(String username){
        String sql = "select user_number, user_password from dbusers where username=?";
        try(PreparedStatement preparedStatement = DBUtil.getConnection().prepareStatement(sql,
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE)){
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                String newPassword = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 8);
                String hashed = DigestUtils.sha256Hex(newPassword);
                resultSet.updateString("user_password", hashed);
                resultSet.updateRow();
                JSONSender.sendSms(resultSet.getString("user_number"),("Your new password: "+ newPassword));
            }
        }
        catch (SQLException e){
            System.err.println("Error getting phone number from DB");
        }
    }

}
