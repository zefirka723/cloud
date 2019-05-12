package com.cloud.client;

import com.cloud.common.*;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    TextField tfFileName, uploadFileName;

    @FXML
    TextField loginField, passField;

    @FXML
    ListView<String> filesList;

    @FXML
    ListView<String> cloudFilesList;

    @FXML
    HBox authPanel, clientPanel, serverPanel;

    @FXML
    VBox rootNode;

    private boolean isLogged = false;

    public void changeScreenMode() {
        Parent root;
        try {
            root = FXMLLoader.load(getClass().getResource(isLogged ? "/cloud.fxml" : "/login.fxml"));
            Scene scene = new Scene(root);
            if (Platform.isFxApplicationThread()) {
                ((Stage) rootNode.getScene().getWindow()).setScene(scene);
            } else {
                Platform.runLater(() -> {
                    ((Stage) rootNode.getScene().getWindow()).setScene(scene);
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.start();
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    AbstractMessage am = Network.readObject();
                    if (am instanceof FileMessage) {
                        FileMessage fm = (FileMessage) am;
                        Files.write(Paths.get("client_storage/" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                        refreshLocalFilesList();
                    }
                    if (am instanceof Command) {
                        switch (((Command) am).getCommandType()) {
                            case AUTH_OK:
                                isLogged = true;
                                changeScreenMode();
                                break;
                        }
//                        switch (((Command) am).getMsgType()) {
//                            case "AUTH_OK":
//                                isLogged = true;
//                                changeScreenMode();
//                                break;
//                            case "AUTH_DENIED":
//                                // алерт об ошибке
//                                break;
//                            case "REFRESH_FILES_LIST":
//                                refreshCloudFilesList();
//                                break;
//                            case "DISCONNECTED":
//                                isLogged = false;
//                                changeScreenMode();
//                                break;
//                        }
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } finally {
                Network.stop();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public void pressOnDownloadBtn(ActionEvent actionEvent) {
        System.out.println("кнопка DOWNLOAD нажата");
        System.out.println(tfFileName.getLength());
        if (tfFileName.getLength() > 0) {
            Network.sendMsg(new FileRequest(tfFileName.getText()));
            tfFileName.clear();
        }
    }

    public void pressOnUploadBtn(ActionEvent actionEvent) {
        System.out.println("кнопка UPLOAD нажата");
        if (uploadFileName.getLength() > 0) {
            try {
                Network.sendMsg(new FileMessage(Paths.get("client_storage/" + uploadFileName.getText())));
            } catch (IOException e) {
                e.printStackTrace();
            }
            uploadFileName.clear();
            //refreshCloudFilesList();
        }
    }

    public void refreshLocalFilesList() {
        if (Platform.isFxApplicationThread()) {
            try {
                filesList.getItems().clear();
                Files.list(Paths.get("client_storage")).map(p -> p.getFileName().toString()).forEach(o -> filesList.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Platform.runLater(() -> {
                try {
                    filesList.getItems().clear();
                    Files.list(Paths.get("client_storage")).map(p -> p.getFileName().toString()).forEach(o -> filesList.getItems().add(o));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }


    public void refreshCloudFilesList() {
        if (Platform.isFxApplicationThread()) {
            try {
                cloudFilesList.getItems().clear();
                Files.list(Paths.get("server_storage/login1/")).map(p -> p.getFileName().toString()).forEach(o -> cloudFilesList.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Platform.runLater(() -> {
                try {
                    cloudFilesList.getItems().clear();
                    Files.list(Paths.get("server_storage/login1/")).map(p -> p.getFileName().toString()).forEach(o -> cloudFilesList.getItems().add(o));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void sendAuth() {
        if (loginField.getLength() > 0 && passField.getLength() > 0) {
     //       Network.sendMsg(new Command("AUTH_REQUEST", loginField.getText() + " " + passField.getText()));
            Network.sendMsg(new Command(Command.CommandType.AUTH_REQUEST, loginField.getText() + " " + passField.getText()));
        }
    }

    public void disconnect() {
        isLogged = false;
        changeScreenMode();
        //Network.sendMsg(new Command("DISCONNECT"));
    }


}

