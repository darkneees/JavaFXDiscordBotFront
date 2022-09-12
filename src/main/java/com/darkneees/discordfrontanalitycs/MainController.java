package com.darkneees.discordfrontanalitycs;


import com.darkneees.discordfrontanalitycs.Entity.GuildEntity;
import com.darkneees.discordfrontanalitycs.Entity.UserEntity;
import com.darkneees.discordfrontanalitycs.Tasks.GetDataSupplier;
import com.darkneees.discordfrontanalitycs.Tasks.LoadingTask;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainController {

    @FXML
    private CheckBox runTimer;
    @FXML
    private VBox BoxAvatar;
    private HostServices hostServices;
    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }
    @FXML
    private Label id;
    @FXML
    private Label name;
    @FXML
    private Label loadingProgress;
    @FXML
    private Label idChannel;
    @FXML
    private Label nameChannel;
    @FXML
    private Label messageInHour;
    private final ObservableList<GuildEntity> guildsObs = FXCollections.observableArrayList();
    @FXML
    private Circle avatar;
    @FXML
    private ComboBox comboGuilds;
    private final ExecutorService service = Executors.newSingleThreadExecutor();


    @FXML
    public void initialize() {
        comboGuilds.setItems(guildsObs);
        BoxAvatar.setVisible(false);

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if(runTimer.isSelected()) Platform.runLater(() -> UpdateGuildInfo());
            }
        };
        Timer timer = new Timer("timer");
        timer.scheduleAtFixedRate(timerTask, 300000000, 300000000);
    }

    public void refreshInfo() {
        UpdateGuildInfo();
    }

    public void refreshGuilds() {
        UpdateListGuilds();
    }

    private void UpdateGuildInfo() {

        GuildEntity guild = (GuildEntity) comboGuilds.getValue();
        if(guild != null) {
            Task<Void> loadingTask = CreateTaskLoading();
            service.execute(loadingTask);
            CompletableFuture<?> member = CompletableFuture.supplyAsync(
                            (new GetDataSupplier("http://localhost:8080/guild/" + guild.getId() + "/bestmember")))
                    .whenComplete((input, exception) -> {
                        if(exception == null) {
                            UserEntity user = new Gson().fromJson(input, UserEntity.class);
                            Platform.runLater(() -> {
                                id.setText("Id user: " + user.getId());
                                name.setText("Name user: " + user.getName());
                                avatar.setFill(new ImagePattern(new Image(user.getAvatar())));
                            });
                        }
                    });

            CompletableFuture<?> channel = CompletableFuture.supplyAsync(
                            (new GetDataSupplier("http://localhost:8080/guild/" + guild.getId() + "/bestchannel")))
                    .whenComplete((input, exception) -> {
                        if(exception == null) {
                        GuildEntity channelEntity = new Gson().fromJson(input, GuildEntity.class);
                        Platform.runLater(() -> {
                                idChannel.setText("Id channel: " + channelEntity.getId());
                                nameChannel.setText("Name channel: " + channelEntity.getName());
                            });
                        }
                    });

            CompletableFuture<?> message = CompletableFuture.supplyAsync(
                            (new GetDataSupplier("http://localhost:8080/guild/" + guild.getId() + "/messagesinhour")))
                    .whenComplete((input, exception) -> {
                        if(exception == null) {
                            String result = "No messages have been written on the server for an hour.";
                            if(!input.equals(""))
                                result = "For an hour on the server it is written " +
                                        JsonParser.parseString(input).getAsJsonObject().get("body").getAsString() + " messages";
                            String finalResult = result;
                            Platform.runLater(() -> messageInHour.setText(finalResult));
                        }
                    });
            CompletableFuture<Void> all = CompletableFuture.allOf(member, channel, message);
            all.whenComplete((input, exception) -> Platform.runLater(() ->  {
                FreeTaskLoading(loadingTask);
                if(exception != null) WindowException(exception.getMessage());
                else BoxAvatar.setVisible(true);
            }));
        } else WindowException("Please choose guild in Combo Box");
    }

    private void UpdateListGuilds() {
        Task<Void> loadingTask = CreateTaskLoading();
        CompletableFuture<?> task = CompletableFuture.supplyAsync(
                (new GetDataSupplier("http://localhost:8080/guilds"))
        ).whenComplete((input, exception) -> {
            Type guildsType = new TypeToken<ArrayList<GuildEntity>>() {
            }.getType();
            ArrayList<GuildEntity> guilds = new Gson().fromJson(input, guildsType);
            Platform.runLater(() -> {
                guildsObs.clear();
                guildsObs.addAll(guilds);
            });
        });

        CompletableFuture<Void> start = CompletableFuture.allOf(task);
        start.whenComplete((input, exception) -> Platform.runLater(() -> {
            FreeTaskLoading(loadingTask);
            if(exception != null) WindowException(exception.getMessage());
        }));
    }

    private Task<Void> CreateTaskLoading(){
        Task<Void> loadingTask = new LoadingTask();
        loadingProgress.setTextFill(Color.YELLOW);
        loadingProgress.textProperty().bind(loadingTask.messageProperty());
        return loadingTask;
    }

    private void FreeTaskLoading(Task<Void> loadingTask){
        loadingTask.cancel();
        loadingProgress.textProperty().unbind();
        loadingProgress.setText("Loaded");
        loadingProgress.setTextFill(Color.GREEN);
    }

    public void goLink() {
        GuildEntity guild = (GuildEntity) comboGuilds.getValue();
        hostServices.showDocument("https://discord.com/users/" + guild.getId());
    }

    public void WindowException(String exception){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(exception);
        alert.showAndWait();
    }
}