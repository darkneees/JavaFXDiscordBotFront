package com.darkneees.discordfrontanalitycs;


import com.darkneees.discordfrontanalitycs.Entity.ChannelEntity;
import com.darkneees.discordfrontanalitycs.Entity.GuildEntity;
import com.darkneees.discordfrontanalitycs.Entity.UserEntity;
import com.darkneees.discordfrontanalitycs.Tasks.GetBestChannelTask;
import com.darkneees.discordfrontanalitycs.Tasks.GetBestMemberTask;
import com.darkneees.discordfrontanalitycs.Tasks.GetGuildsTask;
import com.darkneees.discordfrontanalitycs.Tasks.GetMessageInHourTask;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.HostServices;
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
import javafx.util.Duration;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;
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
    private Label bestUserId;
    @FXML
    private Label bestUserName;
    @FXML
    private Label loadingProgress;
    @FXML
    private Label bestChannelId;
    @FXML
    private Label bestChannelName;
    @FXML
    private Label messageInHour;
    private final ObservableList<GuildEntity> guildsObs = FXCollections.observableArrayList();
    @FXML
    private Circle avatar;
    @FXML
    private ComboBox comboGuilds;
    private String host;
    private final ExecutorService service = Executors.newCachedThreadPool();

    @FXML
    public void initialize() {
        comboGuilds.setItems(guildsObs);
        BoxAvatar.setVisible(false);
        loadConfiguration();

        Timeline timerUpdate = new Timeline(new KeyFrame(
                Duration.seconds(5.0),
                (e) -> {
                    if(runTimer.isSelected()) refreshInfo();
                }));
        timerUpdate.setCycleCount(Animation.INDEFINITE);
        timerUpdate.play();
    }

    public void refreshInfo() {
        GuildEntity entity = (GuildEntity) comboGuilds.getValue();
        if(entity != null) {
            Timeline line = StartLoadingService();
            Task<Void> UpdateInfo = new Task<>() {
                @Override
                protected Void call() {
                    UpdateBestMember(entity.getId());
                    UpdateBestChannel(entity.getId());
                    UpdateMessageInHour(entity.getId());

                    return null;
                }
            };
            UpdateInfo.setOnSucceeded((e) -> SuccessLoadingService(line));
            service.execute(UpdateInfo);
        } else WindowException("Dont' find this guild", "Guilds");
    }

    public void refreshGuilds() {
        Task<ObservableList<GuildEntity>> task = new GetGuildsTask(host + "/guilds");
        comboGuilds.itemsProperty().bind(task.valueProperty());
        Timeline line = StartLoadingService();
        service.execute(task);
        task.setOnSucceeded((element) -> {
            comboGuilds.itemsProperty().unbind();
            SuccessLoadingService(line);
        });
        task.setOnFailed((element -> {

        }));
    }

    public void goLink() {
        GuildEntity guild = (GuildEntity) comboGuilds.getValue();
        hostServices.showDocument("https://discord.com/users/" + guild.getId());
    }

    private void UpdateBestMember(String id) {
        StringBuilder sb = new StringBuilder(host).append("/guild/").append(id).append("/bestmember");
        Task<UserEntity> task = new GetBestMemberTask(sb.toString());
        task.setOnSucceeded(e -> {
            UserEntity user = task.getValue();
            bestUserName.setText("Username: " + user.getName());
            bestUserId.setText("User id: " + user.getId());
            avatar.setFill(new ImagePattern(new Image(user.getAvatar())));
            BoxAvatar.setVisible(true);
        });
        task.setOnFailed(e -> WindowException("Don't find best user", "/bestmember"));
        service.execute(task);
    }

    private void UpdateBestChannel(String id){
        StringBuilder sb = new StringBuilder(host).append("/guild/").append(id).append("/bestchannel");
        Task<ChannelEntity> task = new GetBestChannelTask(sb.toString());
        task.setOnSucceeded(e -> {
            ChannelEntity entity = task.getValue();

            bestChannelId.setText("Channel id:" + entity.getId());
            bestChannelName.setText("Channel name: " + entity.getName());
            BoxAvatar.setVisible(true);
        });
        task.setOnFailed(e -> WindowException("Don't find best channel", "/bestchannel"));
        service.execute(task);
    }

    private void UpdateMessageInHour(String id){
        StringBuilder sb = new StringBuilder(host).append("/guild/").append(id).append("/messagesinhour");
        Task<String> task = new GetMessageInHourTask(sb.toString());
        task.setOnSucceeded(e -> messageInHour.setText("Count messages: " + task.getValue()));
        task.setOnFailed(e -> WindowException("Don't find message in hour", "/messageinhour"));
        service.execute(task);
    }

    private Timeline StartLoadingService(){
        loadingProgress.setText("Loading");
        loadingProgress.setTextFill(Color.YELLOW);
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(1000), new KeyValue(loadingProgress.textProperty(), "Loading.")),
                new KeyFrame(Duration.millis(1500), new KeyValue(loadingProgress.textProperty(), "Loading..")),
                new KeyFrame(Duration.millis(2000), new KeyValue(loadingProgress.textProperty(), "Loading...")),
                new KeyFrame(Duration.millis(2500), new KeyValue(loadingProgress.textProperty(), "Loading"))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        return timeline;
    }

    private void SuccessLoadingService(Timeline timeline) {
        timeline.stop();
        loadingProgress.setTextFill(Color.GREEN);
        loadingProgress.setText("Loaded");
    }

    public void WindowException(String exception, String title){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(title);
        alert.setContentText(exception);
        alert.show();
    }

    private void loadConfiguration(){
        Yaml yaml = new Yaml();
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("application.yml");
        Map<String, Object> config = yaml.load(inputStream);
        host = (String) config.get("host");
    }
}