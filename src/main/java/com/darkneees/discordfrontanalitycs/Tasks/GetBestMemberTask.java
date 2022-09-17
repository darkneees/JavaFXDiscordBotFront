package com.darkneees.discordfrontanalitycs.Tasks;

import com.darkneees.discordfrontanalitycs.Entity.UserEntity;
import com.google.gson.Gson;
import javafx.concurrent.Task;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class GetBestMemberTask extends Task<UserEntity> {

    private Supplier<HttpResponse<String>> getDataFromUrl;

    public GetBestMemberTask(String url) {
        this.getDataFromUrl = new GetDataSupplier(url);
    }

    @Override
    protected UserEntity call() {
        AtomicReference<UserEntity> entity = new AtomicReference<>();
        CompletableFuture.supplyAsync(getDataFromUrl)
                .thenAccept((input) -> {
                    System.out.println(input);
                    entity.set(new Gson().fromJson(input.body(), UserEntity.class));
                }).join();

        return entity.get();
    }
}
