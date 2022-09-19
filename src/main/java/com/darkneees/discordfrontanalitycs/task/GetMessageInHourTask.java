package com.darkneees.discordfrontanalitycs.task;

import com.google.gson.JsonParser;
import javafx.concurrent.Task;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class GetMessageInHourTask extends Task<String> {

    private Supplier<CompletableFuture<HttpResponse<String>>> getDataFromUrl;

    public GetMessageInHourTask(String url) {
        this.getDataFromUrl = new SendRequestSupplier(url);
    }

    @Override
    protected String call() {
        AtomicReference<String> entity = new AtomicReference<>();
        getDataFromUrl.get().thenAccept((input) -> entity.set(JsonParser.parseString(input.body()).getAsJsonObject().get("count").getAsString())).join();
        return entity.get();
    }
}
