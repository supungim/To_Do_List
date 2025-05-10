package controller;

import com.sun.javafx.menu.MenuItemBase;
import db.DBConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class CreateNewAccountFormController {
    public TextField txtConformPassword;
    public TextField txtNewPassword;
    public Label lblpasswordNotMatch1;
    public Label lblpasswordNotMatch2;
    public Button btnRegister;
    public TextField txtEmail;
    public TextField txtUserName;
    public Label lblId;
    public AnchorPane root;

    public void initialize(){
        setLblVisibility(false);
        setDisableCommon(true);

    }

    public void btnRegisterOnAction(ActionEvent actionEvent) {
        register();
    }

    public void txtConfirmPasswordOnAction(ActionEvent actionEvent) {
        register();
    }

    public void  register() {
        String newPassword=txtNewPassword.getText();
        String confirmPassword =txtConformPassword.getText();

        if(newPassword.equals(confirmPassword)){
            setBorderColor("transparent");
            setLblVisibility(false);

            String id = lblId.getText();
            String userName = txtUserName.getText();
            String email = txtEmail.getText();

            Connection connection = DBConnection.getInstance().getConnection();

            try {
                PreparedStatement preparedStatement =  connection.prepareStatement("insert into user values (?,?,?,?)");
                preparedStatement.setObject(1,id);
                preparedStatement.setObject(2,userName);
                preparedStatement.setObject(3,email);
                preparedStatement.setObject(4,confirmPassword);

                int i = preparedStatement.executeUpdate();

                if(i !=0 ){
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION,"Success");
                    alert.showAndWait();

                    Parent parent = FXMLLoader.load(this.getClass().getResource("../view/LoginForm.fxml"));
                    Scene scene = new Scene(parent);
                    Stage primaryStage = (Stage) root.getScene().getWindow();
                    primaryStage.setScene(scene);
                    primaryStage.setTitle("Login to To-Do");
                    primaryStage.centerOnScreen();

                }else{
                    Alert alert = new Alert(Alert.AlertType.ERROR,"Something went wrong ");
                    alert.showAndWait();
                }

            } catch (SQLException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        else{
            setBorderColor("red");
            setLblVisibility(true);

            txtNewPassword.requestFocus();
        }
    }

    public  void  setLblVisibility(boolean isVisible){
        lblpasswordNotMatch1.setVisible(isVisible);
        lblpasswordNotMatch2.setVisible(isVisible);
    }

    public  void setBorderColor(String color){
        txtNewPassword.setStyle("-fx-border-color: " +color);
        txtConformPassword.setStyle("-fx-border-color: " +color);

    }

    public void btnAddNewUserOnAction(ActionEvent actionEvent) {
        setDisableCommon(false);
        txtUserName.requestFocus();

        autoGenerateID();
    }
    public  void  setDisableCommon(boolean isDisable){
        txtUserName.setDisable(isDisable);
        txtEmail.setDisable(isDisable);
        txtNewPassword.setDisable(isDisable);
        txtConformPassword.setDisable(isDisable);
        btnRegister.setDisable(isDisable);
    }

    public  void  autoGenerateID(){

        Connection connection = DBConnection.getInstance().getConnection();

        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet= statement.executeQuery("select id from  user order by id desc limit 1");
            boolean isExist = resultSet.next();

            if(isExist){
                String oldId = resultSet.getString(1);
                oldId = oldId.substring(1, oldId.length());
                int intId = Integer.parseInt(oldId);
                intId =intId +1;

                if(intId < 10){
                    lblId.setText("U00" + intId);
                }else  if(intId < 100){
                    lblId.setText("U0" + intId);
                }else {
                    lblId.setText("U" + intId);
                }

            }else{
                lblId.setText("U001");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
