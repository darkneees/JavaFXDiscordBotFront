package com.darkneees.discordfrontanalitycs.Tasks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Supplier;

public class GetDataSupplier implements Supplier<String> {

    private String urlString;

    public GetDataSupplier(String urlString) {
        this.urlString = urlString;
    }


    @Override
    public String get() {
        StringBuilder content = new StringBuilder();
        try {
            BufferedReader in;
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            int statusCode = con.getResponseCode();
            if(statusCode >= 400) in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            else in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            if(statusCode >= 400) throw new Exception(content.toString());
            return content.toString();
        }
        catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
