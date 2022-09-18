package com.darkneees.discordfrontanalitycs.Tasks;

import com.darkneees.discordfrontanalitycs.Entity.GuildEntity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class GetGuildsTask extends Task<ObservableList<GuildEntity>> {

    private Supplier<CompletableFuture<HttpResponse<String>>> getDataFromUrl;
    public GetGuildsTask(String url) {
        this.getDataFromUrl = new GetDataSupplier(url);
    }

    @Override
    protected ObservableList<GuildEntity> call() {

        Type guildsType = new TypeToken<List<GuildEntity>>() {}.getType();
        ObservableList<GuildEntity> items = FXCollections.observableArrayList();

        getDataFromUrl.get().thenAccept((request) -> {
            if(request.statusCode() == 500) throw new RuntimeException();
            else {
                List<GuildEntity> guildEntities = new Gson().fromJson(request.body(), guildsType);
                items.addAll(guildEntities);
            }
        }).join();

        return items;
    }

}
