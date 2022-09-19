package com.darkneees.discordfrontanalitycs.task;

import com.darkneees.discordfrontanalitycs.entity.ChannelEntity;
import com.google.gson.Gson;
import javafx.concurrent.Task;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class GetBestChannelTask extends Task<ChannelEntity> {

    private Supplier<CompletableFuture<HttpResponse<String>>> getDataFromUrl;

    public GetBestChannelTask(String url) {
        this.getDataFromUrl = new SendRequestSupplier(url);
    }

    @Override
    protected ChannelEntity call() {
        AtomicReference<ChannelEntity> entity = new AtomicReference<>();
        getDataFromUrl.get()
                .thenAccept((input) -> {
            if(input.statusCode() == 500) throw new RuntimeException();
            else entity.set(new Gson().fromJson(input.body(), ChannelEntity.class));
        }).join();
        return entity.get();
    }
}
