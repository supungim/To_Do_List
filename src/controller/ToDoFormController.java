package controller;

import db.DBConnection;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import tm.ToDoTM;

import java.io.IOException;
import java.sql.*;
import java.util.Optional;

public class ToDoFormController {
    public Label lblTitle;
    public Label lblID;
    public AnchorPane root;
    public Pane subroot;
    public TextField txtNewToDo;
    public ListView<ToDoTM> lstToDos;
    public Button btnUpdate;
    public Button btnDelete;
    public TextField txtSelectedToDo;


    public void  initialize(){
        String id = LoginFormController.loginUserID;
        String userName = LoginFormController.loginUserName;

        lblTitle.setText("Hi " + userName + " Welcome to ToDo List ");
        lblID.setText(id);

        subroot.setVisible(false);

        loadList();
        setDisableCommon(true);

        lstToDos.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ToDoTM>() {
            @Override
            public void changed(ObservableValue<? extends ToDoTM> observableValue, ToDoTM toDoTM, ToDoTM t1) {
                setDisableCommon(false);
                subroot.setVisible(false);

                ToDoTM selectedItem = lstToDos.getSelectionModel().getSelectedItem();
                if(selectedItem ==null){
                    return;
                }

                txtSelectedToDo.setText(selectedItem.getDescription());
                txtSelectedToDo.requestFocus();
            }
        });
    }
    public void setDisableCommon(boolean isDisable){
        txtSelectedToDo.setDisable(isDisable);
        btnDelete.setDisable(isDisable);
        btnUpdate.setDisable(isDisable);
    }

    public void btnLogOutOnAction(ActionEvent actionEvent) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,"Do You Want to Log out..? ", ButtonType.YES,ButtonType.NO );
        Optional<ButtonType> buttonType = alert.showAndWait();

        if(buttonType.get().equals(ButtonType.YES)){
            Parent parent = FXMLLoader.load(this.getClass().getResource("../view/LoginForm.fxml"));
            Scene scene = new Scene(parent);
            Stage primaryStage = (Stage) root.getScene().getWindow();
            primaryStage.setScene(scene);
            primaryStage.setTitle("Login to To-Do ");
            primaryStage.centerOnScreen();
        }
    }

    public void btnAddNewToDoOnAction(ActionEvent actionEvent) {

        txtNewToDo.requestFocus();
        lstToDos.getSelectionModel().clearSelection();
        setDisableCommon(false);
        subroot.setVisible(true);
        txtSelectedToDo.clear();
    }

    public void btnAddToListOnAction(ActionEvent actionEvent) {
        String id = autoGenerateID();
        String description = txtNewToDo.getText();
        String user_id = lblID.getText();

        Connection connection = DBConnection.getInstance().getConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("insert into todo values(?,?,?)");
            preparedStatement.setObject(1,id);
            preparedStatement.setObject(2,description);
            preparedStatement.setObject(3,user_id);

            preparedStatement.executeUpdate();

            txtNewToDo.clear();
            subroot.setVisible(false);
            loadList();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
    public  String autoGenerateID() {
        Connection connection = DBConnection.getInstance().getConnection();
        String id = null;


        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("select id from  todo order by id desc limit 1");
            boolean isExist = resultSet.next();

            if (isExist) {
                String oldId = resultSet.getString(1);
                oldId = oldId.substring(1, oldId.length());
                int intId = Integer.parseInt(oldId);
                intId = intId + 1;

                if (intId < 10) {
                    id = "T001" + intId;
                } else if (intId < 100) {
                    id = "T0" + intId;
                } else {
                    id = "T" + intId;
                }

            } else {
                id = "T001";
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return id;

    }
    public  void  loadList(){

        ObservableList<ToDoTM> items = lstToDos.getItems();
        items.clear();

        Connection connection =  DBConnection.getInstance().getConnection();

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("select * from todo where user_id = ? ");
            preparedStatement.setObject(1,LoginFormController.loginUserID);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                String id = resultSet.getString(1);
                String description =resultSet.getString(2);
                String user_id = resultSet.getString(3);

                ToDoTM toDoTM  = new ToDoTM(id,description,user_id);

                items.add(toDoTM);
            }
            lstToDos.refresh();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }

    public void btnUpdateOnAction(ActionEvent actionEvent) {
        String text = txtSelectedToDo.getText();
        ToDoTM selectedItem = lstToDos.getSelectionModel().getSelectedItem();

        Connection connection = DBConnection.getInstance().getConnection();

        try {
            PreparedStatement preparedStatement= connection.prepareStatement("update todo set description = ? where id = ?");
            preparedStatement.setObject(1,text);
            preparedStatement.setObject(2,selectedItem.getId());

            preparedStatement.executeUpdate();

            loadList();
            setDisableCommon(true);
            txtSelectedToDo.clear();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void btnDeleteOnAction(ActionEvent actionEvent) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,"Do You Wnte to delete thistodo",ButtonType.YES,ButtonType.NO);
        Optional<ButtonType>  buttonType = alert.showAndWait();

        if(buttonType.get().equals(ButtonType.YES)){
            Connection connection = DBConnection.getInstance().getConnection();

            try {
                PreparedStatement preparedStatement = connection.prepareStatement("delete from todo where id=?");
                preparedStatement.setObject(1,lstToDos.getSelectionModel().getSelectedItem().getId());

                preparedStatement.executeUpdate();
                loadList();
                txtSelectedToDo.clear();

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }



    }
}
