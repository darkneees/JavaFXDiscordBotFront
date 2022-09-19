package com.darkneees.discordfrontanalitycs.task;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class SendRequestSupplier implements Supplier<CompletableFuture<HttpResponse<String>>> {

    private String urlString;

    public SendRequestSupplier(String urlString) {
        this.urlString = urlString;
    }

    @Override
    public CompletableFuture<HttpResponse<String>> get() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(urlString))
                    .GET()
                    .build();

            return HttpClient.newBuilder()
                    .build()
                    .sendAsync(request, HttpResponse.BodyHandlers.ofString());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
