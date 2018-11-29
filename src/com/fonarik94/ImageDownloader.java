package com.fonarik94;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.ws.http.HTTPException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

class ImageDownloader {
    public static byte[] download() throws IOException {
        URL url = new URL("https://source.unsplash.com/random/?nature");
        HttpsURLConnection httpsConnection = (HttpsURLConnection) url.openConnection();
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
