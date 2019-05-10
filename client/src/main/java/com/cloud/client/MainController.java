package com.cloud.client;

import com.cloud.common.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

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

    private boolean isLogged = false;

    public void showInterface (boolean isLogged) {
        if (isLogged == true) {
            clientPanel.setVisible(true);
            serverPanel.setVisible(true);
            authPanel.setVisible(false);
            refreshLocalFilesList();
            refreshCloudFilesList();
        }
        else {
            authPanel.setVisible(true);
            clientPanel.setVisible(false);
            serverPanel.setVisible(false);
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.start();
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    AbstractMessage am = Network.readObject();
                    if (am instanceof AuthResponse) {
                        System.out.println("Мы получили ответ по авторизации");
                        isLogged = ((AuthResponse) am).isLogged();
                        System.out.println("isLogged = " +isLogged);
                        showInterface(isLogged);
                        System.out.println("тыц!");
                        System.out.println(((AuthResponse) am).getMessage());
                    }
                    if (am instanceof FileMessage) {
                        FileMessage fm = (FileMessage) am;
                        Files.write(Paths.get("client_storage/" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                        refreshLocalFilesList();
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
        showInterface(isLogged);
    }

    public void pressOnDownloadBtn(ActionEvent actionEvent) {
        if (tfFileName.getLength() > 0) {
            Network.sendMsg(new FileRequest(tfFileName.getText()));
            tfFileName.clear();
        }
    }

    public void pressOnUploadBtn(ActionEvent actionEvent) {
        if (uploadFileName.getLength() > 0) {
            try {
                Network.sendMsg(new FileMessage(Paths.get("client_storage/" + uploadFileName.getText())));
            } catch (IOException e) {
                e.printStackTrace();
            }
            uploadFileName.clear();
            refreshCloudFilesList();
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
                Files.list(Paths.get("server_storage")).map(p -> p.getFileName().toString()).forEach(o -> cloudFilesList.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Platform.runLater(() -> {
                try {
                    cloudFilesList.getItems().clear();
                    Files.list(Paths.get("server_storage")).map(p -> p.getFileName().toString()).forEach(o -> cloudFilesList.getItems().add(o));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }


    public void sendAuth(){
        if (loginField.getLength() > 0 && passField.getLength() > 0) {
            Network.sendMsg(new AuthRequest(loginField.getText(), passField.getText()));
        }
//        isLogged = true; // ну ооооочень простая авторизация :D
//        showInterface(isLogged);

    }
}
