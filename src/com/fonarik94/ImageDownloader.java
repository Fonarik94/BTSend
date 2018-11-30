package com.fonarik94;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.ws.http.HTTPException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageDownloader {
    public static byte[] download(String url) throws IOException {
        URL sourceUrl = new URL(url);
        HttpsURLConnection httpsConnection = (HttpsURLConnection) sourceUrl.openConnection();
        httpsConnection.addRequestProperty("Accept", "image/jpeg");
        httpsConnection.addRequestProperty("User-Agent", "Mozilla/4.0");
        if(!httpsConnection.getContentType().equals("image/jpeg")){
            throw new IOException("Incompatible response content type: " + httpsConnection.getContentType() + "; Required \"image/jpeg\"");
        }
        try (InputStream inputStream = new BufferedInputStream(httpsConnection.getInputStream());
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            if (httpsConnection.getResponseCode() == 200) {
                byte[] buffer = new byte[1024 * 8]; //Buffer size 8KB
                int data;
                while ((data = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, data);
                }
                System.out.println("New image downloaded");
                return outputStream.toByteArray();
            } else {
                throw new IOException("Download error. HTTP response code = " + httpsConnection.getResponseCode());
            }
        }
    }
}
