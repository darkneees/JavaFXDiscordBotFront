package com.darkneees.discordfrontanalitycs;


import com.darkneees.discordfrontanalitycs.Entity.ChannelEntity;
import com.darkneees.discordfrontanalitycs.Entity.GuildEntity;
import com.darkneees.discordfrontanalitycs.Entity.UserEntity;
import com.darkneees.discordfrontanalitycs.Tasks.GetBestChannelTask;
import com.darkneees.discordfrontanalitycs.Tasks.GetBestMemberTask;
import com.darkneees.discordfrontanalitycs.Tasks.GetGuildsTask;
import com.darkneees.discordfrontanalitycs.Tasks.GetMessageInHourTask;
import javafx.application.HostServices;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
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
    private ExecutorService service = Executors.newCachedThreadPool();


    @FXML
    public void initialize() {
        comboGuilds.setItems(guildsObs);
        BoxAvatar.setVisible(false);
        loadConfiguration();
    }

    public void refreshInfo() {
        UpdateBestMember();
        UpdateBestChannel();
        UpdateMessageInHour();
    }

    public void refreshGuilds() {
        Task<ObservableList<GuildEntity>> task = new GetGuildsTask("http://localhost:8080/guilds");
        comboGuilds.itemsProperty().bind(task.valueProperty());
        service.execute(task);
        task.setOnSucceeded((element) -> comboGuilds.itemsProperty().unbind());
    }

    public void goLink() {
        GuildEntity guild = (GuildEntity) comboGuilds.getValue();
        hostServices.showDocument("https://discord.com/users/" + guild.getId());
    }

    private void UpdateBestMember() {
        Task<UserEntity> task = new GetBestMemberTask("http://localhost:8080/guild/1013860197233610823/bestmember");

        task.setOnSucceeded(e -> {
            UserEntity user = task.getValue();

            bestUserName.setText("Username: " + user.getName());
            bestUserId.setText("User id: " + user.getId());
            avatar.setFill(new ImagePattern(new Image(user.getAvatar())));
            BoxAvatar.setVisible(true);
        });

        service.execute(task);
    }

    private void UpdateBestChannel(){
        Task<ChannelEntity> task = new GetBestChannelTask("http://localhost:8080/guild/1013860197233610823/bestchannel");

        task.setOnSucceeded(e -> {
            ChannelEntity entity = task.getValue();

            bestChannelId.setText("Channel id:" + entity.getId());
            bestChannelName.setText("Channel name: " + entity.getName());
            BoxAvatar.setVisible(true);
        });
        service.execute(task);
    }

    private void UpdateMessageInHour(){
        Task<String> task = new GetMessageInHourTask("http://localhost:8080/guild/1013860197233610823/messagesinhour");

        task.setOnSucceeded(e -> {
            String count = task.getValue();
            messageInHour.setText("Count messages: " + count);
        });
        service.execute(task);
    }

    public void WindowException(String exception){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(exception);
        alert.showAndWait();
    }

    private void loadConfiguration(){
        Yaml yaml = new Yaml();
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("application.yml");
        Map<String, Object> config = yaml.load(inputStream);
        host = (String) config.get("host");
    }
}