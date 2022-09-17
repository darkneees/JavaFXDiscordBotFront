package com.darkneees.discordfrontanalitycs;


import com.darkneees.discordfrontanalitycs.Entity.GuildEntity;
import com.darkneees.discordfrontanalitycs.Entity.UserEntity;
import com.darkneees.discordfrontanalitycs.Tasks.GetBestMemberTask;
import com.darkneees.discordfrontanalitycs.Tasks.GetGuildsTask;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.SimpleObjectProperty;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainController {

    @FXML
    private Button refreshGuildsButton;
    @FXML
    private Button refreshInfoButton;
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
    private String host;

    private SimpleObjectProperty<GuildEntity> guildEntityObjectBinding = new SimpleObjectProperty<>();

    @FXML
    public void initialize() {
        comboGuilds.setItems(guildsObs);


        BoxAvatar.setVisible(false);
        loadConfiguration();
    }

    public void refreshInfo() {
        Task<UserEntity> task = new GetBestMemberTask("http://localhost:8080/guild/1013860197233610823/bestmember");

        task.setOnSucceeded(e -> {
            UserEntity user = task.getValue();

            name.setText(user.getName());
            id.setText(user.getId());
            avatar.setFill(new ImagePattern(new Image(user.getAvatar())));
            BoxAvatar.setVisible(true);
        });


        //avatar
        service.execute(task);
    }

    public void refreshGuilds() {
        Task<ObservableList<GuildEntity>> task = new GetGuildsTask("http://localhost:8080/guilds");
        comboGuilds.itemsProperty().bind(task.valueProperty());
        System.out.println("task");
        service.execute(task);
        task.setOnSucceeded((element) -> comboGuilds.itemsProperty().unbind());
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

    private void loadConfiguration(){
        Yaml yaml = new Yaml();
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("application.yml");
        Map<String, Object> config = yaml.load(inputStream);
        host = (String) config.get("host");
    }
}