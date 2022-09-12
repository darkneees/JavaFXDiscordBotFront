package com.darkneees.discordfrontanalitycs.Tasks;

import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

public class CreateTaskLoading extends Task<Task> {

    private Label loadingProgress;

    public CreateTaskLoading(Label loadingProgress) {
        this.loadingProgress = loadingProgress;
    }

    @Override
    protected Task call() {
        Task<Void> loadingTask = new LoadingTask();
        loadingProgress.setTextFill(Color.YELLOW);
        loadingProgress.textProperty().bind(loadingTask.messageProperty());
        return loadingTask;
    }
}
