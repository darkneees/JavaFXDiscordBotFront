package com.darkneees.discordfrontanalitycs.Tasks;

import javafx.concurrent.Task;

public class LoadingTask extends Task<Void> {

    public LoadingTask() {
    }

    @Override
    protected Void call() {
        while (!isCancelled()) {
            updateMessage("Loading.");
            sleep();
            updateMessage("Loading..");
            sleep();
            updateMessage("Loading...");
            sleep();
            updateMessage("Loading....");
            sleep();
        }
        return null;
    }


    private void sleep(){
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
